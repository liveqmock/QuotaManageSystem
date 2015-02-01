package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.business.IDept;
import com.bstek.bdf2.core.model.DefaultDept;
import com.bstek.bdf2.core.model.DefaultUser;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.annotation.Expose;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaType;
import com.quotamanagesys.model.QuotaTypeViewMap;

@Component
public class QuotaTypeViewMapDao extends HibernateDao {
	
	@Resource
	DepartmentDao departmentDao;
	@Resource
	QuotaTypeDao quotaTypeDao;
	@Resource
	UserDao userDao;
	

	@DataProvider
	public Collection<QuotaTypeViewMap> getAll(){
		String hqlString="from "+QuotaTypeViewMap.class.getName();
		Collection<QuotaTypeViewMap> quotaTypeViewMaps=this.query(hqlString);
		return quotaTypeViewMaps;
	}
	
	@DataProvider
	public QuotaTypeViewMap getQuotaTypeViewMapByUser(String userId){
		String hqlString="from "+QuotaTypeViewMap.class.getName()+" where user.username='"+userId+"'";
		List<QuotaTypeViewMap> quotaTypeViewMaps=this.query(hqlString);
		if (quotaTypeViewMaps.size()>0) {
			return quotaTypeViewMaps.get(0);
		} else {
			return null;
		}
	}
	
	@DataProvider
	public QuotaTypeViewMap getQuotaTypeViewMap(String id){
		String hqlString="from "+QuotaTypeViewMap.class.getName()+" where id='"+id+"'";
		List<QuotaTypeViewMap> quotaTypeViewMaps=this.query(hqlString);
		if (quotaTypeViewMaps.size()>0) {
			return quotaTypeViewMaps.get(0);
		} else {
			return null;
		}
	}
	
	@DataProvider
	public Collection<QuotaType> getCanViewQuotaTypes(String userId){
		QuotaTypeViewMap quotaTypeViewMap=getQuotaTypeViewMapByUser(userId);
		if (quotaTypeViewMap!=null) {
			return quotaTypeViewMap.getCanViewQuotaTypes();
		} else {
			return null;
		}
	}
	
	@DataProvider
	public Collection<QuotaType> getDefaultViewQuotaTypes(String userId){
		QuotaTypeViewMap quotaTypeViewMap=getQuotaTypeViewMapByUser(userId);
		if (quotaTypeViewMap!=null) {
			return quotaTypeViewMap.getDefaultViewQuotaTypes();
		} else {
			return null;
		}
	}
	
	@DataProvider
	public Collection<QuotaType> getQuotaTypeInUsedNotYetMapByUser(String userId){
		Session session = this.getSessionFactory().openSession();
		QuotaTypeViewMap quotaTypeViewMap=getQuotaTypeViewMapByUser(userId);
		if (quotaTypeViewMap!=null) {
			String quotaTypeViewMapId=quotaTypeViewMap.getId();
			Collection<QuotaType> canViewQuotaTypes=quotaTypeViewMap.getCanViewQuotaTypes();
			List ids=new ArrayList();
			for (QuotaType quotaType : canViewQuotaTypes) {
				ids.add(quotaType.getId());
			}
			if (ids.size()>0) {
				String hqlString="from "+QuotaType.class.getName()+" where inUsed=true and id not in (:ids)";
				Query query = session.createQuery(hqlString);  
			    query.setParameterList("ids", ids);
			    List<QuotaType> quotaTypes=query.list();
			    session.close();
			    return quotaTypes;
			} else {
				session.close();
				return null;
			}
		}else {
			session.close();
			return null;
		}
	}
	
