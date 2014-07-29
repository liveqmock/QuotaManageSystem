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
import com.quotamanagesys.model.QuotaDimensionOne;

@Component
public class QuotaDimensionOneDao extends HibernateDao {

	@DataProvider
	public Collection<QuotaDimensionOne> getAll(){
		String hqlString="from "+QuotaDimensionOne.class.getName();
		Collection<QuotaDimensionOne> quotaDimensionOnes=this.query(hqlString);
		return quotaDimensionOnes;
	}
	
	@DataProvider
	public QuotaDimensionOne getQuotaDimensionOne(String id){
		String hqlString="from "+QuotaDimensionOne.class.getName()+" where id='"+id+"'";
		List<QuotaDimensionOne> quotaDimensionOnes=this.query(hqlString);
		if (quotaDimensionOnes.size()>0) {
			return quotaDimensionOnes.get(0);
		}else {
			return null;
		}
	}
	
	@DataResolver
	public void saveQuotaDimensionOnes(Collection<QuotaDimensionOne> quotaDimensionOnes){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaDimensionOne quotaDimensionOne : quotaDimensionOnes) {
				EntityState state=EntityUtils.getState(quotaDimensionOne);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaDimensionOne);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.update(quotaDimensionOne);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaDimensionOne);
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
