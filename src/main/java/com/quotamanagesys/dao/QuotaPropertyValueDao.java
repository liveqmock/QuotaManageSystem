package com.quotamanagesys.dao;

import java.util.Collection;
import java.util.List;

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
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaPropertyValue;

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
		try {
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				EntityState state=EntityUtils.getState(quotaPropertyValue);
				if (state.equals(EntityState.NEW)) {
					quotaPropertyValue.setQuotaItemCreator(quotaItemCreator);
					session.save(quotaPropertyValue);
				}else if (state.equals(EntityState.MODIFIED)) {
					quotaPropertyValue.setQuotaItemCreator(quotaItemCreator);
					session.merge(quotaPropertyValue);
				}else if (state.equals(EntityState.DELETED)) {
					quotaPropertyValue.setQuotaItemCreator(null);
					quotaPropertyValue.setQuotaProperty(null);
					session.delete(quotaPropertyValue);
				}
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
