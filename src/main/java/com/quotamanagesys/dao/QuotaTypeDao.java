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
import com.quotamanagesys.model.QuotaType;
import com.quotamanagesys.model.QuotaTypeRelation;

@Component
public class QuotaTypeDao extends HibernateDao {
	
	@DataProvider
	public Collection<QuotaType> getAll() {
		String hqlString = "from " + QuotaType.class.getName();
		Collection<QuotaType> quotaTypes = this.query(hqlString);
		return quotaTypes;
	}
	
	@DataProvider
	public QuotaType getQuotaType(String id){
		String hqlString = "from " + QuotaType.class.getName()+" where id='"+id+"'";
		List<QuotaType> quotaTypes = this.query(hqlString);
		if (quotaTypes.size()>0) {
			return quotaTypes.get(0);
		}else {
			return null;
		}
	}

	@DataProvider
	public Collection<QuotaType> getQuotaTypesByManageDept(String manageDeptId) {
		String hqlString="from "+QuotaType.class.getName()+" where manageDept.id='"+manageDeptId+"'";
		Collection<QuotaType> quotaTypes=this.query(hqlString);
		return quotaTypes;
	}

	@DataProvider
	public Collection<QuotaType> getQuotaTypesByProfession(String quotaProfessionId) {
		String hqlString="from "+QuotaType.class.getName()+" where quotaProfession.id='"+quotaProfessionId+"'";
		Collection<QuotaType> quotaTypes=this.query(hqlString);
		return quotaTypes;
	}
	
	@DataProvider
	public Collection<QuotaType> getQuotaTypesByQuotaLevel(String quotaLevelId) {
		String hqlString="from "+QuotaType.class.getName()+" where quotaLevel.id='"+quotaLevelId+"'";
		Collection<QuotaType> quotaTypes=this.query(hqlString);
		return quotaTypes;
	}

	@DataResolver
	public void saveQuotaTypes(Collection<QuotaType> quotaTypes) {
		Session session = this.getSessionFactory().openSession();
		try {
			for (QuotaType quotaType : quotaTypes) {
				EntityState state = EntityUtils.getState(quotaType);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaType);
				} else if (state.equals(EntityState.MODIFIED)) {
					session.merge(quotaType);
				} else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaType);
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
