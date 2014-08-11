package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Collection;
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
import com.bstek.dorado.annotation.Expose;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.QuotaFormula;
import com.quotamanagesys.model.QuotaItem;
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaProperty;
import com.quotamanagesys.model.QuotaPropertyValue;
import com.quotamanagesys.model.QuotaType;

@Component
public class QuotaItemCreatorDao extends HibernateDao {

	@Resource
	QuotaDimensionOneDao quotaDimensionOneDao;
	@Resource
	QuotaDimensionTwoDao quotaDimensionTwoDao;
	@Resource
	QuotaPropertyValueDao quotaPropertyValueDao;
	@Resource
	QuotaFormulaDao quotaFormulaDao;
	@Resource
	QuotaPropertyDao quotaPropertyDao;
	@Resource
	QuotaItemDao quotaItemDao;
	@Resource
	QuotaTypeDao quotaTypeDao;

	@DataProvider
	public Collection<QuotaItemCreator> getAll() {
		String hqlString = "from " + QuotaItemCreator.class.getName();
		Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
		return quotaItemCreators;
	}

	@DataProvider
	public QuotaItemCreator getQuotaItemCreator(String id) {
		String hqlString = "from " + QuotaItemCreator.class.getName()
				+ " where id='" + id + "'";
		List<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
		if (quotaItemCreators.size() > 0) {
			return quotaItemCreators.get(0);
		} else {
			return null;
		}
	}

	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsByQuotaType(
			String quotaTypeId) {
		String hqlString = "from " + QuotaItemCreator.class.getName()
				+ " where quotaType.id='" + quotaTypeId + "'";
		Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
		return quotaItemCreators;
	}

	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsByYear(int year) {
		String hqlString = "from " + QuotaItemCreator.class.getName()
				+ " where year=" + year;
		Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
		return quotaItemCreators;
	}

	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsByManageDept(
			String manageDeptId) throws Exception {
		String hqlString = "from " + QuotaItemCreator.class.getName()
				+ " where quotaType.manageDept.id='" + manageDeptId + "'";
		Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
		List<QuotaItemCreator> results=new ArrayList<QuotaItemCreator>();
		for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
			Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreator.getId());
			String quotaPropertiesNames="";
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				if (quotaPropertiesNames=="") {
					quotaPropertiesNames=quotaPropertyValue.getQuotaProperty().getName();
				}else {
					quotaPropertiesNames=quotaPropertiesNames+","+quotaPropertyValue.getQuotaProperty().getName();
				}
			}
			QuotaItemCreator targetQuotaItemCreator=EntityUtils.toEntity(quotaItemCreator);
			EntityUtils.setValue(targetQuotaItemCreator, "quotaPropertiesNames", quotaPropertiesNames);
			results.add(targetQuotaItemCreator);
		}
		return results;
	}

	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsByManageDept2(
			String manageDeptId,String quotaFormulaResultId) throws Exception {
		String hqlString = "from " + QuotaItemCreator.class.getName()
				+ " where quotaType.manageDept.id='" + manageDeptId + "'";
		Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
		List<QuotaItemCreator> results=new ArrayList<QuotaItemCreator>();
		for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
			Collection<QuotaFormula> quotaFormulas=quotaItemCreator.getQuotaFormulas();
			boolean formulaResultIsLinked=false;
			if (quotaFormulas.size()>0) {
				for (QuotaFormula quotaFormula : quotaFormulas) {
					if (quotaFormula.getQuotaFormulaResult().getId().equals(quotaFormulaResultId)) {
						formulaResultIsLinked=true;
						break;
					}
				}
			}
			if (formulaResultIsLinked==false) {
				Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreator.getId());
				String quotaPropertiesNames="";
				for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
					if (quotaPropertiesNames=="") {
						quotaPropertiesNames=quotaPropertyValue.getQuotaProperty().getName();
					}else {
						quotaPropertiesNames=quotaPropertiesNames+","+quotaPropertyValue.getQuotaProperty().getName();
					}
				}
				
				QuotaItemCreator targetQuotaItemCreator=EntityUtils.toEntity(quotaItemCreator);
				EntityUtils.setValue(targetQuotaItemCreator, "quotaPropertiesNames", quotaPropertiesNames);
				results.add(targetQuotaItemCreator);
			}
		}
		return results;
	}

	
	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsByQuotaCover(
			String quotaCoverId) {
		String hqlString = "from " + QuotaItemCreator.class.getName()
				+ " where quotaCover.id='" + quotaCoverId + "'";
		Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
		return quotaItemCreators;
	}

	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsByFormula(String quotaFormulaId) throws Exception {
		Session session = this.getSessionFactory().openSession();
		List<QuotaItemCreator> quotaItemCreators = session
				.createCriteria(QuotaItemCreator.class)
				.createAlias("quotaFormulas", "q")
				.add(Restrictions.eq("q.id", quotaFormulaId)).list();
		session.flush();
		session.close();
		List<QuotaItemCreator> results=new ArrayList<QuotaItemCreator>();
		for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
			Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreator.getId());
			String quotaPropertiesNames="";
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				if (quotaPropertiesNames=="") {
					quotaPropertiesNames=quotaPropertyValue.getQuotaProperty().getName();
				}else {
					quotaPropertiesNames=quotaPropertiesNames+","+quotaPropertyValue.getQuotaProperty().getName();
				}
			}
			QuotaItemCreator targetQuotaItemCreator=EntityUtils.toEntity(quotaItemCreator);
			EntityUtils.setValue(targetQuotaItemCreator, "quotaPropertiesNames", quotaPropertiesNames);
			results.add(targetQuotaItemCreator);
		}
		return results;
	}

	@Expose
	public void createQuotaItems() {
		Collection<QuotaItemCreator> quotaItemCreators = this.getAll();
		Session session = this.getSessionFactory().openSession();
		try {
			for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
				Collection<QuotaItem> quotaItems = quotaItemDao
						.getQuotaItemsByQuotaItemCreator(quotaItemCreator
								.getId());
				if (quotaItems.size() == 0) {
					String rate = quotaItemCreator.getQuotaType().getRate();
					switch (rate) {
					case "年度": {
						QuotaItem quotaItem = new QuotaItem();
						quotaItem.setYear(quotaItemCreator.getYear());
						quotaItem.setQuotaItemCreator(quotaItemCreator);
						session.save(quotaItem);
						break;
					}
					case "季度": {
						int quarterCount = 4;
						for (int i = 1; i <= quarterCount; i++) {
							QuotaItem quotaItem = new QuotaItem();
							quotaItem.setYear(quotaItemCreator.getYear());
							quotaItem.setQuarter(i);
							quotaItem.setQuotaItemCreator(quotaItemCreator);
							session.save(quotaItem);
						}
						break;
					}
					case "月度": {
						int monthCount = 12;
						for (int i = 1; i <= monthCount; i++) {
							QuotaItem quotaItem = new QuotaItem();
							quotaItem.setYear(quotaItemCreator.getYear());
							quotaItem.setMonth(i);
							quotaItem.setQuotaItemCreator(quotaItemCreator);
							session.save(quotaItem);
						}
						break;
					}
					default:
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		} finally {
			session.flush();
			session.close();
		}
	}

	@DataResolver
	public void saveQuotaItemCreators(Collection<QuotaItemCreator> quotaItemCreators) {
		Session session = this.getSessionFactory().openSession();
		try {
			for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
				EntityState state = EntityUtils.getState(quotaItemCreator);
				if (state.equals(EntityState.NEW)) {
					QuotaType quotaType = quotaTypeDao.getQuotaType(quotaItemCreator.getQuotaType().getId());
					quotaItemCreator.setName(quotaType.getQuotaTypeName());
					quotaItemCreator.setQuotaType(quotaType);
					session.merge(quotaItemCreator);
				} else if (state.equals(EntityState.MODIFIED)) {
					QuotaType quotaType = quotaTypeDao.getQuotaType(quotaItemCreator.getQuotaType().getId());
					String oldName = this.getQuotaItemCreator(
							quotaItemCreator.getId()).getName();
					String newName = quotaType.getQuotaTypeName();
					if (!oldName.equals(newName)) {
						quotaItemCreator.setName(newName);
					}
					quotaItemCreator.setQuotaType(quotaType);
					session.merge(quotaItemCreator);
				} else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaItemCreator);
				}
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		} finally {
			session.flush();
			session.close();
		}
	}
	
	@DataResolver
	public void saveQuotaItemCreatorsWithFormula(Collection<QuotaItemCreator> quotaItemCreators,String quotaFormulaId){
		Session session=this.getSessionFactory().openSession();
		QuotaFormula quotaFormula=quotaFormulaDao.getQuotaFormula(quotaFormulaId);
		try {
			for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
				EntityState state=EntityUtils.getState(quotaItemCreator);
				if (state.equals(EntityState.NEW)) {
					QuotaItemCreator updateQuotaItemCreator=this.getQuotaItemCreator(quotaItemCreator.getId());
					Set<QuotaFormula> formulas=updateQuotaItemCreator.getQuotaFormulas();
					formulas.add(quotaFormula);
					updateQuotaItemCreator.setQuotaFormulas(formulas);
					session.merge(updateQuotaItemCreator);
				}else if (state.equals(EntityState.DELETED)) {
					QuotaItemCreator updateQuotaItemCreator=this.getQuotaItemCreator(quotaItemCreator.getId());
					Set<QuotaFormula> formulas=updateQuotaItemCreator.getQuotaFormulas();
					for (QuotaFormula quotaFormula2 : formulas) {
						if (quotaFormula2.getId().equals(quotaFormula.getId())) {
							formulas.remove(quotaFormula2);
							break;
						}
					}	
					updateQuotaItemCreator.setQuotaFormulas(formulas);
					session.merge(updateQuotaItemCreator);
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
