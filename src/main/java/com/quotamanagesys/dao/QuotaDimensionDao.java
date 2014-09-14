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
import com.quotamanagesys.model.QuotaDimension;

@Component
public class QuotaDimensionDao extends HibernateDao {
	
	@DataProvider
	public Collection<QuotaDimension> getAll(){
		String hqlString="from "+QuotaDimension.class.getName();
		Collection<QuotaDimension> quotaDimensions=this.query(hqlString);
		return quotaDimensions;
	}

	@DataProvider
	public QuotaDimension getQuotaDimension(String id){
		String hqlString="from "+QuotaDimension.class.getName()+" where id='"+id+"'";
		List<QuotaDimension> quotaDimensions=this.query(hqlString);
		if (quotaDimensions.size()>0) {
			return quotaDimensions.get(0);
 		}else {
			return null;
		}
	}
	
	@DataResolver
	public void saveQuotaDimensions(Collection<QuotaDimension> quotaDimensions){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaDimension quotaDimension : quotaDimensions) {
				EntityState state=EntityUtils.getState(quotaDimension);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaDimension);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.update(quotaDimension);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaDimension);
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
