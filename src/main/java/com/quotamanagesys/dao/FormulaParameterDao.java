package com.quotamanagesys.dao;

import java.util.Collection;
import java.util.List;

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
import com.quotamanagesys.model.QuotaProperty;

@Component
public class FormulaParameterDao extends HibernateDao {

	@DataProvider
	public Collection<FormulaParameter> getAll() {
		String hqlString = "from " + FormulaParameter.class.getName();
		Collection<FormulaParameter> formulaParameters = this.query(hqlString);
		return formulaParameters;
	}

	@DataProvider
	public FormulaParameter getFormulaParameter(String id) {
		String hqlString = "from " + FormulaParameter.class.getName()
				+ " where id='" + id + "'";
		List<FormulaParameter> formulaParameters = this.query(hqlString);
		if (formulaParameters.size() > 0) {
			return formulaParameters.get(0);
		} else {
			return null;
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
