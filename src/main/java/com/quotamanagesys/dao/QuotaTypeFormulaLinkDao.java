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
	@Resource
	FormulaParameterDao formulaParameterDao;

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
	
	//获取该指标种类公式关联记录关联的参数集合
	@DataProvider
	public Collection<FormulaParameter> getFormulaParametersByQuotaTypeFormulaLink(String quotaTypeId,String quotaFormulaId){
		if (quotaTypeId!=null&&quotaFormulaId!=null) {
			String hqlString="from "+QuotaTypeFormulaLink.class.getName()+" where quotaType.id='"+quotaTypeId+"'"
					+" and quotaFormula.id='"+quotaFormulaId+"'";
			List<QuotaTypeFormulaLink> quotaTypeFormulaLinks=this.query(hqlString);
			if (quotaTypeFormulaLinks.size()>0) {
				return quotaTypeFormulaLinks.get(0).getFormulaParameters();
			}else {
				return null;
			}
		}else {
			return null;
		}
	}

	//批量关联指标种类到公式
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
	
	//清除指标种类关联的计算公式
	@Expose
	public void clearQuotaTypeFormulaLink(Collection<QuotaType> quotaTypes){
		Session session=this.getSessionFactory().openSession();
		try {
			for (QuotaType quotaType : quotaTypes) {
				Collection<QuotaTypeFormulaLink> quotaTypeFormulaLinks=getQuotaTypeFormulaLinksByQuotaType(quotaType.getId());
				for (QuotaTypeFormulaLink quotaTypeFormulaLink : quotaTypeFormulaLinks) {
					quotaTypeFormulaLink.setQuotaFormula(null);
					quotaTypeFormulaLink.setQuotaType(null);
					quotaTypeFormulaLink.setFormulaParameters(null);
					session.delete(quotaTypeFormulaLink);
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
	
	//指标种类公式关联维护（单条维护）
	@DataResolver
	public void saveQuotaTypeFormulaLink(String quotaTypeId,Collection<QuotaFormula> quotaFormulas){
		Session session=this.getSessionFactory().openSession();
		QuotaType quotaType=quotaTypeDao.getQuotaType(quotaTypeId);
		try {
			for (QuotaFormula quotaFormula : quotaFormulas) {
				EntityState state=EntityUtils.getState(quotaFormula);
				if ((state.equals(EntityState.NEW))||(state.equals(EntityState.NONE))) {
					String checkIsExsits = "from "+ QuotaTypeFormulaLink.class.getName()
							+ " where quotaType.id='" + quotaTypeId
							+ "' and quotaFormula.quotaFormulaResult.id='"
							+ quotaFormula.getQuotaFormulaResult().getId() + "'";
					Collection<QuotaTypeFormulaLink> linkedTypeFormulaLinks = this.query(checkIsExsits);
					if (linkedTypeFormulaLinks.size()==0) {
						QuotaTypeFormulaLink quotaTypeFormulaLink=new QuotaTypeFormulaLink();
						quotaTypeFormulaLink.setQuotaType(quotaType);
						QuotaFormula thisQuotaFormula=quotaFormulaDao.getQuotaFormula(quotaFormula.getId());
						quotaTypeFormulaLink.setQuotaFormula(thisQuotaFormula);
						
						session.save(quotaTypeFormulaLink);
						session.flush();
						session.clear();
					}
				}else if (state.equals(EntityState.DELETED)) {
					Collection<QuotaTypeFormulaLink> quotaTypeFormulaLinks=getQuotaTypeFormulaLinksByQuotaType(quotaTypeId);
					for (QuotaTypeFormulaLink quotaTypeFormulaLink : quotaTypeFormulaLinks) {
						if ((quotaTypeFormulaLink.getQuotaFormula().getId()).equals(quotaFormula.getId())) {
							quotaTypeFormulaLink.setQuotaFormula(null);
							quotaTypeFormulaLink.setQuotaType(null);
							quotaTypeFormulaLink.setFormulaParameters(null);
							session.delete(quotaTypeFormulaLink);
							session.flush();
							session.clear();
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	//当Collection为前台UpdateAction传过来的参数时，务必要保证updateItem中的alias和该函数中的参数名一致
	@DataResolver
	public void saveQuotaFormulaLinkParameters(Collection<FormulaParameter> formulaParameters,String quotaTypeId,String quotaFormulaId){
		Session session=this.getSessionFactory().openSession();
		String hqlString="from "+QuotaTypeFormulaLink.class.getName()+" where quotaType.id='"+quotaTypeId+"'"
				+" and quotaFormula.id='"+quotaFormulaId+"'";
		List<QuotaTypeFormulaLink> quotaTypeFormulaLinks=this.query(hqlString);
		
		if (quotaTypeFormulaLinks.size()>0) {
			QuotaTypeFormulaLink quotaTypeFormulaLink=quotaTypeFormulaLinks.get(0);
			Set<FormulaParameter> thisFormulaParameters=quotaTypeFormulaLink.getFormulaParameters();
			
			try {
				for (FormulaParameter formulaParameter : formulaParameters) {
					EntityState state=EntityUtils.getState(formulaParameter);
					if ((state.equals(EntityState.NEW))||(state.equals(EntityState.NONE))) {
						FormulaParameter thisFormulaParameter=formulaParameterDao.getFormulaParameter(formulaParameter.getId());
						thisFormulaParameters.add(thisFormulaParameter);
					}else if (state.equals(EntityState.DELETED)) {
						for (FormulaParameter formulaParameter2 : thisFormulaParameters) {
							if ((formulaParameter2.getId()).equals(formulaParameter.getId())) {
								thisFormulaParameters.remove(formulaParameter2);
								break;
							}
						}
					}
				}
				quotaTypeFormulaLink.setFormulaParameters(thisFormulaParameters);
				session.merge(quotaTypeFormulaLink);
			} catch (Exception e) {
				System.out.print(e.toString());
			}finally{
				session.flush();
				session.close();
			}
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
