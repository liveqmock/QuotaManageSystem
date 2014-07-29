package com.quotamanagesys.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.QuotaProperty;

@Component
public class QuotaPropertyDao extends HibernateDao {

	@DataProvider
	public Collection<QuotaProperty> getAll(){
		String hqlString="from "+QuotaProperty.class.getName();
		Collection<QuotaProperty> quotaProperties=this.query(hqlString);
		return quotaProperties;
	}
	
	@DataProvider
	public QuotaProperty getQuotaProperty(String id){
		String hqlString="from "+QuotaProperty.class.getName()+" where id='"+id+"'";
		List<QuotaProperty> quotaProperties=this.query(hqlString);
		if (quotaProperties.size()>0) {
			return quotaProperties.get(0);
		}else {
			return null;
		}
	}
	
	@DataProvider
	public QuotaProperty getQuotaPropertyByName(String quotaPropertyName){
		String hqlString="from "+QuotaProperty.class.getName()+" where name='"+quotaPropertyName+"'";
		List<QuotaProperty> quotaProperties=this.query(hqlString);
		if (quotaProperties.size()>0) {
			return quotaProperties.get(0);
		}else {
			return null;
		}
	}
	
	@DataResolver
	public void saveQuotaProperties(Collection<QuotaProperty> quotaProperties){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaProperty quotaProperty : quotaProperties) {
				EntityState state=EntityUtils.getState(quotaProperty);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaProperty);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.update(quotaProperty);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaProperty);
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
