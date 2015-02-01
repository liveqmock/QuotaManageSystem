package com.quotamanagesys.dao;

import java.util.ArrayList;
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
import com.bstek.dorado.annotation.Expose;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.LightItem;

@Component
public class LightItemDao extends HibernateDao {
	
	@DataProvider
	public Collection<LightItem> getAll(){
		String hqlString="from "+LightItem.class.getName();
		Collection<LightItem> lightItems=this.query(hqlString);
		return lightItems;
	}
	
	@DataProvider
	public LightItem getLightItem(String id){
		String hqlString="from "+LightItem.class.getName()+" where id='"+id+"'";
		List<LightItem> lightItems=this.query(hqlString);
		if (lightItems.size()>0) {
			return lightItems.get(0);
		} else {
			return null;
		}
	}
	
	@DataResolver
	public void saveLightItems(Collection<LightItem> lightItems){
		Collection<LightItem> adds=new ArrayList<LightItem>();
		//Collection<LightItem> updates=new ArrayList<LightItem>();
		Collection<LightItem> deletes=new ArrayList<LightItem>();
		
		for (LightItem lightItem : lightItems) {
			EntityState state=EntityUtils.getState(lightItem);
			if (state.equals(EntityState.NEW)) {
				adds.add(lightItem);
			}else if (state.equals(EntityState.DELETED)) {
				deletes.add(lightItem);
			}
		}
		if (adds.size()>0) {
			add(adds);
		}
		if (deletes.size()>0) {
			delete(deletes);
		}
	}
	
	@Expose
	public void add(Collection<LightItem> lightItems){
		Session session=this.getSessionFactory().openSession();
		try {
			for (LightItem lightItem : lightItems) {
				session.save(lightItem);
				session.flush();
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@Expose
	public void delete(Collection<LightItem> lightItems){
		Session session=this.getSessionFactory().openSession();
		try {
			for (LightItem lightItem : lightItems) {
				LightItem thisLightItem=getLightItem(lightItem.getId());
				thisLightItem.setQuotaFormulaResult(null);
				session.delete(thisLightItem);
				session.flush();
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
