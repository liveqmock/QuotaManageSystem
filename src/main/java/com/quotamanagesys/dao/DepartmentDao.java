package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.business.IDept;
import com.bstek.bdf2.core.business.IUser;
import com.bstek.bdf2.core.context.ContextHolder;
import com.bstek.bdf2.core.model.DefaultDept;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.quotamanagesys.model.QuotaCover;

@Component
public class DepartmentDao extends HibernateDao {
	
	@Resource
	QuotaCoverDao quotaCoverDao;

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
	
	//建立在部门名称不重复的基础之上
	@DataProvider
	public DefaultDept getDeptByName(String deptName){
		String hqlString="from "+DefaultDept.class.getName()+" where name='"+deptName+"'";
		List<DefaultDept> depts=this.query(hqlString);
		if (depts.size()>0) {
			return depts.get(0);
		}else {
			return null;
		}
	}
	
	@DataProvider
	public Collection<DefaultDept> getDutyDeptsByQuotaCover(String quotaCoverId){
		QuotaCover quotaCover=quotaCoverDao.getQuotaCover(quotaCoverId);
		if (quotaCover!=null) {
			return quotaCover.getDutyDepts();
		}else {
			return null;
		}
	}
	
	@DataProvider
	public Collection<DefaultDept> getDeptsByLoginUser(){
		IUser loginuser = ContextHolder.getLoginUser();
		if (loginuser.isAdministrator()) {
			return getAll();
		}else {
			List<IDept> idepts=loginuser.getDepts();
			Collection<DefaultDept> depts=new ArrayList<DefaultDept>();
			for (IDept iDept : idepts) {
				depts.add(getDept(iDept.getId()));
			}
			return depts;
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
