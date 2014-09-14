package com.quotamanagesys.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.FormulaParameter;
import com.quotamanagesys.model.QuotaFormula;
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaProperty;
import com.quotamanagesys.model.QuotaPropertyValue;
import com.quotamanagesys.model.QuotaType;
import com.quotamanagesys.model.QuotaTypeFormulaLink;

@Component
public class QuotaPropertyValueDao extends HibernateDao {

	@Resource
	QuotaItemCreatorDao quotaItemCreatorDao;
	
	@DataProvider
	public Collection<QuotaPropertyValue> getAll(){
		String hqlString="from "+QuotaPropertyValue.class.getName();
		Collection<QuotaPropertyValue> quotaPropertyValues=this.query(hqlString);
		return quotaPropertyValues;
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
	
	@DataResolver
	public void saveQuotaPropertyValues(Collection<QuotaPropertyValue> quotaPropertyValues,String quotaItemCreatorId){
		Session session=this.getSessionFactory().openSession();
		QuotaItemCreator quotaItemCreator=quotaItemCreatorDao.getQuotaItemCreator(quotaItemCreatorId);
		boolean isFormulaLinkChange=false;
		
		try {
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				EntityState state=EntityUtils.getState(quotaPropertyValue);
				if (state.equals(EntityState.NEW)) {
					quotaPropertyValue.setQuotaItemCreator(quotaItemCreator);
					session.save(quotaPropertyValue);
					session.flush();
					session.clear();
					isFormulaLinkChange=true;
				}else if (state.equals(EntityState.MODIFIED)) {
					quotaPropertyValue.setQuotaItemCreator(quotaItemCreator);
					session.merge(quotaPropertyValue);
					session.flush();
					session.clear();
				}else if (state.equals(EntityState.DELETED)) {
					quotaPropertyValue.setQuotaItemCreator(null);
					quotaPropertyValue.setQuotaProperty(null);
					session.delete(quotaPropertyValue);
					session.flush();
					session.clear();
					isFormulaLinkChange=true;
				}
			}
			
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
		try {
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				quotaPropertyValue.setQuotaItemCreator(null);
				quotaPropertyValue.setQuotaProperty(null);
				session.delete(quotaPropertyValue);
			}
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
