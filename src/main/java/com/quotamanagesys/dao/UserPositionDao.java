package com.quotamanagesys.dao;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.model.DefaultPosition;
import com.bstek.bdf2.core.model.UserPosition;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.annotation.Expose;

@Component
public class UserPositionDao extends HibernateDao {
	@DataProvider
	public Collection<UserPosition> getAll() {
		String hqlString = "from " + UserPosition.class.getName();
		Collection<UserPosition> userPositions = this.query(hqlString);
		return userPositions;
	}

	@DataProvider
	public Collection<UserPosition> getUserPositionsByPosition(String positionId) {
		String hqlString = "from " + UserPosition.class.getName()
				+ " where positionId='" + positionId + "'";
		Collection<UserPosition> userPositions = this.query(hqlString);
		return userPositions;
	}

	@DataProvider
	public Collection<UserPosition> getUserPositionsByUser(String username) {
		String hqlString = "from " + UserPosition.class.getName()
				+ " where username='" + username + "'";
		List<UserPosition> userPositions = this.query(hqlString);
		return userPositions;
	}

	@DataProvider
	public UserPosition getUserPosition(String id) {
		String hqlString = "from " + UserPosition.class.getName() + " where id='" + id + "'";
		List<UserPosition> userPositions = this.query(hqlString);
		UserPosition userPosition = null;
		if (userPositions.size() > 0) {
			userPosition = userPositions.get(0);
		}
		return userPosition;
	}
	
	@Expose
	public void addUserPositions(String username,Collection<DefaultPosition> positions){
		Session session=this.getSessionFactory().openSession();
		try {
			for (DefaultPosition position : positions) {
				UserPosition userPosition=new UserPosition();
				//生成32位随机id，UserDept类id属性未关联id生成策略，故要手动生成随机id
				String id=UUID.randomUUID().toString();
				userPosition.setId(id);
				userPosition.setPositionId(position.getId());
				userPosition.setUsername(username);
				session.save(userPosition);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}

	@DataResolver
	public void deleteUserPositionsByUser(String username) {
		Session session = getSessionFactory().openSession();
		try {
			session.createQuery("delete " + UserPosition.class.getName() + " u where u.username = :username").setString("username", username).executeUpdate();
		} finally {
			session.flush();
			session.close();
		}
	}
}
