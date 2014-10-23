package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.business.IDept;
import com.bstek.bdf2.core.business.IUser;
import com.bstek.bdf2.core.context.ContextHolder;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.annotation.Expose;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.interceptor.CalculateCore;
import com.quotamanagesys.interceptor.ResultTableCreator;
import com.quotamanagesys.model.QuotaItem;
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaProperty;
import com.quotamanagesys.model.QuotaPropertyValue;
import com.quotamanagesys.model.QuotaTargetValue;

@Component
public class QuotaTargetValueDao extends HibernateDao {
	
	@Resource
	QuotaItemDao quotaItemDao;
	@Resource
	QuotaPropertyDao quotaPropertyDao;
	@Resource
	CalculateCore calculateCore;
	@Resource
	ResultTableCreator resultTableCreator;
	@Resource
	QuotaItemCreatorDao quotaItemCreatorDao;
	@Resource
	QuotaPropertyValueDao quotaPropertyValueDao;
	
	@DataProvider
	public Collection<QuotaTargetValue> getAll(){
		String hqlString="from "+QuotaTargetValue.class.getName();
		Collection<QuotaTargetValue> quotaTargetValues=this.query(hqlString);
		return quotaTargetValues;
	}
	
	@DataProvider
	public Collection<QuotaTargetValue> getQuotaTargetValuesByManageDept(String manageDeptId) throws Exception{
		Collection<QuotaItemCreator> quotaItemCreators=quotaItemCreatorDao.getQuotaItemCreatorsByManageDept(manageDeptId);	
		Collection<QuotaItem> quotaItems=new ArrayList<QuotaItem>();
		for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
			quotaItems.addAll(quotaItemDao.getQuotaItemsByQuotaItemCreator(quotaItemCreator.getId()));
		}
		