	@DataProvider
	public Collection<QuotaType> getCanViewQuotaTypesNotYetMapByUser(String userId){
		Session session = this.getSessionFactory().openSession();
		QuotaTypeViewMap quotaTypeViewMap=getQuotaTypeViewMapByUser(userId);
		if (quotaTypeViewMap!=null) {
			String quotaTypeViewMapId=quotaTypeViewMap.getId();
			Collection<QuotaType> canViewQuotaTypes=quotaTypeViewMap.getCanViewQuotaTypes();
			Collection<QuotaType> defaultQuotaTypes=quotaTypeViewMap.getDefaultViewQuotaTypes();
			canViewQuotaTypes.removeAll(defaultQuotaTypes);
			List ids=new ArrayList();
			for (QuotaType quotaType : canViewQuotaTypes) {
				ids.add(quotaType.getId());
			}
			if (ids.size()>0) {
				String hqlString="from "+QuotaType.class.getName()+" where inUsed=true and id in (:ids)";
				Query query = session.createQuery(hqlString);  
			    query.setParameterList("ids", ids);
			    List<QuotaType> quotaTypes=query.list();
			    session.close();
			    return quotaTypes;
			} else {
				session.close();
				return null;
			}
			
		}else {
			session.close();
			return null;
		}
	}
	
	@Expose
	public void initQuotaTypeViewMaps(){
		Collection<DefaultDept> depts=departmentDao.getAll();
		for (DefaultDept dept : depts) {
			initQuotaTypeViewMapsByDept(dept.getId());
		}
	}
	
