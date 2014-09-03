package com.quotamanagesys.dao;

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
import com.bstek.dorado.annotation.Expose;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.FormulaParameter;
import com.quotamanagesys.model.QuotaFormula;
import com.quotamanagesys.model.QuotaType;
import com.quotamanagesys.model.QuotaTypeFormulaLink;

@Component
public class QuotaTypeFormulaLinkDao extends HibernateDao {

	@Resource
	QuotaTypeDao quotaTypeDao;
	@Resource
	QuotaFormulaDao quotaFormulaDao;

	@DataProvider
	public Collection<QuotaTypeFormulaLink> getAll() {
		String hqlString = "from " + QuotaTypeFormulaLink.class.getName();
		Collection<QuotaTypeFormulaLink> quotaTypeFormulaLinks = this
				.query(hqlString);
		return quotaTypeFormulaLinks;
	}

	@DataProvider
	public Collection<QuotaTypeFormulaLink> getQuotaTypeFormulaLinksByQuotaType(
			String quotaTypeId) {
		String hqlString = "from " + QuotaTypeFormulaLink.class.getName()
				+ " where quotaType.id='" + quotaTypeId + "'";
		Collection<QuotaTypeFormulaLink> quotaTypeFormulaLinks = this
				.query(hqlString);
		return quotaTypeFormulaLinks;
	}

	@DataProvider
	public Collection<QuotaTypeFormulaLink> getQuotaTypeFormulaLinksByFormula(
			String quotaFormulaId) {
		String hqlString = "from " + QuotaTypeFormulaLink.class.getName()
				+ " where quotaFormula.id='" + quotaFormulaId + "'";
		Collection<QuotaTypeFormulaLink> quotaTypeFormulaLinks = this
				.query(hqlString);
		return quotaTypeFormulaLinks;
	}

	@DataProvider
	public QuotaTypeFormulaLink getQuotaTypeFormulaLink(String id) {
		String hqlString = "from " + QuotaTypeFormulaLink.class.getName()
				+ " where id='" + id + "'";
		List<QuotaTypeFormulaLink> quotaTypeFormulaLinks = this
				.query(hqlString);
		if (quotaTypeFormulaLinks.size() > 0) {
			return quotaTypeFormulaLinks.get(0);
		} else {
			return null;
		}
	}

	@DataResolver
	public void saveQuotaTypeFormulaLinks(
			Collection<QuotaTypeFormulaLink> quotaTypeFormulaLinks) {
		Session session = this.getSessionFactory().openSession();
		try {
			for (QuotaTypeFormulaLink quotaTypeFormulaLink : quotaTypeFormulaLinks) {
				EntityState state = EntityUtils.getState(quotaTypeFormulaLink);
				if (state.equals(EntityState.NEW)) {
					session.save(quotaTypeFormulaLink);
				} else if (state.equals(EntityState.MODIFIED)) {
					session.merge(quotaTypeFormulaLink);
				} else if (state.equals(EntityState.DELETED)) {
					session.delete(quotaTypeFormulaLink);
				}
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		} finally {
			session.flush();
			session.close();
		}
	}

	@Expose
	public void creatorQuotaTypeFormulaLinks(Collection<QuotaType> quotaTypes,
			Collection<FormulaParameter> formulaParameters,
			String quotaFormulaId) {
		Session session = this.getSessionFactory().openSession();
		Set<FormulaParameter> formulaParametersSet = new HashSet<FormulaParameter>();
		for (FormulaParameter formulaParameter : formulaParameters) {
			formulaParametersSet.add(formulaParameter);
		}

		try {
			for (QuotaType quotaType : quotaTypes) {
				QuotaFormula quotaFormula = quotaFormulaDao
						.getQuotaFormula(quotaFormulaId);
				String checkIsExsits = "from "
						+ QuotaTypeFormulaLink.class.getName()
						+ " where quotaType.id='" + quotaType.getId()
						+ "' and quotaFormula.quotaFormulaResult.id='"
						+ quotaFormula.getQuotaFormulaResult().getId() + "'";
				Collection<QuotaTypeFormulaLink> linkedTypeFormulaLinks = this.query(checkIsExsits);
				//如已存在同类公式，则操作不再执行。
				if (linkedTypeFormulaLinks.size() == 0) {
					QuotaType thisQuotaType = quotaTypeDao.getQuotaType(quotaType.getId());

					QuotaTypeFormulaLink quotaTypeFormulaLink = new QuotaTypeFormulaLink();
					quotaTypeFormulaLink.setQuotaType(thisQuotaType);
					quotaTypeFormulaLink.setQuotaFormula(quotaFormula);
					quotaTypeFormulaLink.setFormulaParameters(formulaParametersSet);

					session.save(quotaTypeFormulaLink);
					session.flush();
					session.clear();
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
