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
import com.quotamanagesys.model.QuotaCover;

@Component
public class QuotaCoverDao extends HibernateDao {

	@DataProvider
	public Collection<QuotaCover> getAll(){
		String hqlString="from "+QuotaCover.class.getName();
		Collection<QuotaCover> quotaCovers=this.query(hqlString);
		return quotaCovers;
	}
	
	@DataProvider
	public QuotaCover getQuotaCover(String id){
		String hqlString="from "+QuotaCover.class.getName()+" where id='"+id+"'";
		List<QuotaCover> quotaCovers=this.query(hqlString);
		if (quotaCovers.size()>0) {
			return quotaCovers.get(0);
		}else {
			return null;
		}
	}
	
	@DataResolver
	public void saveQuotaCovers(Collection<QuotaCover> quotaCovers){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaCover quotaCover : quotaCovers) {
				EntityState state=EntityUtils.getState(quotaCover);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaCover);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.update(quotaCover);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaCover);
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
