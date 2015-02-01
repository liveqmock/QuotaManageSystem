package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.nfunk.jep.JEP;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.annotation.Expose;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.LightItem;
import com.quotamanagesys.model.QuotaFormulaResult;
import com.quotamanagesys.model.QuotaFormulaResultValue;

@Component
public class QuotaFormulaResultValueDao extends HibernateDao {
	
	@Resource
	LightItemDao lightItemDao;
	@Resource
	QuotaItemDao quotaItemDao;
	@Resource
	QuotaFormulaResultDao quotaFormulaResultDao;
	
	@DataProvider
	public Collection<QuotaFormulaResultValue> getAll(){
		String hqlString="from "+QuotaFormulaResultValue.class.getName();
		Collection<QuotaFormulaResultValue> quotaFormulaResultValues=this.query(hqlString);
		return quotaFormulaResultValues;
	}
	
	@DataProvider
	public QuotaFormulaResultValue getFormulaResultValue(String id){
		String hqlString="from "+QuotaFormulaResultValue.class.getName();
		List<QuotaFormulaResultValue> quotaFormulaResultValues=this.query(hqlString);
		if (quotaFormulaResultValues.size()>0) {
			return quotaFormulaResultValues.get(0);
		}else {
			return null;
		}
	}
	
	@DataProvider
	public Collection<QuotaFormulaResultValue> getQuotaFormulaResultValuesByQuotaItem(String quotaItemId){
		String hqlString="from "+QuotaFormulaResultValue.class.getName()+" where quotaItem.id='"+quotaItemId+"'";
		Collection<QuotaFormulaResultValue> quotaFormulaResultValues=this.query(hqlString);
		return quotaFormulaResultValues;
	}
	
	//获取指标对应的亮灯值
	@DataProvider
	public Collection<QuotaFormulaResultValue> getQuotaFormulaResultValuesOfLightTypeByQuotaItem(String quotaItemId){
		if (quotaItemId==null) {
			return null;
		} else {
			Session session = this.getSessionFactory().openSession();
			Collection<LightItem> lightItems=lightItemDao.getAll();
			ArrayList<String> quotaFormulaResultsOfLightType=new ArrayList<>();
			for (LightItem lightItem : lightItems) {
				quotaFormulaResultsOfLightType.add(lightItem.getQuotaFormulaResult().getId());
			}
			
			List<QuotaFormulaResultValue> quotaFormulaResultValues=session.createCriteria(QuotaFormulaResultValue.class)
					.createAlias("quotaFormulaResult", "qf")
					.createAlias("quotaItem", "qi")
					.add(Restrictions.and(Restrictions.in("qf.id",quotaFormulaResultsOfLightType),Restrictions.eq("qi.id", quotaItemId)))
					.addOrder(Order.asc("qf.name"))
					.list();
			session.close();
			return quotaFormulaResultValues;
		}
	}
	
	@DataResolver
	public void saveQuotaFormulaResultValues(Collection<QuotaFormulaResultValue> quotaFormulaResultValues){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaFormulaResultValue quotaFormulaResultValue : quotaFormulaResultValues) {
				EntityState state=EntityUtils.getState(quotaFormulaResultValue);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaFormulaResultValue);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.update(quotaFormulaResultValue);
				}else if (state.equals(EntityState.DELETED)) {
					quotaFormulaResultValue.setQuotaFormulaResult(null);
					quotaFormulaResultValue.setQuotaItem(null);
					session.delete(quotaFormulaResultValue);
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
	public void deleteQuotaFormulaResultValues(Collection<QuotaFormulaResultValue> quotaFormulaResultValues){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaFormulaResultValue quotaFormulaResultValue : quotaFormulaResultValues) {
				quotaFormulaResultValue.setQuotaFormulaResult(null);
				quotaFormulaResultValue.setQuotaItem(null);
				session.delete(quotaFormulaResultValue);
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