		Collection<QuotaTargetValue> quotaTargetValues=new ArrayList<QuotaTargetValue>();
		for (QuotaItem quotaItem : quotaItems) {
			quotaTargetValues.addAll(getQuotaTargetValuesByQuotaItem(quotaItem.getId()));
		}
		return quotaTargetValues;
	}
	
	@DataProvider
	public Collection<QuotaTargetValue> getQuotaTargetValuesByQuotaProperty(String quotaPropertyId){
		String hqlString="from "+QuotaTargetValue.class.getName()+" where quotaProperty.id='"+quotaPropertyId+"'";
		Collection<QuotaTargetValue> quotaTargetValues=this.query(hqlString);
		return quotaTargetValues;
	}
	
	@DataProvider
	public Collection<QuotaTargetValue> getQuotaTargetValuesByQuotaItem(String quotaItemId){
		String hqlString="from "+QuotaTargetValue.class.getName()+" where quotaItem.id='"+quotaItemId+"'";
		Collection<QuotaTargetValue> quotaTargetValues=this.query(hqlString);
		return quotaTargetValues;
	}
	
	@DataProvider
	public QuotaTargetValue getQuotaTargetValue(String id){
		String hqlString="from "+QuotaTargetValue.class.getName()+" where id='"+id+"'";
		List<QuotaTargetValue> quotaTargetValues=this.query(hqlString);
		if (quotaTargetValues.size()>0) {
			return quotaTargetValues.get(0);
		}else {
			return null;
		}
	}
	
	@DataResolver
	public void saveQuotaTargetValues(Collection<QuotaTargetValue> quotaTargetValues){
		Session session=this.getSessionFactory().openSession();
		Collection<QuotaItem> updateQuotaItems=new ArrayList<QuotaItem>();
		
		try {
			for (QuotaTargetValue quotaTargetValue : quotaTargetValues) {
				EntityState state=EntityUtils.getState(quotaTargetValue);
				if (state.equals(EntityState.NEW)) {
					session.merge(quotaTargetValue);
				}else if (state.equals(EntityState.MODIFIED)) {
					QuotaItem thisQuotaItem=quotaItemDao.getQuotaItem(quotaTargetValue.getQuotaItem().getId());
					QuotaProperty thisQuotaProperty=quotaPropertyDao.getQuotaProperty(quotaTargetValue.getQuotaProperty().getId());
					quotaTargetValue.setQuotaItem(thisQuotaItem);
					quotaTargetValue.setQuotaProperty(thisQuotaProperty);
					session.merge(quotaTargetValue);
					session.flush();
					session.clear();
					updateQuotaItems.add(thisQuotaItem);
				}else if (state.equals(EntityState.DELETED)) {
					quotaTargetValue.setQuotaItem(null);
					quotaTargetValue.setQuotaProperty(null);
					session.delete(quotaTargetValue);
				}
			}
			
			calculateCore.calculate(updateQuotaItems);
			resultTableCreator.createOrUpdateResultTable(updateQuotaItems);
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@DataResolver
	public void deleteQuotaTargetValues(Collection<QuotaTargetValue> quotaTargetValues){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaTargetValue quotaTargetValue : quotaTargetValues) {
				quotaTargetValue.setQuotaItem(null);
				quotaTargetValue.setQuotaProperty(null);
				session.delete(quotaTargetValue);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	//修复月度目标值
	@Expose
	public void repairQuotaTargetValues(String quotaItemCreatorId){
		Session session=this.getSessionFactory().openSession();
		Collection<QuotaItem> quotaItems=quotaItemDao.getQuotaItemsByQuotaItemCreator(quotaItemCreatorId);
		Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreatorId);
		try {
			for (QuotaItem quotaItem : quotaItems) {
				Collection<QuotaTargetValue> quotaTargetValues=getQuotaTargetValuesByQuotaItem(quotaItem.getId());
				Collection<QuotaTargetValue> add=new ArrayList<QuotaTargetValue>();
				Collection<QuotaTargetValue> delete=new ArrayList<QuotaTargetValue>();
				
				for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
					boolean isFinded=false;
					for (QuotaTargetValue quotaTargetValue : quotaTargetValues) {
						if (quotaTargetValue.getQuotaProperty().equals(quotaPropertyValue.getQuotaProperty())) {
							isFinded=true;
							break;
						}
					}
					if (isFinded==false) {
						QuotaTargetValue quotaTargetValue = new QuotaTargetValue();
						quotaTargetValue.setQuotaItem(quotaItem);
						quotaTargetValue.setQuotaProperty(quotaPropertyValue.getQuotaProperty());
						quotaTargetValue.setParameterName(quotaPropertyValue.getQuotaProperty().getParameterName()+"_M");
						add.add(quotaTargetValue);
					}
				}
				
				for (QuotaTargetValue quotaTargetValue : quotaTargetValues) {
					boolean isFind=false;
					for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
						if (quotaPropertyValue.getQuotaProperty().equals(quotaTargetValue.getQuotaProperty())) {
							isFind=true;
							break;
						}
					}
					if (isFind==false) {
						delete.add(quotaTargetValue);
					}
				}
				
				for (QuotaTargetValue quotaTargetValue : add) {
					session.merge(quotaTargetValue);
					session.flush();
					session.clear();
				}
				
				deleteQuotaTargetValues(delete);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}	
	}
	
	//修复指标管理部门所有月度目标值
	@Expose
	public void repairQuotaTargetValuesByManageDept(String manageDeptId) throws Exception{
		Collection<QuotaItemCreator> quotaItemCreators=quotaItemCreatorDao.getQuotaItemCreatorsByManageDept(manageDeptId);
		for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
			repairQuotaTargetValues(quotaItemCreator.getId());
		}
	}
	
	@DataResolver
	public void excuteHQL(String HQL) {
		Session session = this.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			Query query = session.createQuery(HQL);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
			System.out.println(e.toString());
		} finally {
			session.flush();
			session.close();
		}
	}
}
