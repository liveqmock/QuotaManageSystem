package com.quotamanagesys.dao;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.model.UserDept;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.annotation.Expose;

@Component
public class UserDeptDao extends HibernateDao {
	
	@Resource
	UserPositionDao userPositionDao;
	@Resource
	QuotaTypeViewMapDao quotaTypeViewMapDao;
	
	@DataProvider
	public Collection<UserDept> getAll() {
		String hqlString = "from " + UserDept.class.getName();
		Collection<UserDept> userDepts = this.query(hqlString);
		return userDepts;
	}

	@DataProvider
	public Collection<UserDept> getUserDeptsByDept(String deptId) {
		String hqlString = "from " + UserDept.class.getName()
				+ " where deptId='" + deptId + "'";
		Collection<UserDept> userDepts = this.query(hqlString);
		return userDepts;
	}

	@DataProvider
	public UserDept getUserDeptByUser(String username) {
		String hqlString = "from " + UserDept.class.getName()
				+ " where username='" + username + "'";
		List<UserDept> userDepts = this.query(hqlString);
		UserDept userDept = null;
		if (userDepts.size() > 0) {
			userDept = userDepts.get(0);
		}
		return userDept;
	}

	@DataProvider
	public UserDept getUserDept(String id) {
		String hqlString = "from " + UserDept.class.getName() + " where id='"
				+ id + "'";
		List<UserDept> userDepts = this.query(hqlString);
		UserDept userDept = null;
		if (userDepts.size() > 0) {
			userDept = userDepts.get(0);
		}
		return userDept;
	}
	
	@Expose
	public void addUserDept(String username,String deptId){
		Session session=this.getSessionFactory().openSession();
		try {
			UserDept userDept=new UserDept();
			//生成32位随机id，UserDept类id属性未关联id生成策略，故要手动生成随机id
			String id=UUID.randomUUID().toString();
			userDept.setId(id);
			userDept.setDeptId(deptId);
			userDept.setUsername(username);
			session.save(userDept);
			session.flush();
			
			//调整用户指标可视范围
			quotaTypeViewMapDao.initQuotaTypeViewMapsByUser(username);
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();	
		}
	}
	
	@Expose
	public void changeUserDept(String username,String deptId){
		deleteUserDeptByUser(username);
		userPositionDao.deleteUserPositionsByUser(username);
		Session session=this.getSessionFactory().openSession();
		try {
			addUserDept(username, deptId);
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}

	@DataResolver
	public void deleteUserDeptByUser(String username) {
		Session session = getSessionFactory().openSession();
		try {
			session.createQuery("delete " + UserDept.class.getName()+ " u where u.username = :username").setString("username", username).executeUpdate();
		} finally {
			session.flush();
			session.close();
		}
	}
}