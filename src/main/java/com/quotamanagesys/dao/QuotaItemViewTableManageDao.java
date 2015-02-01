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
import com.quotamanagesys.model.QuotaItemViewTableManage;
import com.quotamanagesys.model.ShowColumn;
import com.quotamanagesys.model.ShowColumnGroup;

@Component
public class QuotaItemViewTableManageDao extends HibernateDao {
	
	@Resource
	ShowColumnDao showColumnDao;
	@Resource
	ShowColumnGroupDao showColumnGroupDao;

	@DataProvider
	public Collection<QuotaItemViewTableManage> getAll(){
		String hqlString="from "+QuotaItemViewTableManage.class.getName()+" order by year desc";
		Collection<QuotaItemViewTableManage> quotaItemViewTableManages=this.query(hqlString);
		return quotaItemViewTableManages;
	}
	
	@DataProvider
	public QuotaItemViewTableManage getItemViewTableManageByYear(int year){
		String hqlString="from "+QuotaItemViewTableManage.class.getName()+" where year="+year;
		List<QuotaItemViewTableManage> quotaItemViewTableManages=this.query(hqlString);
		if (quotaItemViewTableManages.size()>0) {
			return quotaItemViewTableManages.get(0);
		} else {
			return null;
		}
	}
	
	@DataProvider
	public QuotaItemViewTableManage getItemViewTableManage(String id){
		String hqlString="from "+QuotaItemViewTableManage.class.getName()+" where id='"+id+"'";
		List<QuotaItemViewTableManage> quotaItemViewTableManages=this.query(hqlString);
		if (quotaItemViewTableManages.size()>0) {
			return quotaItemViewTableManages.get(0);
		} else {
			return null;
		}
	}
	
	@Expose
	public void initQuotaItemViewTableManage(int year){
		if (getItemViewTableManageByYear(year)==null) {
			Session session=this.getSessionFactory().openSession();
			try {
				QuotaItemViewTableManage quotaItemViewTableManage=new QuotaItemViewTableManage();
				quotaItemViewTableManage.setShowName(year+"年指标信息总表");
				quotaItemViewTableManage.setTableName("quota_item_view_"+year);
				quotaItemViewTableManage.setYear(year);
				session.save(quotaItemViewTableManage);
				session.flush();
				session.clear();
			} catch (Exception e) {
				System.out.print(e.toString());
			}finally{
				session.flush();
				session.close();
			}
		} else {
			System.out.print("已经存在"+year+"年指标信息总表记录");
		}	
	}
	
	@DataProvider
	public void saveQuotaItemViewTableManages(Collection<QuotaItemViewTableManage> quotaItemViewTableManages){
		Collection<QuotaItemViewTableManage> adds=new ArrayList<QuotaItemViewTableManage>();
		Collection<QuotaItemViewTableManage> updates=new ArrayList<QuotaItemViewTableManage>();
		Collection<QuotaItemViewTableManage> deletes=new ArrayList<QuotaItemViewTableManage>();
		
		for (QuotaItemViewTableManage quotaItemViewTableManage : quotaItemViewTableManages) {
			EntityState state=EntityUtils.getState(quotaItemViewTableManage);
			if (state.equals(EntityState.NEW)) {
				adds.add(quotaItemViewTableManage);
			}else if (state.equals(EntityState.MODIFIED)) {
				updates.add(quotaItemViewTableManage);
			}else if (state.equals(EntityState.DELETED)) {
				deletes.add(quotaItemViewTableManage);
			}
		}
		if (adds.size()>0) {
			add(adds);
		}
		if (updates.size()>0) {
			update(updates);
		}
		if (deletes.size()>0) {
			delete(deletes);
		}
	}
	
	@Expose
	public void add(Collection<QuotaItemViewTableManage> quotaItemViewTableManages){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaItemViewTableManage quotaItemViewTableManage : quotaItemViewTableManages) {
				session.save(quotaItemViewTableManage);
				session.flush();
				session.clear();
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@Expose
	public void update(Collection<QuotaItemViewTableManage> quotaItemViewTableManages){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaItemViewTableManage quotaItemViewTableManage : quotaItemViewTableManages) {
				session.update(quotaItemViewTableManage);
				session.flush();
				session.clear();
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@Expose
	public void delete(Collection<QuotaItemViewTableManage> quotaItemViewTableManages){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaItemViewTableManage quotaItemViewTableManage : quotaItemViewTableManages) {
				String quotaItemViewTableManageId=quotaItemViewTableManage.getId();
				Collection<ShowColumn> showColumns=showColumnDao.getShowColumnsByQuotaItemViewTableManage(quotaItemViewTableManageId);
				Collection<ShowColumnGroup> showColumnGroups=showColumnGroupDao.getShowColumnGroupsByQuotaItemViewTableManage(quotaItemViewTableManageId);
				showColumnDao.delete(quotaItemViewTableManageId, showColumns);
				showColumnGroupDao.delete(quotaItemViewTableManageId, showColumnGroups);
				/*
				 *此处需加入删除数据表的代码
				 */
				session.delete(quotaItemViewTableManage);
				session.flush();
				session.clear();
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
