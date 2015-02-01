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
import com.quotamanagesys.model.FormulaParameter;
import com.quotamanagesys.model.QuotaFormula;
import com.quotamanagesys.model.QuotaItem;
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaProperty;
import com.quotamanagesys.model.QuotaPropertyValue;
import com.quotamanagesys.model.QuotaTargetValue;
import com.quotamanagesys.model.QuotaType;
import com.quotamanagesys.model.QuotaTypeFormulaLink;
import com.quotamanagesys.tools.CriteriaConvertCore;

@Component
public class QuotaPropertyValueDao extends HibernateDao {

	@Resource
	QuotaItemCreatorDao quotaItemCreatorDao;
	@Resource
	QuotaItemDao quotaItemDao;
	@Resource
	CalculateCore calculateCore;
	@Resource
	ResultTableCreator resultTableCreator;
	@Resource
	QuotaTargetValueDao quotaTargetValueDao;
	@Resource
	CriteriaConvertCore criteriaConvertCore;
	
	@DataProvider
	public Collection<QuotaPropertyValue> getAll(){
		String hqlString="from "+QuotaPropertyValue.class.getName();
		Collection<QuotaPropertyValue> quotaPropertyValues=this.query(hqlString);
		return quotaPropertyValues;
	}
	
	@DataProvider
	public Collection<QuotaPropertyValue> getQuotaPropertyValuesByManageDept(String manageDeptId) throws Exception{
		String hqlString="from "+QuotaPropertyValue.class.getName()+" where quotaItemCreator.quotaType.manageDept.id='"+manageDeptId+"'";
		Collection<QuotaPropertyValue> quotaPropertyValues=this.query(hqlString);
		return quotaPropertyValues;
	}
	
	//分页方式查询
	@DataProvider
	public void getQuotaPropertyValuesByManageDeptWithPage(Page<QuotaPropertyValue> page,Criteria criteria,String manageDeptId) throws Exception{
		if (manageDeptId!=null) {
			String filterString=criteriaConvertCore.convertToSQLString(criteria);
			if (!filterString.equals("")) {
				filterString=" and ("+filterString+")";
			}
			
			String hqlString="from "+QuotaPropertyValue.class.getName()+" where quotaItemCreator.quotaType.manageDept.id='"+manageDeptId+"'"
			+filterString;
			this.pagingQuery(page, hqlString, "select count(*)" + hqlString);
		} else {
			System.out.print("参数为空");
		}
	}
	
	@DataProvider
	public QuotaPropertyValue getQuotaPropertyValue(String id){
		String hqlString="from "+QuotaPropertyValue.class.getName()+" where id='"+id+"'";
		List<QuotaPropertyValue> quotaPropertyValues=this.query(hqlString);
		if (quotaPropertyValues.size()>0) {
			return quotaPropertyValues.get(0);
		}else {
			return null;
		}
	}
	
	@DataProvider
	public Collection<QuotaPropertyValue> getQuotaPropertyValuesByQuotaProperty(String quotaPropertyId){
		String hqlString="from "+QuotaPropertyValue.class.getName()+" where quotaProperty.id='"+quotaPropertyId+"'";
		Collection<QuotaPropertyValue> quotaPropertyValues=this.query(hqlString);
		return quotaPropertyValues;
	}
	
	@DataProvider
	public Collection<QuotaPropertyValue> getQuotaPropertyValuesByQuotaItemCreator(String quotaItemCreatorId){
		String hqlString="from "+QuotaPropertyValue.class.getName()+" where quotaItemCreator.id='"+quotaItemCreatorId+"'";
		Collection<QuotaPropertyValue> quotaPropertyValues=this.query(hqlString);
		return quotaPropertyValues;
	}
	
	//获取quotaitem的年度目标值
	@DataProvider
	public Collection<QuotaPropertyValue> getQuotaPropertyValuesByQuotaItem(String quotaItemId){
		if (quotaItemId==null) {
			return null;
		} else {
			QuotaItem quotaItem=quotaItemDao.getQuotaItem(quotaItemId);
			String quotaItemCreatorId=quotaItem.getQuotaItemCreator().getId();
			String hqlString="from "+QuotaPropertyValue.class.getName()+" where quotaItemCreator.id='"+quotaItemCreatorId+"' order by quotaProperty.name asc";
			Collection<QuotaPropertyValue> quotaPropertyValues=this.query(hqlString);
			return quotaPropertyValues;
		}
	}
	
