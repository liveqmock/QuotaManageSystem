package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import com.quotamanagesys.model.FormulaParameter;
import com.quotamanagesys.model.QuotaCover;
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaProperty;
import com.quotamanagesys.model.QuotaPropertyValue;
import com.quotamanagesys.model.QuotaType;

@Component
public class QuotaPropertyDao extends HibernateDao {
	
	@Resource
	QuotaItemCreatorDao quotaItemCreatorDao;
	@Resource
	QuotaCoverDao quotaCoverDao;
	@Resource
	QuotaPropertyValueDao quotaPropertyValueDao;

	@DataProvider
	public Collection<QuotaProperty> getAll() {
		String hqlString = "from " + QuotaProperty.class.getName();
		Collection<QuotaProperty> quotaProperties = this.query(hqlString);
		return quotaProperties;
	}

	@DataProvider
	public QuotaProperty getQuotaProperty(String id) {
		String hqlString = "from " + QuotaProperty.class.getName()
				+ " where id='" + id + "'";
		List<QuotaProperty> quotaProperties = this.query(hqlString);
		if (quotaProperties.size() > 0) {
			return quotaProperties.get(0);
		} else {
			return null;
		}
	}

	@DataProvider
	public QuotaProperty getQuotaPropertyByName(String quotaPropertyName) {
		String hqlString = "from " + QuotaProperty.class.getName()
				+ " where name='" + quotaPropertyName + "'";
		List<QuotaProperty> quotaProperties = this.query(hqlString);
		if (quotaProperties.size() > 0) {
			return quotaProperties.get(0);
		} else {
			return null;
		}
	}
	
	//下一级口径的指标属性只能是上级口径指标属性的子集
	@DataProvider
	public Collection<QuotaProperty> getQuotaPropertiesByQuotaItemCreator(String quotaItemCreatorId){
		QuotaItemCreator quotaItemCreator=quotaItemCreatorDao.getQuotaItemCreator(quotaItemCreatorId);
		QuotaCover quotaCover=quotaItemCreator.getQuotaCover();
		QuotaType quotaType=quotaItemCreator.getQuotaType();
		int year=quotaItemCreator.getYear();
		if ((quotaCover.getFatherQuotaCover())==null) {
			return getAll();
		}else{
			String hqlString="from "+QuotaItemCreator.class.getName()+" where year="+year
					+" and quotaType.id='"+quotaType.getId()+"'"
					+" and quotaCover.id='"+quotaCover.getFatherQuotaCover().getId()+"'";
			List<QuotaItemCreator> quotaItemCreators=this.query(hqlString);
			QuotaItemCreator fatherCoverQuotaItemCreator=quotaItemCreators.get(0);
			ArrayList<QuotaProperty> quotaProperties=new ArrayList<QuotaProperty>();
			Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(fatherCoverQuotaItemCreator.getId());
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				quotaProperties.add(quotaPropertyValue.getQuotaProperty());
			}
			return quotaProperties;
		}
	}

	@DataResolver
	public void saveQuotaProperties(Collection<QuotaProperty> quotaProperties) {
		Session session = this.getSessionFactory().openSession();
		try {
			for (QuotaProperty quotaProperty : quotaProperties) {
				EntityState state = EntityUtils.getState(quotaProperty);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaProperty);
					session.flush();
					session.clear();

					FormulaParameter formulaParameter1 = new FormulaParameter();
					formulaParameter1.setQuotaProperty(quotaProperty);
					formulaParameter1.setParameterName(quotaProperty
							.getParameterName());
					formulaParameter1.setRate("年");
					session.save(formulaParameter1);

					FormulaParameter formulaParameter2 = new FormulaParameter();
					formulaParameter2.setQuotaProperty(quotaProperty);
					formulaParameter2.setParameterName(quotaProperty
							.getParameterName() + "_M");
					formulaParameter2.setRate("月");
					session.save(formulaParameter2);
					session.flush();
					session.clear();
				} else if (state.equals(EntityState.MODIFIED)) {
					session.merge(quotaProperty);
					session.flush();
					session.clear();

					String hqlString = "from "
							+ FormulaParameter.class.getName()
							+ " where quotaProperty.id='"
							+ quotaProperty.getId() + "'";
					Collection<FormulaParameter> formulaParameters=this.query(hqlString);
					if (formulaParameters.size()>0) {
						for (FormulaParameter formulaParameter : formulaParameters) {
							if (formulaParameter.getRate().equals("年")) {
								formulaParameter.setParameterName(quotaProperty.getParameterName());
								session.merge(formulaParameter);
								session.flush();
								session.clear();
							}else {
								formulaParameter.setParameterName(quotaProperty.getParameterName()+"_M");
								session.merge(formulaParameter);
								session.flush();
								session.clear();
							}
							
						}
					}else {
						FormulaParameter formulaParameter1 = new FormulaParameter();
						formulaParameter1.setQuotaProperty(quotaProperty);
						formulaParameter1.setParameterName(quotaProperty
								.getParameterName());
						formulaParameter1.setRate("年");
						session.save(formulaParameter1);

						FormulaParameter formulaParameter2 = new FormulaParameter();
						formulaParameter2.setQuotaProperty(quotaProperty);
						formulaParameter2.setParameterName(quotaProperty
								.getParameterName() + "_M");
						formulaParameter2.setRate("月");
						session.save(formulaParameter2);
						session.flush();
						session.clear();
					}
					
				} else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaProperty);
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
