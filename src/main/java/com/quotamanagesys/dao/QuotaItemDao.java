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
import com.bstek.bdf2.core.exception.NoneLoginException;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.annotation.Expose;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.interceptor.CalculateCore;
import com.quotamanagesys.interceptor.ResultTableCreator;
import com.quotamanagesys.model.QuotaFormulaResultValue;
import com.quotamanagesys.model.QuotaItem;
import com.quotamanagesys.model.QuotaLevel;
import com.quotamanagesys.model.QuotaTargetValue;

@Component
public class QuotaItemDao extends HibernateDao {

	@Resource
	QuotaFormulaResultValueDao quotaFormulaResultValueDao;
	@Resource
	QuotaTargetValueDao quotaTargetValueDao;
	@Resource
	QuotaLevelDao quotaLevelDao;
	@Resource
	CalculateCore calculateCore;
	@Resource
	ResultTableCreator resultTableCreator;

	@DataProvider
	public Collection<QuotaItem> getAll() {
		String hqlString = "from " + QuotaItem.class.getName();
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public QuotaItem getQuotaItem(String id) {
		String hqlString = "from " + QuotaItem.class.getName() + " where id='"+ id + "'";
		List<QuotaItem> quotaItems = this.query(hqlString);
		if (quotaItems.size() > 0) {
			return quotaItems.get(0);
		} else {
			return null;
		}
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByFatherItem(String fatherItemId){
		String hqlString="from "+QuotaItem.class.getName()+" where fatherQuotaItem.id='"+fatherItemId+"'";
		Collection<QuotaItem> quotaItems=this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByManageDept(String manageDeptId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaType.manageDept.id='"
				+ manageDeptId + "'";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByUserDept(){
		IUser loginuser = ContextHolder.getLoginUser();
		List<IDept> depts=loginuser.getDepts();
		if (depts.size()>0) {
			String hqlString="from "+QuotaItem.class.getName()
					+" where quotaItemCreator.quotaType.manageDept.id='"+depts.get(0).getId()+"'";
			Collection<QuotaItem> quotaItems=this.query(hqlString);
			return quotaItems;
		}else {
			if (loginuser.isAdministrator()) {
				String hqlString="from "+QuotaItem.class.getName();
				Collection<QuotaItem> quotaItems=this.query(hqlString);
				return quotaItems;
			}else {
				return null;
			}
		}
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByManageDept_Month(String manageDeptId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaType.manageDept.id='"
				+ manageDeptId 
				+ "' and quotaItemCreator.quotaType.rate='月'";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByManageDept_Year(String manageDeptId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaType.manageDept.id='"
				+ manageDeptId 
				+ "' and quotaItemCreator.quotaType.rate='年'";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByManageDeptAndTopLevel(String manageDeptId) {
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
		
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaType.manageDept.id='"+ manageDeptId 
				+ "' and quotaItemCreator.quotaType.quotaLevel.level='"+highestLevel+"'";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByYear(int year) {
		String hqlString = "from " + QuotaItem.class.getName() + " where year="
				+ year;
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByQuotaType(String quotaTypeId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaType.id='" + quotaTypeId + "'";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByQuotaCover(String quotaCoverId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaCover.id='" + quotaCoverId
				+ "'";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByRate(String rate) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaType.rate='" + rate + "'";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByQuotaItemCreator(
			String quotaItemCreatorId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.id='" + quotaItemCreatorId + "'";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByDutyDept(String dutyDeptId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaDutyDept.id='" + dutyDeptId
				+ "'";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}
	
	//关联具体指标间关系
	@Expose
	public void createQuotaItemsRelation() throws Exception{
		Session session=this.getSessionFactory().openSession();
		try {
			String checkUnRelatedQuotaItems="from "+QuotaItem.class.getName()+" where fatherQuotaItem.id=null "
					+"and quotaItemCreator.quotaType.quotaLevel.level>1 order by quotaItemCreator.quotaType.quotaLevel.level asc";
			Collection<QuotaItem> unRelatedQuotaItems=this.query(checkUnRelatedQuotaItems);
			for (QuotaItem quotaItem : unRelatedQuotaItems) {
				String findFatherQuotaItem="from "+QuotaItem.class.getName()+" where year="+quotaItem.getYear()
						+" and month="+quotaItem.getMonth()
						+" and quotaItemCreator.quotaCover.id='"+quotaItem.getQuotaItemCreator().getQuotaCover().getId()+"'"
						+" and quotaItemCreator.quotaType.id='"+quotaItem.getQuotaItemCreator().getQuotaType().getFatherQuotaType().getId()+"'";
				List<QuotaItem> fatherQuotaItems=this.query(findFatherQuotaItem);
				QuotaItem fatherQuotaItem=null;
				if (fatherQuotaItems.size()>0) {
					fatherQuotaItem=fatherQuotaItems.get(0);
					quotaItem.setFatherQuotaItem(fatherQuotaItem);
				}
				session.merge(quotaItem);
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
	public void saveQuotaItems(Collection<QuotaItem> quotaItems) {
		Session session = this.getSessionFactory().openSession();
		Collection<QuotaItem> updateQuotaItems=new ArrayList<QuotaItem>();
		
		try {
			for (QuotaItem quotaItem : quotaItems) {
				EntityState state = EntityUtils.getState(quotaItem);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaItem);
				} else if (state.equals(EntityState.MODIFIED)) {
					QuotaItem thisQuotaItem = getQuotaItem(quotaItem.getId());
					if (quotaItem.getFinishValue() != null) {
						thisQuotaItem.setFinishValue(quotaItem.getFinishValue());
					}
					if (quotaItem.getAccumulateValue()!=null) {
						thisQuotaItem.setAccumulateValue(quotaItem.getAccumulateValue());
					}
					if (quotaItem.getSameTermValue()!=null) {
						thisQuotaItem.setSameTermValue(quotaItem.getSameTermValue());
					}
					session.merge(thisQuotaItem);
					session.flush();
					session.clear();
					updateQuotaItems.add(thisQuotaItem);
				} else if (state.equals(EntityState.DELETED)) {
					//将下级具体指标的父级设置为null
					Collection<QuotaItem> childrenItems=getQuotaItemsByFatherItem(quotaItem.getId());
					for (QuotaItem child : childrenItems) {
						child.setFatherQuotaItem(null);
						session.merge(child);
						session.flush();
						session.clear();
					}
					
					//级联删除QuotaFormulaResultValue
					Collection<QuotaFormulaResultValue> quotaFormulaResultValues = quotaFormulaResultValueDao
							.getQuotaFormulaResultValuesByQuotaItem(quotaItem.getId());
					quotaFormulaResultValueDao.deleteQuotaFormulaResultValues(quotaFormulaResultValues);
					
					//级联删除QuotaTargetValue
					Collection<QuotaTargetValue> quotaTargetValues=quotaTargetValueDao
							.getQuotaTargetValuesByQuotaItem(quotaItem.getId());
					quotaTargetValueDao.deleteQuotaTargetValues(quotaTargetValues);

					quotaItem.setFatherQuotaItem(null);
					quotaItem.setQuotaItemCreator(null);
					session.delete(quotaItem);
					session.flush();
					session.clear();
				}
			}
			
			calculateCore.calculate(updateQuotaItems);
			resultTableCreator.createOrUpdateResultTable(updateQuotaItems);
		} catch (Exception e) {
			System.out.print(e.toString());
		} finally {
			session.flush();
			session.close();
		}
	}
	
	@DataResolver
	public void deleteQuotaItems(Collection<QuotaItem> quotaItems) {
		Session session = this.getSessionFactory().openSession();
		try {
			resultTableCreator.deleteItemsFromResultTable(quotaItems);
			for (QuotaItem quotaItem : quotaItems) {
				//将下级具体指标的父级设置为null
				Collection<QuotaItem> childrenItems=getQuotaItemsByFatherItem(quotaItem.getId());
				for (QuotaItem child : childrenItems) {
					child.setFatherQuotaItem(null);
					session.merge(child);
					session.flush();
					session.clear();
				}
				
				//级联删除QuotaFormulaResultValue
				Collection<QuotaFormulaResultValue> quotaFormulaResultValues = quotaFormulaResultValueDao
						.getQuotaFormulaResultValuesByQuotaItem(quotaItem.getId());
				quotaFormulaResultValueDao.deleteQuotaFormulaResultValues(quotaFormulaResultValues);
				
				//级联删除QuotaTargetValue
				Collection<QuotaTargetValue> quotaTargetValues=quotaTargetValueDao
						.getQuotaTargetValuesByQuotaItem(quotaItem.getId());
				quotaTargetValueDao.deleteQuotaTargetValues(quotaTargetValues);

				quotaItem.setFatherQuotaItem(null);
				quotaItem.setQuotaItemCreator(null);
				session.delete(quotaItem);
				session.flush();
				session.clear();
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		} finally {
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
