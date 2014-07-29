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
import com.quotamanagesys.model.QuotaProfession;

@Component
public class QuotaProfessionDao extends HibernateDao {

	@DataProvider
	public Collection<QuotaProfession> getAll() {
		String hqlString = "from " + QuotaProfession.class.getName();
		Collection<QuotaProfession> quotaProfessions = this.query(hqlString);
		return quotaProfessions;
	}

	@DataProvider
	public QuotaProfession getQuotaProfession(String id) {
		String hqlString = "from " + QuotaProfession.class.getName()
				+ " where id='" + id + "'";
		List<QuotaProfession> quotaProfessions = this.query(hqlString);
		if (quotaProfessions.size() > 0) {
			return quotaProfessions.get(0);
		} else {
			return null;
		}
	}
	
	@DataResolver
	public void saveQuotaProfessions(Collection<QuotaProfession> quotaProfessions){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaProfession quotaProfession : quotaProfessions) {
				EntityState state=EntityUtils.getState(quotaProfession);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaProfession);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.update(quotaProfession);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaProfession);
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