	@Expose
	public void initQuotaTypeViewMapsByDept(String deptId){
		Session session = this.getSessionFactory().openSession();
		Collection<QuotaType> quotaTypes=quotaTypeDao.getQuotaTypesInUsedByManageDept(deptId);
		Set<QuotaType> quotaTypesSet=new HashSet<QuotaType>();
		quotaTypesSet.addAll(quotaTypes);
		
		Collection<DefaultUser> users=userDao.getUsersByDept(deptId);
		
		try {
			for (DefaultUser user : users) {
				initQuotaTypeViewMapsByUser(user.getUsername());
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@Expose
	public void initQuotaTypeViewMapsByUser(String userId){
		Session session = this.getSessionFactory().openSession();
		DefaultUser user=userDao.getUser(userId);
		if (user!=null) {
			DefaultDept dept=userDao.getUserDept(userId);
			if (dept!=null) {
				String deptId=dept.getId();
				Collection<QuotaType> quotaTypes=quotaTypeDao.getQuotaTypesInUsedByManageDept(deptId);
				Set<QuotaType> quotaTypesSet=new HashSet<QuotaType>();
				quotaTypesSet.addAll(quotaTypes);
				
				try {
					QuotaTypeViewMap preQuotaTypeViewMap=getQuotaTypeViewMapByUser(user.getUsername());
					if (preQuotaTypeViewMap!=null) {
						preQuotaTypeViewMap.setUser(null);
						preQuotaTypeViewMap.setCanViewQuotaTypes(null);
						preQuotaTypeViewMap.setDefaultViewQuotaTypes(null);
						session.delete(preQuotaTypeViewMap);
						session.flush();
						session.clear();
					}
					QuotaTypeViewMap quotaTypeViewMap=new QuotaTypeViewMap();
					quotaTypeViewMap.setUser(user);
					quotaTypeViewMap.setCanViewQuotaTypes(quotaTypesSet);
					quotaTypeViewMap.setDefaultViewQuotaTypes(quotaTypesSet);
					session.save(quotaTypeViewMap);
					session.flush();
					session.clear();
				} catch (Exception e) {
					System.out.print(e.toString());
				}finally{
					session.flush();
					session.close();
				}
			}else {
				System.out.print("用户未关联部门，无法初始化 ");
			}
			
		}else {
			System.out.print("用户为空，无法初始化指标可视关系");
		}
		
	}
	
	@DataResolver
	public void saveQuotaTypeViewMaps(Collection<QuotaTypeViewMap> quotaTypeViewMaps){
		Collection<QuotaTypeViewMap> adds=new ArrayList<QuotaTypeViewMap>();
		Collection<QuotaTypeViewMap> updates=new ArrayList<QuotaTypeViewMap>();
		Collection<QuotaTypeViewMap> deletes=new ArrayList<QuotaTypeViewMap>();
		
		for (QuotaTypeViewMap quotaTypeViewMap : quotaTypeViewMaps) {
			EntityState state=EntityUtils.getState(quotaTypeViewMap);
			if (state.equals(EntityState.NEW)) {
				adds.add(quotaTypeViewMap);
			}else if (state.equals(EntityState.MODIFIED)) {
				updates.add(quotaTypeViewMap);
			}else if (state.equals(EntityState.DELETED)) {
				deletes.add(quotaTypeViewMap);
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
	public void add(Collection<QuotaTypeViewMap> quotaTypeViewMaps){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaTypeViewMap quotaTypeViewMap : quotaTypeViewMaps) {
				session.save(quotaTypeViewMap);
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
	public void update(Collection<QuotaTypeViewMap> quotaTypeViewMaps){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaTypeViewMap quotaTypeViewMap : quotaTypeViewMaps) {
				session.update(quotaTypeViewMap);
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
	public void delete(Collection<QuotaTypeViewMap> quotaTypeViewMaps){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaTypeViewMap quotaTypeViewMap : quotaTypeViewMaps) {
				QuotaTypeViewMap thisQuotaTypeViewMap=getQuotaTypeViewMap(quotaTypeViewMap.getId());
				thisQuotaTypeViewMap.setUser(null);
				thisQuotaTypeViewMap.setCanViewQuotaTypes(null);
				thisQuotaTypeViewMap.setDefaultViewQuotaTypes(null);
				session.delete(thisQuotaTypeViewMap);
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
	public void saveCanViewQuotaTypes(String userId,Collection<QuotaType> quotaTypes){
		Session session=this.getSessionFactory().openSession();
		QuotaTypeViewMap quotaTypeViewMap=getQuotaTypeViewMapByUser(userId);
		Set<QuotaType> thisCanViewQuotaTypes=quotaTypeViewMap.getCanViewQuotaTypes();
		Set<QuotaType> thisDefaultViewQuotaTypes=quotaTypeViewMap.getDefaultViewQuotaTypes();

		try {
			for (QuotaType quotaType : quotaTypes) {
				EntityState state=EntityUtils.getState(quotaType);
				if (state.equals(EntityState.NEW)) {
					QuotaType add=quotaTypeDao.getQuotaType(quotaType.getId());
					thisCanViewQuotaTypes.add(add);
				}else if (state.equals(EntityState.DELETED)) {
					for (QuotaType thisCanViewQuotaType : thisCanViewQuotaTypes) {
						if ((thisCanViewQuotaType.getId()).equals(quotaType.getId())) {
							thisCanViewQuotaTypes.remove(thisCanViewQuotaType);
							for (QuotaType thisDefaultViewQuotaType : thisDefaultViewQuotaTypes) {
								if ((thisDefaultViewQuotaType.getId()).equals(quotaType.getId())) {
									thisDefaultViewQuotaTypes.remove(thisDefaultViewQuotaType);
									break;
								}
							}
							break;
						}
					}
				}
			}
			quotaTypeViewMap.setCanViewQuotaTypes(thisCanViewQuotaTypes);
			quotaTypeViewMap.setDefaultViewQuotaTypes(thisDefaultViewQuotaTypes);
			session.merge(quotaTypeViewMap);
			session.flush();
			session.clear();
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@DataResolver
	public void saveDefaultViewQuotaTypes(String userId,Collection<QuotaType> quotaTypes){
		Session session=this.getSessionFactory().openSession();
		QuotaTypeViewMap quotaTypeViewMap=getQuotaTypeViewMapByUser(userId);
		Set<QuotaType> thisDefaultViewQuotaTypes=quotaTypeViewMap.getDefaultViewQuotaTypes();
		
		try {
			for (QuotaType quotaType : quotaTypes) {
				EntityState state=EntityUtils.getState(quotaType);
				if (state.equals(EntityState.NEW)) {
					QuotaType add=quotaTypeDao.getQuotaType(quotaType.getId());
					thisDefaultViewQuotaTypes.add(add);
				}else if (state.equals(EntityState.DELETED)) {
					for (QuotaType thisDefaultViewQuotaType : thisDefaultViewQuotaTypes) {
						if ((thisDefaultViewQuotaType.getId()).equals(quotaType.getId())) {
							thisDefaultViewQuotaTypes.remove(thisDefaultViewQuotaType);
							break;
						}
					}
				}
			}
			quotaTypeViewMap.setDefaultViewQuotaTypes(thisDefaultViewQuotaTypes);
			session.merge(quotaTypeViewMap);
			session.flush();
			session.clear();
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
