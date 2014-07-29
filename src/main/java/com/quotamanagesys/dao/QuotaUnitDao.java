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
import com.quotamanagesys.model.QuotaUnit;

@Component
public class QuotaUnitDao extends HibernateDao {

	@DataProvider
	public Collection<QuotaUnit> getAll(){
		String hqlString="from "+QuotaUnit.class.getName();
		Collection<QuotaUnit> quotaUnits=this.query(hqlString);
		return quotaUnits;
	}
	
	@DataProvider
	public QuotaUnit getQuotaUnit(String id){
		String hqlString="from "+QuotaUnit.class.getName()+" where id='"+id+"'";
		List<QuotaUnit> quotaUnits=this.query(hqlString);
		if (quotaUnits.size()>0) {
			return quotaUnits.get(0);
		}else {
			return null;
		}
	}
	
	@DataResolver
	public void saveQuotaUnits(Collection<QuotaUnit> quotaUnits){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaUnit quotaUnit : quotaUnits) {
				EntityState state=EntityUtils.getState(quotaUnit);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaUnit);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.update(quotaUnit);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaUnit);
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
