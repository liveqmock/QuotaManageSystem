package com.quotamanagesys.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.model.DefaultDept;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;

@Component
public class DepartmentDao extends HibernateDao {

	@DataProvider
	public Collection<DefaultDept> getAll(){
		String hqlString="from "+DefaultDept.class.getName();
		Collection<DefaultDept> depts=this.query(hqlString);
		return depts;
	}
	
	@DataProvider
	public DefaultDept getDept(String id){
		String hqlString="from "+DefaultDept.class.getName()+" where id='"+id+"'";
		List<DefaultDept> depts=this.query(hqlString);
		if (depts.size()>0) {
			return depts.get(0);
		}else {
			return null;
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
