package com.quotamanagesys.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.model.DefaultDept;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.QuotaCover;

@Component
public class QuotaCoverDao extends HibernateDao {

	@DataProvider
	public Collection<QuotaCover> getAll(){
		String hqlString="from "+QuotaCover.class.getName()+" order by sort asc";
		Collection<QuotaCover> quotaCovers=this.query(hqlString);
		return quotaCovers;
	}
	
	@DataProvider
	public QuotaCover getQuotaCover(String id){
		String hqlString="from "+QuotaCover.class.getName()+" where id='"+id+"'";
		List<QuotaCover> quotaCovers=this.query(hqlString);
		if (quotaCovers.size()>0) {
			return quotaCovers.get(0);
		}else {
			return null;
		}
	}
	
	@DataProvider
	public QuotaCover getFatherQuotaCover(String id){
		String hqlString="from "+QuotaCover.class.getName()+" where id='"+id+"'";
		List<QuotaCover> quotaCovers=this.query(hqlString);
		if (quotaCovers.size()>0) {
			return quotaCovers.get(0).getFatherQuotaCover();
		}else {
			return null;
		}
	}
	
	@DataProvider
	public List<QuotaCover> getTopQuotaCovers(){
		String hqlString="from "+QuotaCover.class.getName()+" where fatherQuotaCover=null  order by sort asc";
		List<QuotaCover> quotaCovers=this.query(hqlString);
		return quotaCovers;
	}
	
	//获取下一级口径
	@DataProvider
	public Collection<QuotaCover> getQuotaCoversByFatherCover(String fatherQuotaCoverId){
		String hqString="from "+QuotaCover.class.getName()+" where fatherQuotaCover.id='"+fatherQuotaCoverId+"'  order by sort asc";
		Collection<QuotaCover> quotaCovers=this.query(hqString);
		return quotaCovers;
	}
	
	//获取子口径树
	@DataProvider
	public Collection<QuotaCover> getQuotaCoversTreeByFatherCover(String fatherQuotaCoverId,Collection<QuotaCover> quotaCoversTree){
		Collection<QuotaCover> childrenQuotaCovers=getQuotaCoversByFatherCover(fatherQuotaCoverId);
		quotaCoversTree.addAll(childrenQuotaCovers);
		for (QuotaCover child : childrenQuotaCovers) {
			getQuotaCoversTreeByFatherCover(child.getId(), quotaCoversTree);
		}
		return quotaCoversTree;
	}
	
	@DataResolver
	public void saveQuotaCovers(Collection<QuotaCover> quotaCovers){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaCover quotaCover : quotaCovers) {
				EntityState state=EntityUtils.getState(quotaCover);
				if (state.equals(EntityState.NEW)) {
					QuotaCover fatherQuotaCover=quotaCover.getFatherQuotaCover();
					quotaCover.setFatherQuotaCover(getQuotaCover(fatherQuotaCover.getId()));
					
					session.merge(quotaCover);
					session.flush();
					session.clear();
				}else if (state.equals(EntityState.MODIFIED)) {
					QuotaCover thisQuotaCover=getQuotaCover(quotaCover.getId());
					thisQuotaCover.setName(quotaCover.getName());
					thisQuotaCover.setSort(quotaCover.getSort());
					if ((quotaCover.getFatherQuotaCover()).equals(null)) {
						thisQuotaCover.setFatherQuotaCover(null);
					}else {
						thisQuotaCover.setFatherQuotaCover(getQuotaCover(quotaCover.getFatherQuotaCover().getId()));
					}
					
					session.merge(thisQuotaCover);
					session.flush();
					session.clear();
				}else if (state.equals(EntityState.DELETED)) {
					Collection<QuotaCover> childrenQuotaCovers=getQuotaCoversByFatherCover(quotaCover.getId());
					for (QuotaCover child : childrenQuotaCovers) {
						child.setFatherQuotaCover(null);
						session.merge(child);
						session.flush();
						session.clear();
					}
					
					quotaCover.setDutyDepts(null);
					quotaCover.setFatherQuotaCover(null);
					session.delete(quotaCover);
					session.flush();
					session.clear();
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
	public void saveDutyDepts(Collection<DefaultDept> dutyDepts,String quotaCoverId){
		QuotaCover quotaCover=getQuotaCover(quotaCoverId);
		Set<DefaultDept> thisDutyDepts=quotaCover.getDutyDepts();
		
		Session session=this.getSessionFactory().openSession();
		try {
			for (DefaultDept dutyDept : dutyDepts) {
				EntityState state=EntityUtils.getState(dutyDept);
				if (state.equals(EntityState.NEW)) {
					thisDutyDepts.add(dutyDept);
				}else if (state.equals(EntityState.DELETED)) {
					for (DefaultDept thisDutyDept : thisDutyDepts) {
						if ((thisDutyDept.getId()).equals(dutyDept.getId())) {
							thisDutyDepts.remove(thisDutyDept);
							break;
						}
					}
				}
			}
			quotaCover.setDutyDepts(thisDutyDepts);
			session.merge(quotaCover);
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
