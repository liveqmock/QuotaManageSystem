package com.quotamanagesys.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaLevel;
import com.quotamanagesys.model.QuotaPropertyValue;
import com.quotamanagesys.model.QuotaType;

@Component
public class QuotaTypeDao extends HibernateDao {
	
	@Resource
	QuotaLevelDao quotaLevelDao;
	@Resource
	QuotaTypeDao quotaTypeDao;
	@Resource
	QuotaItemCreatorDao quotaItemCreatorDao;
	@Resource
	QuotaPropertyValueDao quotaPropertyValueDao;
	
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
	public QuotaType getFatherQuotaType(String id){
		String hqlString = "from " + QuotaType.class.getName()+" where id='"+id+"'";
		List<QuotaType> quotaTypes = this.query(hqlString);
		if (quotaTypes.size()>0) {
			return quotaTypes.get(0).getFatherQuotaType();
		}else {
			return null;
		}
	}
	
	@DataProvider
	public Collection<QuotaType> getFatherQuotaTypesByQuotaLevel(String quotaLevelId) throws Exception{
		QuotaLevel quotaLevel=quotaLevelDao.getQuotaLevel(quotaLevelId);
		Session session = this.getSessionFactory().openSession();
		List<QuotaType> fatherQuotaTypes= session.createCriteria(QuotaType.class).createAlias("quotaLevel", "l")
				.add(Restrictions.eq("l.level",(quotaLevel.getLevel())-1)).list();
		//获取父指标种类
		session.flush();
		session.close();
		return fatherQuotaTypes;
	}
	
	public void getFatherQuotaTypeTree(QuotaType quotaType,Collection<QuotaType> fatherQuotaTypeTree){
		QuotaType fatherQuotaType=quotaType.getFatherQuotaType();
		if (fatherQuotaType!=null) {
			fatherQuotaTypeTree.add(fatherQuotaType);
			getFatherQuotaTypeTree(fatherQuotaType, fatherQuotaTypeTree);
		}
	}
	
	@DataProvider
	public Collection<QuotaType> getTopLevelQuotaTypes(){
		Collection<QuotaLevel> quotaLevels=quotaLevelDao.getAll();
		int highestLevel=-1;
		for (QuotaLevel quotaLevel : quotaLevels) {
			if (highestLevel==-1) {
				highestLevel=quotaLevel.getLevel();
			}else{
				if (highestLevel>quotaLevel.getLevel()) {
					highestLevel=quotaLevel.getLevel();
				}
			}
		}
		String hqlString="from "+QuotaType.class.getName()+" where quotaLevel.level='"+highestLevel+"'";
		Collection<QuotaType> quotaTypes=this.query(hqlString);
		return quotaTypes;
	}
	
	@DataProvider
	public Collection<QuotaType> getChildrenQuotaTypes(String id){
		String hqlString="from "+QuotaType.class.getName()+" where fatherQuotaType.id='"+id+"'";
		Collection<QuotaType> quotaTypes=this.query(hqlString);
		return quotaTypes;
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
	
	@DataProvider
	public Collection<QuotaType> getQuotaTypesByQuotaDimension(String quotaDimensionId){
		String hqlString="from "+QuotaType.class.getName()+" where quotaDimension.id='"+quotaDimensionId+"'";
		Collection<QuotaType> quotaTypes=this.query(hqlString);
		return quotaTypes;
	}
	
	@DataProvider
	public Collection<QuotaType> getQuotaTypesByFatherQuotaType(String fatherQuotaTypeId){
		String hqlString="from "+QuotaType.class.getName()+" where fatherQuotaType.id='"+fatherQuotaTypeId+"'";
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
					QuotaType fatherQuotaType=quotaType.getFatherQuotaType();
					QuotaType oldQuotaType=getQuotaType(quotaType.getId());
					QuotaType oldFatherQuotaType=oldQuotaType.getFatherQuotaType();
					if (fatherQuotaType==null) {
						quotaType.setFatherQuotaType(oldFatherQuotaType);
					}else {
						quotaType.setFatherQuotaType(quotaTypeDao.getQuotaType(fatherQuotaType.getId()));
					}
					session.merge(quotaType);
				} else if (state.equals(EntityState.DELETED)) {
					//将下级指标种类的父级设置为null
					Collection<QuotaType> childrenQuotaTypes=getChildrenQuotaTypes(quotaType.getId());
					for (QuotaType child : childrenQuotaTypes) {
						child.setFatherQuotaType(null);
						session.merge(child);
						session.flush();
						session.clear();
					}
					
					//级联删除QuotaItemCreator
					Collection<QuotaItemCreator> quotaItemCreators=quotaItemCreatorDao.getQuotaItemCreatorsByQuotaType(quotaType.getId());
					quotaItemCreatorDao.deleteQuotaItemCreators(quotaItemCreators);
					
					//级联删除QuotaPropertyValue
					Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaProperty(quotaType.getId());
					quotaPropertyValueDao.deleteQuotaPropertyValues(quotaPropertyValues);
	
					quotaType.setFatherQuotaType(null);
					quotaType.setManageDept(null);
					quotaType.setQuotaDimension(null);
					quotaType.setQuotaLevel(null);
					quotaType.setQuotaProfession(null);
					quotaType.setQuotaUnit(null);
					session.delete(quotaType);
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
	public void deleteQuotaTypes(Collection<QuotaType> quotaTypes) {
		Session session = this.getSessionFactory().openSession();
		try {
			for (QuotaType quotaType : quotaTypes) {
				//将下级指标种类的父级设置为null
				Collection<QuotaType> childrenQuotaTypes=getChildrenQuotaTypes(quotaType.getId());
				for (QuotaType child : childrenQuotaTypes) {
					child.setFatherQuotaType(null);
					session.merge(child);
					session.flush();
					session.clear();
				}
				
				//级联删除QuotaItemCreator
				Collection<QuotaItemCreator> quotaItemCreators=quotaItemCreatorDao.getQuotaItemCreatorsByQuotaType(quotaType.getId());
				quotaItemCreatorDao.deleteQuotaItemCreators(quotaItemCreators);
				
				//级联删除QuotaPropertyValue
				Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaProperty(quotaType.getId());
				quotaPropertyValueDao.deleteQuotaPropertyValues(quotaPropertyValues);

				quotaType.setFatherQuotaType(null);
				quotaType.setManageDept(null);
				quotaType.setQuotaDimension(null);
				quotaType.setQuotaLevel(null);
				quotaType.setQuotaProfession(null);
				quotaType.setQuotaUnit(null);
				session.delete(quotaType);
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
