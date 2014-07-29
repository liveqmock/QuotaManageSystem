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
import com.quotamanagesys.model.QuotaDimensionOne;
import com.quotamanagesys.model.QuotaDimensionTwo;

@Component
public class QuotaDimensionTwoDao extends HibernateDao {

	@Resource
	QuotaDimensionOneDao quotaDimensionOneDao;
	
	@DataProvider
	public Collection<QuotaDimensionTwo> getAll() {
		String hqlString = "from " + QuotaDimensionTwo.class.getName();
		Collection<QuotaDimensionTwo> quotaDimensionTwos = this
				.query(hqlString);
		return quotaDimensionTwos;
	}

	@DataProvider
	public QuotaDimensionTwo getQuotaDimensionTwo(String id) {
		String hqlString = "from " + QuotaDimensionTwo.class.getName()
				+ " where id='" + id + "'";
		List<QuotaDimensionTwo> quotaDimensionTwos = this.query(hqlString);
		if (quotaDimensionTwos.size() > 0) {
			return quotaDimensionTwos.get(0);
		} else {
			return null;
		}
	}

	@DataProvider
	public Collection<QuotaDimensionTwo> getQuotaDimensionTwosByDimensionOne(
			String quotaDimensionOneId) {
		String hqlString= "from " + QuotaDimensionTwo.class.getName()
				+ " where quotaDimensionOne.id='" + quotaDimensionOneId + "'";
		Collection<QuotaDimensionTwo> quotaDimensionTwos=this.query(hqlString);
		return quotaDimensionTwos;
	}

	@DataResolver
	public void saveQuotaDimensionTwos(Collection<QuotaDimensionTwo> quotaDimensionTwos,String quotaDimensionOneId){
		Session session=this.getSessionFactory().openSession();
		QuotaDimensionOne quotaDimensionOne=quotaDimensionOneDao.getQuotaDimensionOne(quotaDimensionOneId);
		try {
			for (QuotaDimensionTwo quotaDimensionTwo : quotaDimensionTwos) {
				EntityState state=EntityUtils.getState(quotaDimensionTwo);
				if (state.equals(EntityState.NEW)) {
					quotaDimensionTwo.setQuotaDimensionOne(quotaDimensionOne);
					session.save(quotaDimensionTwo);
				}else if (state.equals(EntityState.MODIFIED)) {
					quotaDimensionTwo.setQuotaDimensionOne(quotaDimensionOne);
					session.update(quotaDimensionTwo);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaDimensionTwo);
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
