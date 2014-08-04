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
import com.quotamanagesys.model.QuotaFormulaResult;

@Component
public class QuotaFormulaResultDao extends HibernateDao {

	@DataProvider
	public Collection<QuotaFormulaResult> getAll(){
		String hqlString="from "+QuotaFormulaResult.class.getName();
		Collection<QuotaFormulaResult> quotaFormulaResults=this.query(hqlString);
		return quotaFormulaResults;
	}
	
	@DataProvider
	public QuotaFormulaResult getFormulaResult(String id){
		String hqlString="from "+QuotaFormulaResult.class.getName()+" where id='"+id+"'";
		List<QuotaFormulaResult> quotaFormulaResults=this.query(hqlString);
		if (quotaFormulaResults.size()>0) {
			return quotaFormulaResults.get(0);
		}else {
			return null;
		}
	}
	
	@DataResolver
	public void saveQuotaFormulaResults(Collection<QuotaFormulaResult> quotaFormulaResults){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaFormulaResult quotaFormulaResult : quotaFormulaResults) {
				EntityState state=EntityUtils.getState(quotaFormulaResult);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaFormulaResult);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.update(quotaFormulaResult);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaFormulaResult);
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
