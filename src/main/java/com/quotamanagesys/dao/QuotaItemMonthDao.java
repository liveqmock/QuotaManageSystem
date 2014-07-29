package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.QuotaItemMonth;
import com.quotamanagesys.model.QuotaProperty;
import com.quotamanagesys.model.QuotaPropertyValue;

@Component
public class QuotaItemMonthDao extends HibernateDao {

	@Resource
	QuotaPropertyDao quotaPropertyDao;
	
	@Resource
	QuotaDimensionOneDao quotaDimensionOneDao;
	
	@Resource
	QuotaDimensionTwoDao quotaDimensionTwoDao;
	
	@Resource
	QuotaPropertyValueDao quotaPropertyValueDao;
	
	@DataProvider
	public Collection<QuotaItemMonth> getAll() {
		String hqlString = "from " + QuotaItemMonth.class.getName();
		Collection<QuotaItemMonth> quotaItemMonths = this.query(hqlString);
		return quotaItemMonths;
	}

	@DataProvider
	public QuotaItemMonth getQuotaItemMonth(String id) {
		String hqlString = "from " + QuotaItemMonth.class.getName()
				+ " where id='" + id + "'";
		List<QuotaItemMonth> quotaItemMonths = this.query(hqlString);
		if (quotaItemMonths.size() > 0) {
			return quotaItemMonths.get(0);
		} else {
			return null;
		}
	}

	@DataProvider
	//
	public Collection<QuotaItemMonth> getQuotaItemMonthsByTime(int year,
			int month) {
		String hqlString;
		if (year > 0 && month > 0) {
			hqlString = "from " + QuotaItemMonth.class.getName()
					+ " where year=" + year + " and month=" + month;
		} else if (year > 0) {
			hqlString = "from " + QuotaItemMonth.class.getName()
					+ " where year=" + year;
		} else if (month > 0) {
			hqlString = "from " + QuotaItemMonth.class.getName()
					+ " where month=" + month;
		} else {
			hqlString = "from " + QuotaItemMonth.class.getName();
		}
		Collection<QuotaItemMonth> quotaItemMonths = this.query(hqlString);
		return quotaItemMonths;
	}

	@DataProvider
	//
	public Collection<QuotaItemMonth> getQuotaItemMonthsByManageDept(
			String manageDeptId) {
		String hqlString = "from " + QuotaItemMonth.class.getName()
				+ " where quotaType.manageDept.id='" + manageDeptId + "'";
		Collection<QuotaItemMonth> quotaItemMonths = this.query(hqlString);
		return quotaItemMonths;
	}

	@DataProvider
	//
	public Collection<QuotaItemMonth> getQuotaItemMonthsByQuotaCover(
			String quotaCoverId) {
		String hqlString = "from " + QuotaItemMonth.class.getName()
				+ " where quotaCover.id='" + quotaCoverId + "'";
		Collection<QuotaItemMonth> quotaItemMonths = this.query(hqlString);
		return quotaItemMonths;
	}

	@DataProvider
	//
	public Collection<QuotaItemMonth> getQuotaItemMonthsByQuotaType(
			String quotaTypeId) {
		String hqlString = "from " + QuotaItemMonth.class.getName()
				+ " where quotaType.id='" + quotaTypeId + "'";
		Collection<QuotaItemMonth> quotaItemMonths = this.query(hqlString);
		return quotaItemMonths;
	}
	
	@DataProvider
	//
	public Collection<QuotaItemMonth> getQuotaItemMonthsByProfession(String quotaProfessionId) throws Exception{
		String hqlString="from "+QuotaItemMonth.class.getName()+" where quotaType.quotaProfession.id='"+quotaProfessionId+"'";
		Collection<QuotaItemMonth> quotaItemMonths=this.query(hqlString);
		List<QuotaItemMonth> results=new ArrayList<QuotaItemMonth>();
		for (QuotaItemMonth quotaItemMonth : quotaItemMonths) {
			QuotaItemMonth targetQuotaItemMonth=EntityUtils.toEntity(quotaItemMonth);
			Set<QuotaProperty> quotaProperties=targetQuotaItemMonth.getQuotaProperties();
			String quotaPropertiesNames="";
			for (QuotaProperty quotaProperty : quotaProperties) {
				if (quotaPropertiesNames=="") {
					quotaPropertiesNames=quotaProperty.getName();
				}else {
					quotaPropertiesNames=quotaPropertiesNames+","+quotaProperty.getName();
				}
			}
			EntityUtils.setValue(targetQuotaItemMonth,"quotaPropertiesNames",quotaPropertiesNames);
			results.add(targetQuotaItemMonth);
		}
		return results;
	}

	@DataProvider
	//
	public Collection<QuotaItemMonth> getQuotaItemMonthsByQuotaDimension(
			String dimensionOneId, String dimensionTwoId) {
		String hqlString;
		if (dimensionOneId != null && dimensionTwoId != null) {
			hqlString = "from " + QuotaItemMonth.class.getName()
					+ " where quotaDimensionOne.id='" + dimensionOneId
					+ "' and quotaDimensionTwo.id='" + dimensionTwoId + "'";
		} else if (dimensionOneId != null) {
			hqlString = "from " + QuotaItemMonth.class.getName()
					+ " where quotaDimensionOne.id='" + dimensionOneId
					+ "'";
		} else if (dimensionTwoId != null) {
			hqlString = "from " + QuotaItemMonth.class.getName()
					+ " where quotaDimensionTwo.id='" + dimensionTwoId + "'";
		} else {
			hqlString = "from " + QuotaItemMonth.class.getName();
		}
		Collection<QuotaItemMonth> quotaItemMonths=this.query(hqlString);
		return quotaItemMonths;
	}

	/*
	@DataProvider
	//
	public Collection<QuotaItemMonth> getQuotaItemMonthsByQuotaProperty(
			String quotaPropertyId) {

	}
	*/
	
	@DataResolver
	public void saveQuotaItemMonths(Collection<QuotaItemMonth> quotaItemMonths){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaItemMonth quotaItemMonth : quotaItemMonths) {
				EntityState state=EntityUtils.getState(quotaItemMonth);
				if (state.equals(EntityState.NEW)) {
					String quotaPropertiesNamesString=EntityUtils.getValue(quotaItemMonth,"quotaPropertiesNames");
					String[] quotaPropertiesNames=quotaPropertiesNamesString.split(",");
					Set<QuotaProperty> quotaProperties=new HashSet<QuotaProperty>();
					for (String quotaPropertyName : quotaPropertiesNames) {
						quotaProperties.add(quotaPropertyDao.getQuotaPropertyByName(quotaPropertyName));
					}
					quotaItemMonth.setQuotaProperties(quotaProperties);
					session.merge(quotaItemMonth);
				}else if (state.equals(EntityState.MODIFIED)) {
					QuotaItemMonth oldQuotaItemMonth=this.getQuotaItemMonth(quotaItemMonth.getId());
					if (oldQuotaItemMonth.getQuotaProperties().size()!=0) {
						quotaItemMonth.setQuotaProperties(oldQuotaItemMonth.getQuotaProperties());
					}
					if (oldQuotaItemMonth.getQuotaPropertyValues().size()!=0) {
						quotaItemMonth.setQuotaPropertyValues(oldQuotaItemMonth.getQuotaPropertyValues());
					}
					session.merge(quotaItemMonth);
				}else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaItemMonth);
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
