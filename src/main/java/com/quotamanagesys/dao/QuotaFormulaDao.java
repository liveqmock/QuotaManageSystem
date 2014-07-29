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
import com.quotamanagesys.model.QuotaFormula;

@Component
public class QuotaFormulaDao extends HibernateDao {

	@DataProvider
	public Collection<QuotaFormula> getAll(){
		String hqlString="from "+QuotaFormula.class.getName();
		Collection<QuotaFormula> quotaFormulas=this.query(hqlString);
		return quotaFormulas;
	}
	
	@DataProvider
	public Collection<QuotaFormula> getQuotaFormulasByType(String type){
		String hqlString="from "+QuotaFormula.class.getName()+" where type='"+type+"'";
		Collection<QuotaFormula> quotaFormulas=this.query(hqlString);
		return quotaFormulas;
	}
	
	@DataProvider
	public QuotaFormula getQuotaFormula(String id){
		String hqlString="from "+QuotaFormula.class.getName()+" where id='"+id+"'";
		List<QuotaFormula> quotaFormulas=this.query(hqlString);
		if (quotaFormulas.size()>0) {
			return quotaFormulas.get(0);
		}else {
			return null;
		}
	}
	
	@DataResolver
	public void saveQuotaFormulas(Collection<QuotaFormula> quotaFormulas){
		Session session=this.getSessionFactory().openSession();
		for (QuotaFormula quotaFormula : quotaFormulas) {
			try {
				EntityState state=EntityUtils.getState(quotaFormula);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaFormula);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.update(quotaFormula);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaFormula);
				}
			} catch (Exception e) {
				System.out.print(e.toString());
			}finally{
				session.flush();
				session.close();
			}
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