	@DataResolver
	public void saveQuotaPropertyValues(Collection<QuotaPropertyValue> quotaPropertyValues,String quotaItemCreatorId){
		Session session=this.getSessionFactory().openSession();
		QuotaItemCreator quotaItemCreator=quotaItemCreatorDao.getQuotaItemCreator(quotaItemCreatorId);
		boolean isFormulaLinkChange=false;
		ArrayList<QuotaItem> updateQuotaItems=new ArrayList<QuotaItem>();
		
		try {
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				EntityState state=EntityUtils.getState(quotaPropertyValue);
				if (state.equals(EntityState.NEW)) {
					quotaPropertyValue.setQuotaItemCreator(quotaItemCreator);
					session.save(quotaPropertyValue);
					session.flush();
					session.clear();
					isFormulaLinkChange=true;
					Collection<QuotaItem> quotaItems=quotaItemDao.getQuotaItemsByQuotaItemCreator(quotaItemCreatorId);
					updateQuotaItems.addAll(quotaItems);
				}else if (state.equals(EntityState.MODIFIED)) {
					quotaPropertyValue.setQuotaItemCreator(quotaItemCreator);
					session.merge(quotaPropertyValue);
					session.flush();
					session.clear();
					Collection<QuotaItem> quotaItems=quotaItemDao.getQuotaItemsByQuotaItemCreator(quotaItemCreatorId);
					updateQuotaItems.addAll(quotaItems);
				}else if (state.equals(EntityState.DELETED)) {
					QuotaProperty quotaProperty=quotaPropertyValue.getQuotaProperty();
					Collection<QuotaItem> quotaItems=quotaItemDao.getQuotaItemsByQuotaItemCreator(quotaItemCreatorId);
					if (quotaItems.size()>0) {
						//删除具体指标相关的月度目标值
						for (QuotaItem quotaItem : quotaItems) {
							String hqlString="from "+QuotaTargetValue.class.getName()+" where quotaItem.id='"+quotaItem.getId()
									+"' and quotaProperty.id='"+quotaProperty.getId()+"'";
							Collection<QuotaTargetValue> quotaTargetValues=this.query(hqlString);
							quotaTargetValueDao.deleteQuotaTargetValues(quotaTargetValues);
						}
					}
					quotaPropertyValue.setQuotaItemCreator(null);
					quotaPropertyValue.setQuotaProperty(null);
					session.delete(quotaPropertyValue);
					session.flush();
					session.clear();
					isFormulaLinkChange=true;
					updateQuotaItems.addAll(quotaItems);
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
			
			//属性值做增删变动时，会重新更新该指标的公式关联
			if (isFormulaLinkChange) {
				quotaItemCreatorDao.linkQuotaFormulas(quotaItemCreatorId);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@DataResolver
	public void deleteQuotaPropertyValues(Collection<QuotaPropertyValue> quotaPropertyValues){
		Session session=this.getSessionFactory().openSession();
		ArrayList<QuotaItem> updateQuotaItems=new ArrayList<QuotaItem>();
		try {
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				QuotaItemCreator quotaItemCreator=quotaPropertyValue.getQuotaItemCreator();
				QuotaProperty quotaProperty=quotaPropertyValue.getQuotaProperty();
				Collection<QuotaItem> quotaItems=quotaItemDao.getQuotaItemsByQuotaItemCreator(quotaItemCreator.getId());
				if (quotaItems.size()>0) {
					//删除具体指标相关的月度目标值
					for (QuotaItem quotaItem : quotaItems) {
						String hqlString="from "+QuotaTargetValue.class.getName()+" where quotaItem.id='"+quotaItem.getId()
								+"' and quotaProperty.id='"+quotaProperty.getId()+"'";
						Collection<QuotaTargetValue> quotaTargetValues=this.query(hqlString);
						quotaTargetValueDao.deleteQuotaTargetValues(quotaTargetValues);
					}
				}
				quotaPropertyValue.setQuotaItemCreator(null);
				quotaPropertyValue.setQuotaProperty(null);
				session.delete(quotaPropertyValue);
				session.flush();
				session.clear();
				updateQuotaItems.addAll(quotaItems);
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
