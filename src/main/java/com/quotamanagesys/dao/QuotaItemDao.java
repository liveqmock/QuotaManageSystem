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
import com.quotamanagesys.model.QuotaItem;

@Component
public class QuotaItemDao extends HibernateDao {

	@DataProvider
	public Collection<QuotaItem> getAll(){
		String hqlString="from "+QuotaItem.class.getName();
		Collection<QuotaItem> quotaItems=this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByManageDept(String manageDeptId){
		String hqlString="from "+QuotaItem.class.getName()+" where quotaItemCreator.quotaType.manageDept.id='"+manageDeptId+"'";
		Collection<QuotaItem> quotaItems=this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByYear(int year){
		String hqlString="from "+QuotaItem.class.getName()+" where year="+year;
		Collection<QuotaItem> quotaItems=this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByQuotaType(String quotaTypeId){
		String hqlString="from "+QuotaItem.class.getName()+" where quotaItemCreator.quotaType.id='"+quotaTypeId+"'";
		Collection<QuotaItem> quotaItems=this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByQuotaCover(String quotaCoverId){
		String hqlString="from "+QuotaItem.class.getName()+" where quotaItemCreator.quotaCover.id='"+quotaCoverId+"'";
		Collection<QuotaItem> quotaItems=this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByRate(String rate){
		String hqlString="from "+QuotaItem.class.getName()+" where quotaItemCreator.quotaType.rate='"+rate+"'";
		Collection<QuotaItem> quotaItems=this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByQuotaItemCreator(String quotaItemCreatorId){
		String hqlString="from "+QuotaItem.class.getName()+" where quotaItemCreator.id='"+quotaItemCreatorId+"'";
		Collection<QuotaItem> quotaItems=this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByDutyDept(String dutyDeptId){
		String hqlString="from "+QuotaItem.class.getName()+" where quotaItemCreator.quotaDutyDept.id='"+dutyDeptId+"'";
		Collection<QuotaItem> quotaItems=this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public QuotaItem getQuotaItem(String id){
		String hqlString="from "+QuotaItem.class.getName()+" where id='"+id+"'";
		List<QuotaItem> quotaItems=this.query(hqlString);
		if (quotaItems.size()>0) {
			return quotaItems.get(0);
		} else {
			return null;
		}
	}
	
	@DataResolver
	public void saveQuotaItems(Collection<QuotaItem> quotaItems){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaItem quotaItem : quotaItems) {
				EntityState state=EntityUtils.getState(quotaItem);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaItem);
				}else if (state.equals(EntityState.MODIFIED)) {
					QuotaItem thisQuotaItem=getQuotaItem(quotaItem.getId());
					thisQuotaItem.setTargetValue(quotaItem.getTargetValue());
					if (quotaItem.getFinishValue()!=null) {
						thisQuotaItem.setFinishValue(quotaItem.getFinishValue());
					}
					session.merge(thisQuotaItem);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaItem);
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
