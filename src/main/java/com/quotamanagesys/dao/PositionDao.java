package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.business.IUser;
import com.bstek.bdf2.core.context.ContextHolder;
import com.bstek.bdf2.core.exception.NoneLoginException;
import com.bstek.bdf2.core.model.DefaultPosition;
import com.bstek.bdf2.core.model.GroupMember;
import com.bstek.bdf2.core.model.Role;
import com.bstek.bdf2.core.model.RoleMember;
import com.bstek.bdf2.core.model.UserDept;
import com.bstek.bdf2.core.model.UserPosition;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.bdf2.core.service.IGroupService;
import com.bstek.bdf2.core.service.IRoleService;
import com.bstek.bdf2.core.service.MemberType;
import com.bstek.bdf2.core.service.impl.DefaultGroupService;
import com.bstek.bdf2.core.service.impl.DefaultRoleService;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.bstek.dorado.data.provider.Criteria;
import com.bstek.dorado.data.provider.Page;
import com.quotamanagesys.model.PositionDept;

@Component
public class PositionDao extends HibernateDao {
	@Resource
	public PositionDeptDao positionDeptDao;
	@Resource
	public UserDao userDao;
	@Resource
	public UserDeptDao userDeptDao;
	
	@DataProvider
	public Collection<DefaultPosition> getAll() {
		return this.query("from " + DefaultPosition.class.getName());
	}

	@DataProvider
	public void getAll(Page<DefaultPosition> page, Criteria criteria) {
		DetachedCriteria dt = this.buildDetachedCriteria(criteria,
				DefaultPosition.class);
		this.pagingQuery(page, dt);
	}

	@DataProvider
	public DefaultPosition getPosition(String id) {
		List<DefaultPosition> positions = this
				.query("from DefaultPosition where id=" + "'" + id + "'");
		DefaultPosition position = null;
		if (positions.size() > 0) {
			position = positions.get(0);
		}
		return position;
	}

	@DataProvider
	public void getPositionByName(Page<DefaultPosition> page, String name)
			throws Exception {
		if (null != name) {
			String hqlString = "from " + DefaultPosition.class.getName()
					+ " where name like '%" + name + "%'";
			this.pagingQuery(page, hqlString, "select count(*)" + hqlString);
		} else {
			String hqlString = "from " + DefaultPosition.class.getName();
			this.pagingQuery(page, hqlString, "select count(*)" + hqlString);
		}
	}

	@DataProvider
	public Collection<DefaultPosition> getPositionsByUser(String userId) {
		List<DefaultPosition> positions = new ArrayList<DefaultPosition>();

		if (null != userId) {
			String hqlString = "from " + DefaultPosition.class.getName()
					+ " where id in (select positionId from "
					+ UserPosition.class.getName() + " where username='"
					+ userId + "')";
			positions = this.query(hqlString);
		}
		return positions;
	}
	
	//获取用户尚未关联的本部门的岗位
	@DataProvider
	public Collection<DefaultPosition> getPositionsNotLinkedByUser(String userId){
		if (userId!=null) {
			UserDept userDept=userDeptDao.getUserDeptByUser(userId);
			String deptId=userDept.getDeptId();
			String hqlString="from "+DefaultPosition.class.getName()
					+" where (id not in (select positionId from "
					+ UserPosition.class.getName() + " where username='"
					+ userId + "')) and (id in (select positionId from "
					+ PositionDept.class.getName() + " where deptId='"
					+ deptId + "'))";
			Collection<DefaultPosition> positions=this.query(hqlString);
			return positions;
		}else{
			return null;
		}
	}

	@DataResolver
	public void savePositions(Collection<DefaultPosition> positions,String deptId) throws Exception {
		Session session = getSessionFactory().openSession();
		try {
			IUser user = ContextHolder.getLoginUser();
			if (user == null) {
				throw new NoneLoginException("Please login first");
			}
			String companyId = user.getCompanyId();
			if (StringUtils.isNotEmpty(getFixedCompanyId())) {
				companyId = getFixedCompanyId();
			}
			for (DefaultPosition position : positions) {
				EntityState state = com.bstek.dorado.data.entity.EntityUtils.getState(position);
				if (state.equals(EntityState.NEW)) {
					UUID uuid=UUID.randomUUID();
					position.setId(uuid.toString());
					position.setCompanyId(companyId);
					position.setCreateDate(new Date());
					session.save(position);
					session.flush();
					
					PositionDept positionDept=new PositionDept();
					positionDept.setDeptId(deptId);
					positionDept.setPositionId(position.getId());
					positionDept=EntityUtils.toEntity(positionDept);
					EntityUtils.setState(positionDept, EntityState.NEW);
					System.out.print(positionDept.getDeptId());
					System.out.print(positionDept.getPositionId());
					System.out.print(positionDept.getId());
					positionDeptDao.savePositionDept(positionDept);	
				} else if (state.equals(EntityState.MODIFIED)) {
					session.update(position);
				} else if (state.equals(EntityState.DELETED)) {
					if (this.query("from "+GroupMember.class.getName()+" where positionId='"+position.getId()+"'").size()>0) {
						DefaultGroupService groupService=new DefaultGroupService();
						groupService.deleteGroupMemeber(position.getId(),MemberType.Position);
					}
					if (this.query("from "+RoleMember.class.getName()+" where positionId='"+position.getId()+"'").size()>0) {
						DefaultRoleService roleService=new DefaultRoleService();
						roleService.deleteRoleMemeber(position.getId(),MemberType.Position);
					}

					PositionDept positionDept=positionDeptDao.getPositionDeptByPosition(position.getId());
					positionDept=EntityUtils.toEntity(positionDept);
					EntityUtils.setState(positionDept, EntityState.DELETED);
					positionDeptDao.savePositionDept(positionDept);
					session.delete(position);
				}
			}
		} finally {
			session.flush();
			session.close();
		}
	}
}
