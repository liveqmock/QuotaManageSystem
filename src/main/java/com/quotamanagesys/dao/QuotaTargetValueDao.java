package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
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
import com.bstek.dorado.data.provider.Criteria;
import com.bstek.dorado.data.provider.Page;
import com.quotamanagesys.interceptor.CalculateCore;
import com.quotamanagesys.interceptor.ResultTableCreator;
import com.quotamanagesys.model.QuotaItem;
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaProperty;
import com.quotamanagesys.model.QuotaPropertyValue;
import com.quotamanagesys.model.QuotaTargetValue;
import com.quotamanagesys.tools.CriteriaConvertCore;

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
	@Resource
	CriteriaConvertCore criteriaConvertCore;
	
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
	
	//分页方式查询
	@DataProvider
	public void getQuotaTargetValuesByManageDeptWithPage(Page<QuotaTargetValue> page,Criteria criteria,String manageDeptId) throws Exception{
		if (manageDeptId!=null) {
			String filterString=criteriaConvertCore.convertToSQLString(criteria);
			if (!filterString.equals("")) {
				filterString=" and ("+filterString+")";
			}
			
			String hqlString = "from " + QuotaTargetValue.class.getName()
					+ " where quotaItem.quotaItemCreator.quotaType.manageDept.id='"+manageDeptId+"' and quotaItem.month<>0"+filterString;
			this.pagingQuery(page, hqlString, "select count(*)" + hqlString);
		} else {
			System.out.print("参数为空");
		}
	}
	
	@DataProvider
	public Collection<QuotaTargetValue> getQuotaTargetValuesByQuotaProperty(String quotaPropertyId){
		String hqlString="from "+QuotaTargetValue.class.getName()+" where quotaProperty.id='"+quotaPropertyId+"'";
		Collection<QuotaTargetValue> quotaTargetValues=this.query(hqlString);
		return quotaTargetValues;
	}
	
	@DataProvider
	public Collection<QuotaTargetValue> getQuotaTargetValuesByQuotaItem(String quotaItemId){
		if (quotaItemId==null) {
			return null;
		} else {
			String hqlString="from "+QuotaTargetValue.class.getName()+" where quotaItem.id='"+quotaItemId+"'  order by quotaProperty.name asc";
			Collection<QuotaTargetValue> quotaTargetValues=this.query(hqlString);
			return quotaTargetValues;
		}
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
		ArrayList<QuotaItem> updateQuotaItems=new ArrayList<QuotaItem>();
		
		try {
			for (QuotaTargetValue quotaTargetValue : quotaTargetValues) {
				EntityState state=EntityUtils.getState(quotaTargetValue);
				if (state.equals(EntityState.NEW)) {
					QuotaItem thisQuotaItem=quotaItemDao.getQuotaItem(quotaTargetValue.getQuotaItem().getId());
					session.merge(quotaTargetValue);
					updateQuotaItems.add(thisQuotaItem);
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
					QuotaItem thisQuotaItem=quotaItemDao.getQuotaItem(quotaTargetValue.getQuotaItem().getId());
					quotaTargetValue.setQuotaItem(null);
					quotaTargetValue.setQuotaProperty(null);
					session.delete(quotaTargetValue);
					updateQuotaItems.add(thisQuotaItem);
				}
			}
			
			for ( int i = 0 ; i < updateQuotaItems.size() - 1 ; i ++ ) {  
			    for ( int j = updateQuotaItems.size() - 1 ; j > i; j -- ) {  
			      if (updateQuotaItems.get(j).getId().equals(updateQuotaItems.get(i).getId())) {  
			    	  updateQuotaItems.remove(j);  
			      }   
			    }   
			}

			calculateCore.calculate(updateQuotaItems);
			quotaItemDao.setAllowSubmitStatus(updateQuotaItems);
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
			ArrayList<QuotaItem> updateQuotaItems=new ArrayList<QuotaItem>();
			
			for (QuotaTargetValue quotaTargetValue : quotaTargetValues) {
				QuotaItem thisQuotaItem=quotaItemDao.getQuotaItem(quotaTargetValue.getQuotaItem().getId());
				quotaTargetValue.setQuotaItem(null);
				quotaTargetValue.setQuotaProperty(null);
				session.delete(quotaTargetValue);
				updateQuotaItems.add(thisQuotaItem);
			}
			
			for ( int i = 0 ; i < updateQuotaItems.size() - 1 ; i ++ ) {  
			    for ( int j = updateQuotaItems.size() - 1 ; j > i; j -- ) {  
			      if (updateQuotaItems.get(j).getId().equals(updateQuotaItems.get(i).getId())) {  
			    	  updateQuotaItems.remove(j);  
			      }   
			    }   
			}
			
			calculateCore.calculate(updateQuotaItems);
			quotaItemDao.setAllowSubmitStatus(updateQuotaItems);
			resultTableCreator.createOrUpdateResultTable(updateQuotaItems);
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
				
				if (add.size()>0) {
					ArrayList<QuotaItem> updateQuotaItems=new ArrayList<QuotaItem>();
					
					for (QuotaTargetValue quotaTargetValue : add) {
						session.merge(quotaTargetValue);
						session.flush();
						session.clear();
						updateQuotaItems.add(quotaTargetValue.getQuotaItem());
					}
					
					for ( int i = 0 ; i < updateQuotaItems.size() - 1 ; i ++ ) {  
					    for ( int j = updateQuotaItems.size() - 1 ; j > i; j -- ) {  
					      if (updateQuotaItems.get(j).getId().equals(updateQuotaItems.get(i).getId())) {  
					    	  updateQuotaItems.remove(j);  
					      }   
					    }   
					}
					
					calculateCore.calculate(updateQuotaItems);
					quotaItemDao.setAllowSubmitStatus(updateQuotaItems);
					resultTableCreator.createOrUpdateResultTable(updateQuotaItems);
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
