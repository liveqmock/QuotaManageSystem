package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
import com.quotamanagesys.model.QuotaType;
import com.quotamanagesys.model.QuotaTypeRelation;

@Component
public class QuotaTypeRelationDao extends HibernateDao {

	@Resource
	QuotaTypeDao quotaTypeDao;
	
	@DataProvider
	public Collection<QuotaTypeRelation> getAll(){
		String hqlString="from "+QuotaTypeRelation.class.getName();
		Collection<QuotaTypeRelation> quotaTypeRelations=this.query(hqlString);
		return quotaTypeRelations;
	}
	
	@DataProvider
	public QuotaTypeRelation getQuotaTypeRelation(String id){
		String hqlString="from "+QuotaTypeRelation.class.getName()+" where id='"+id+"'";
		List<QuotaTypeRelation> quotaTypeRelations=this.query(hqlString);
		if (quotaTypeRelations.size()>0) {
			return quotaTypeRelations.get(0);
		}else {
			return null;
		}
	}
	
	@DataProvider
	public Collection<QuotaType> getSubQuotaTypes(String quotaTypeRelationId){
		if (quotaTypeRelationId!=null) {
			QuotaTypeRelation quotaTypeRelation=getQuotaTypeRelation(quotaTypeRelationId);
			return quotaTypeRelation.getSubQuotaTypes();
		}else {
			return null;
		}
	}
	
	@DataProvider
	public QuotaTypeRelation getQuotaTypeRelationByQuotaType(String quotaTypeId){
		String hqlString="from "+QuotaTypeRelation.class.getName()+" where mainQuotaType.id='"+quotaTypeId+"'";
		List<QuotaTypeRelation> quotaTypeRelations=this.query(hqlString);
		if (quotaTypeRelations.size()>0) {
			return quotaTypeRelations.get(0);
		}else {
			return null;
		}
	}
	
	@DataProvider
	public Collection<QuotaTypeRelation> getQuotaTypeRelationsByManageDept(String manageDeptId){
		Collection<QuotaType> quotaTypes=quotaTypeDao.getQuotaTypesByManageDept(manageDeptId);
		createQuotaTypeRelations(quotaTypes);
		Collection<QuotaTypeRelation> quotaTypeRelations=new ArrayList<QuotaTypeRelation>();
		for (QuotaType quotaType : quotaTypes) {
			quotaTypeRelations.add(getQuotaTypeRelationByQuotaType(quotaType.getId()));
		}
		return quotaTypeRelations;
	}
	
	@DataProvider
	public Collection<QuotaTypeRelation> getQuotaTypeRelationsByProfession(String quotaProfessionId){
		Collection<QuotaType> quotaTypes=quotaTypeDao.getQuotaTypesByProfession(quotaProfessionId);
		createQuotaTypeRelations(quotaTypes);
		Collection<QuotaTypeRelation> quotaTypeRelations=new ArrayList<QuotaTypeRelation>();
		for (QuotaType quotaType : quotaTypes) {
			quotaTypeRelations.add(getQuotaTypeRelationByQuotaType(quotaType.getId()));
		}
		return quotaTypeRelations;
	}
	
	@DataResolver
	public void createQuotaTypeRelation(QuotaType quotaType){
		String hqlString="from "+QuotaTypeRelation.class.getName()+" where mainQuotaType.id='"+quotaType.getId()+"'";
		Collection<QuotaTypeRelation> quotaTypeRelations=this.query(hqlString);
		if (quotaTypeRelations.size()==0) {
			Session session = this.getSessionFactory().openSession();
			QuotaTypeRelation quotaTypeRelation=new QuotaTypeRelation();
			quotaTypeRelation.setMainQuotaType(quotaType);
			try {
				session.save(quotaTypeRelation);
			} catch (Exception e) {
				System.out.print(e.toString());
			}finally{
				session.flush();
				session.close();
			}
		}
	}
	
	@DataResolver
	public void createQuotaTypeRelations(Collection<QuotaType> quotaTypes){
		for (QuotaType quotaType : quotaTypes) {
			createQuotaTypeRelation(quotaType);
		}
	}
	
	@DataResolver
	public void saveQuotaTypeRelations(Collection<QuotaTypeRelation> quotaTypeRelations) {
		Session session = this.getSessionFactory().openSession();
		try {
			for (QuotaTypeRelation quotaTypeRelation : quotaTypeRelations) {
				EntityState state = EntityUtils.getState(quotaTypeRelation);
				if (state.equals(EntityState.NEW)) {	
					session.save(quotaTypeRelation);
				} else if (state.equals(EntityState.MODIFIED)) {
					session.merge(quotaTypeRelation);
				} else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaTypeRelation);
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
	public void saveQuotaTypeRelation(QuotaTypeRelation quotaTypeRelation){
		Session session = this.getSessionFactory().openSession();
		try {
			EntityState state = EntityUtils.getState(quotaTypeRelation);
			if (state.equals(EntityState.NEW)) {	
				session.save(quotaTypeRelation);
			} else if (state.equals(EntityState.MODIFIED)) {
				session.merge(quotaTypeRelation);
			} else if (state.equals(EntityState.DELETED)) {
				session.delete(quotaTypeRelation);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@DataResolver
	public void saveSubQuotaTypes(Collection<QuotaType> subQuotaTypes,String quotaTypeRelationId){
		Session session=this.getSessionFactory().openSession();
		QuotaTypeRelation quotaTypeRelation=getQuotaTypeRelation(quotaTypeRelationId);
		Set<QuotaType> oldSubQuotaTypes=quotaTypeRelation.getSubQuotaTypes();
		try {
			for (QuotaType quotaType : subQuotaTypes) {
				EntityState state=EntityUtils.getState(quotaType);
				if (state.equals(EntityState.NEW)) {
					QuotaType tempQuotaType=quotaTypeDao.getQuotaType(quotaType.getId());
					oldSubQuotaTypes.add(tempQuotaType);
				}else if (state.equals(EntityState.DELETED)) {
					for (QuotaType quotaType2 : oldSubQuotaTypes) {
						if (quotaType2.getId().equals(quotaType.getId())) {
							oldSubQuotaTypes.remove(quotaType2);
							break;
						}
					}
				}
			}
			quotaTypeRelation.setSubQuotaTypes(oldSubQuotaTypes);
			session.merge(quotaTypeRelation);
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
