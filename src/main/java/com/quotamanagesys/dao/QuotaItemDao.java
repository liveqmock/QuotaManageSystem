package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Calendar;
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
import com.bstek.dorado.view.View;
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
		String hqlString = "from " + QuotaItem.class.getName()
				+" order by quotaItemCreator.quotaType.quotaLevel.level asc,quotaItemCreator.name,year asc,month asc,"
				+"quotaItemCreator.quotaCover.sort asc";
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
	public Collection<QuotaItem> getQuotaItemsByManageDept(String manageDeptId) {
		Calendar calendar=Calendar.getInstance();	
		//获取执行时的年月
		int year=calendar.get(Calendar.YEAR);
		int month=calendar.get(Calendar.MONTH);//calendar的真实月份需要+1,因为calendar的月份从0开始
		
		//去年12月情况
		if (month==0) {
			year=year-1;
			month=12;
		}
		
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaType.manageDept.id='"
				+ manageDeptId + "' and year="+year+" and month="+month
				+" order by quotaItemCreator.quotaType.quotaLevel.level asc,quotaItemCreator.name,year asc,month asc,"
				+"quotaItemCreator.quotaCover.sort asc";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByManageDept_Month(String manageDeptId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaType.manageDept.id='"
				+ manageDeptId 
				+ "' and quotaItemCreator.quotaType.rate='月'"
				+" order by quotaItemCreator.quotaType.quotaLevel.level asc,quotaItemCreator.name,year asc,month asc,"
				+"quotaItemCreator.quotaCover.sort asc";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByManageDept_Year(String manageDeptId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaType.manageDept.id='"
				+ manageDeptId 
				+ "' and quotaItemCreator.quotaType.rate='年'"
				+" order by quotaItemCreator.quotaType.quotaLevel.level asc,quotaItemCreator.name,year asc,month asc,"
				+"quotaItemCreator.quotaCover.sort asc";
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
				+ "' and quotaItemCreator.quotaType.quotaLevel.level='"+highestLevel+"'"
				+" order by quotaItemCreator.quotaType.quotaLevel.level asc,quotaItemCreator.name,year asc,month asc,"
				+"quotaItemCreator.quotaCover.sort asc";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}
	
	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByYear(int year) {
		String hqlString = "from " + QuotaItem.class.getName() + " where year="
				+ year
				+" order by quotaItemCreator.quotaType.quotaLevel.level asc,quotaItemCreator.name,year asc,month asc,"
				+"quotaItemCreator.quotaCover.sort asc";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByQuotaType(String quotaTypeId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaType.id='" + quotaTypeId + "'"
				+" order by quotaItemCreator.quotaType.quotaLevel.level asc,quotaItemCreator.name,year asc,month asc,"
				+"quotaItemCreator.quotaCover.sort asc";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByQuotaCover(String quotaCoverId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaCover.id='" + quotaCoverId
				+ "'"
				+" order by quotaItemCreator.quotaType.quotaLevel.level asc,quotaItemCreator.name,year asc,month asc,"
				+"quotaItemCreator.quotaCover.sort asc";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByRate(String rate) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaType.rate='" + rate + "'"
				+" order by quotaItemCreator.quotaType.quotaLevel.level asc,quotaItemCreator.name,year asc,month asc,"
				+"quotaItemCreator.quotaCover.sort asc";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByQuotaItemCreator(
			String quotaItemCreatorId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.id='" + quotaItemCreatorId + "'"
				+" order by quotaItemCreator.quotaType.quotaLevel.level asc,quotaItemCreator.name,year asc,month asc,"
				+"quotaItemCreator.quotaCover.sort asc";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
	}

	@DataProvider
	public Collection<QuotaItem> getQuotaItemsByDutyDept(String dutyDeptId) {
		String hqlString = "from " + QuotaItem.class.getName()
				+ " where quotaItemCreator.quotaDutyDept.id='" + dutyDeptId
				+ "'"
				+" order by quotaItemCreator.quotaType.quotaLevel.level asc,quotaItemCreator.name,year asc,month asc,"
				+"quotaItemCreator.quotaCover.sort asc";
		Collection<QuotaItem> quotaItems = this.query(hqlString);
		return quotaItems;
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
					//级联删除QuotaFormulaResultValue
					Collection<QuotaFormulaResultValue> quotaFormulaResultValues = quotaFormulaResultValueDao
							.getQuotaFormulaResultValuesByQuotaItem(quotaItem.getId());
					quotaFormulaResultValueDao.deleteQuotaFormulaResultValues(quotaFormulaResultValues);
					
					//级联删除QuotaTargetValue
					Collection<QuotaTargetValue> quotaTargetValues=quotaTargetValueDao
							.getQuotaTargetValuesByQuotaItem(quotaItem.getId());
					quotaTargetValueDao.deleteQuotaTargetValues(quotaTargetValues);

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
				//级联删除QuotaFormulaResultValue
				Collection<QuotaFormulaResultValue> quotaFormulaResultValues = quotaFormulaResultValueDao
						.getQuotaFormulaResultValuesByQuotaItem(quotaItem.getId());
				quotaFormulaResultValueDao.deleteQuotaFormulaResultValues(quotaFormulaResultValues);
				
				//级联删除QuotaTargetValue
				Collection<QuotaTargetValue> quotaTargetValues=quotaTargetValueDao
						.getQuotaTargetValuesByQuotaItem(quotaItem.getId());
				quotaTargetValueDao.deleteQuotaTargetValues(quotaTargetValues);

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
