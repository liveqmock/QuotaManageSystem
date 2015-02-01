package com.quotamanagesys.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.model.DefaultDept;
import com.bstek.bdf2.core.model.DefaultPosition;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.PositionDept;

@Component
public class PositionDeptDao extends HibernateDao {

	@DataProvider
	public Collection<PositionDept> getAll() {
		String hqlString = "from " + PositionDept.class.getName();
		return this.query(hqlString);
	}

	@DataProvider
	public Collection<PositionDept> getPositionDeptsByDept(String deptId) {
		String hqlString = "from " + PositionDept.class.getName()
				+ " where deptId='" + deptId + "'";
		return this.query(hqlString);
	}

	@DataProvider
	public Collection<DefaultPosition> getPositionsByDept(String deptId) {
		String hqlString = "from " + DefaultPosition.class.getName()
				+ " where id in (select positionId from "
				+ PositionDept.class.getName() + " where deptId='" + deptId
				+ "')";
		return this.query(hqlString);
	}

	@DataProvider
	public PositionDept getPositionDeptByPosition(String positionId){
		String hqlString="from "+PositionDept.class.getName()+" where positionId='"+positionId+"'";
		List<PositionDept> positionDepts=this.query(hqlString);
		PositionDept positionDept=null;
		if (positionDepts.size()>0) {
			positionDept=positionDepts.get(0);
		}
		return positionDept;
	}
	
	@DataProvider
	public DefaultDept getDeptPositionBelongTo(String positionId) {
		String hqlString = "from " + DefaultDept.class.getName()
				+ " where id in (select deptId from "
				+ PositionDept.class.getName() + " where positionId='"+positionId+"')";
		List<DefaultDept> depts=this.query(hqlString) ;
		DefaultDept deptBelongTo=null;
		if (depts.size()>0) {
			deptBelongTo=depts.get(0);
		}
		return deptBelongTo;
	}
	
	@DataResolver
	public void savePositionDepts(Collection<PositionDept> positionDepts){
		Session session=this.getSessionFactory().openSession();
		try {
			for (PositionDept positionDept : positionDepts) {
				EntityState state=EntityUtils.getState(positionDept);
				if(state.equals(EntityState.NEW)){
					session.save(positionDept);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.update(positionDept);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(positionDept);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@DataResolver
	public void savePositionDept(PositionDept positionDept){
		Session session=this.getSessionFactory().openSession();
		try {
			EntityState state=EntityUtils.getState(positionDept);
			if(state.equals(EntityState.NEW)){
				session.save(positionDept);
			}else if (state.equals(EntityState.MODIFIED)) {
				session.update(positionDept);
			}else if (state.equals(EntityState.DELETED)) {
				session.delete(positionDept);
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
}
