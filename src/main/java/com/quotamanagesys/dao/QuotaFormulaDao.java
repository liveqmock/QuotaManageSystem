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
import com.quotamanagesys.model.QuotaFormula;
import com.quotamanagesys.model.QuotaFormulaResult;
import com.quotamanagesys.model.QuotaItemCreator;

@Component
public class QuotaFormulaDao extends HibernateDao {
	
	@Resource
	QuotaFormulaResultDao quotaFormulaResultDao;
	@Resource
	QuotaItemCreatorDao quotaItemCreatorDao;

	@DataProvider
	public Collection<QuotaFormula> getAll(){
		String hqlString="from "+QuotaFormula.class.getName();
		Collection<QuotaFormula> quotaFormulas=this.query(hqlString);
		return quotaFormulas;
	}
	
	@DataProvider
	public Collection<QuotaFormula> getQuotaFormulasByResult(String quotaFormulaResultId){
		String hqlString="from "+QuotaFormula.class.getName()+" where quotaFormulaResult.id='"+quotaFormulaResultId+"'";
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

	@DataProvider
	public Collection<QuotaFormula> getQuotaFormulasByQuotaItemCreator(String quotaItemCreatorId){
		QuotaItemCreator quotaItemCreator=quotaItemCreatorDao.getQuotaItemCreator(quotaItemCreatorId);
		return quotaItemCreator.getQuotaFormulas();
	}
	
	@DataResolver
	public void saveQuotaFormulas(Collection<QuotaFormula> quotaFormulas,String quotaFormulaResultId){
		Session session=this.getSessionFactory().openSession();
		QuotaFormulaResult quotaFormulaResult=quotaFormulaResultDao.getFormulaResult(quotaFormulaResultId);
		try {
			for (QuotaFormula quotaFormula : quotaFormulas) {
				EntityState state=EntityUtils.getState(quotaFormula);
				if (state.equals(EntityState.NEW)) {
					quotaFormula.setQuotaFormulaResult(quotaFormulaResult);
					session.merge(quotaFormula);
				}else if (state.equals(EntityState.MODIFIED)) {
					quotaFormula.setQuotaFormulaResult(quotaFormulaResult);
					session.merge(quotaFormula);
				}else if (state.equals(EntityState.DELETED)) {
					quotaFormula.setQuotaFormulaResult(null);
					session.delete(quotaFormula);
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
