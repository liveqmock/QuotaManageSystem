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
import com.quotamanagesys.model.QuotaLevel;

@Component
public class QuotaLevelDao extends HibernateDao {

	@DataProvider
	public Collection<QuotaLevel> getAll(){
		String hqlString="from "+QuotaLevel.class.getName();
		Collection<QuotaLevel> quotaLevels=this.query(hqlString);
		return quotaLevels;
	}
	
	@DataProvider
	public QuotaLevel getQuotaLevel(String id){
		String hqString="from "+QuotaLevel.class.getName()+" where id='"+id+"'";
		List<QuotaLevel> quotaLevels=this.query(hqString);
		if (quotaLevels.size()>0) {
			return quotaLevels.get(0);
		}else {
			return null;
		}
	}
	
	@DataResolver
	public void saveQuotaLevels(Collection<QuotaLevel> quotaLevels) {
		Session session = this.getSessionFactory().openSession();
		try {
			for (QuotaLevel quotaLevel : quotaLevels) {
				EntityState state = EntityUtils.getState(quotaLevel);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaLevel);
				} else if (state.equals(EntityState.MODIFIED)) {
					session.update(quotaLevel);
				} else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaLevel);
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
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
