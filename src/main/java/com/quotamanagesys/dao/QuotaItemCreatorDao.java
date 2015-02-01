package com.quotamanagesys.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.business.IDept;
import com.bstek.bdf2.core.business.IUser;
import com.bstek.bdf2.core.context.ContextHolder;
import com.bstek.bdf2.core.exception.NoneLoginException;
import com.bstek.bdf2.core.model.DefaultDept;
import com.bstek.bdf2.core.orm.ParseResult;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.annotation.Expose;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.bstek.dorado.data.provider.Criteria;
import com.bstek.dorado.data.provider.Page;
import com.quotamanagesys.interceptor.ResultTableCreator;
import com.quotamanagesys.model.FormulaParameter;
import com.quotamanagesys.model.QuotaCover;
import com.quotamanagesys.model.QuotaFormula;
import com.quotamanagesys.model.QuotaItem;
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaProperty;
import com.quotamanagesys.model.QuotaPropertyValue;
import com.quotamanagesys.model.QuotaTargetValue;
import com.quotamanagesys.model.QuotaType;
import com.quotamanagesys.model.QuotaTypeFormulaLink;
import com.quotamanagesys.tools.CriteriaConvertCore;

@Component
public class QuotaItemCreatorDao extends HibernateDao {

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
	@Resource
	QuotaCoverDao quotaCoverDao;
	@Resource
	QuotaTypeFormulaLinkDao quotaTypeFormulaLinkDao;
	@Resource
	DepartmentDao departmentDao;
	@Resource
	ResultTableCreator resultTableCreator;
	@Resource
	CriteriaConvertCore criteriaConvertCore;

	@DataProvider
	public Collection<QuotaItemCreator> getAll() {
		String hqlString = "from " + QuotaItemCreator.class.getName()+" order by quotaType.quotaLevel.level asc,quotaType.name asc,quotaCover.sort asc";
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
				+ " where quotaType.id='" + quotaTypeId + "'"
				+" order by quotaType.quotaLevel.level asc,quotaType.name asc,quotaCover.sort asc";
		Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
		return quotaItemCreators;
	}

	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsByYear(int year) {
		String hqlString = "from " + QuotaItemCreator.class.getName()
				+ " where year=" + year
				+" order by quotaType.quotaLevel.level asc,quotaType.name asc,quotaCover.sort asc";
		Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
		return quotaItemCreators;
	}

	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsByDutyDept(String dutyDeptId){
		String hqlString = "from " + QuotaItemCreator.class.getName()
				+ " where quotaDutyDept.id='" + dutyDeptId + "'"
				+" order by quotaType.quotaLevel.level asc,quotaType.name asc,quotaCover.sort asc";
		Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
		return quotaItemCreators;
	}
	
	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsByManageDept(
			String manageDeptId) throws Exception {
		String hqlString = "from " + QuotaItemCreator.class.getName()
				+ " where quotaType.manageDept.id='" + manageDeptId + "'"
				+" order by quotaType.quotaLevel.level asc,quotaType.name asc,quotaCover.sort asc";
		Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
		/*
		List<QuotaItemCreator> results = new ArrayList<QuotaItemCreator>();
		for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
			Collection<QuotaPropertyValue> quotaPropertyValues = quotaPropertyValueDao
					.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreator.getId());
			String quotaPropertiesNames = "";
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				if (quotaPropertiesNames == "") {
					quotaPropertiesNames = quotaPropertyValue.getQuotaProperty().getName();
				} else {
					quotaPropertiesNames = quotaPropertiesNames + ","+ quotaPropertyValue.getQuotaProperty().getName();
				}
			}
			results.add(quotaItemCreator);
			QuotaItemCreator targetQuotaItemCreator = EntityUtils
					.toEntity(quotaItemCreator);
			EntityUtils.setValue(targetQuotaItemCreator,
					"quotaPropertiesNames", quotaPropertiesNames);
			results.add(targetQuotaItemCreator);
		}
		return results;
		*/
		return quotaItemCreators;
	}
	
	//分页方式查询
	@DataProvider
	public void getQuotaItemCreatorsByManageDeptWithPage(Page<QuotaItemCreator> page,Criteria criteria,String manageDeptId) throws Exception{
		if (manageDeptId!=null) {
			String filterString=criteriaConvertCore.convertToSQLString(criteria);
			if (!filterString.equals("")) {
				filterString=" and ("+filterString+")";
			}
			
			String hqlString = "from " + QuotaItemCreator.class.getName()
					+ " where quotaType.manageDept.id='" + manageDeptId + "'"+filterString
					+" order by quotaType.quotaLevel.level asc,quotaType.name asc,quotaCover.sort asc";
			this.pagingQuery(page, hqlString, "select count(*)" + hqlString);
		} else {
			System.out.print("参数为空");
		}
	}
	
	/*
	@DataProvider
	public void getQuotaItemCreatorsByManageDept(String manageDeptId,Page<QuotaItemCreator> page,Criteria criteria) throws Exception {
		DetachedCriteria dt=this.buildDetachedCriteria(criteria, QuotaItemCreator.class);
		dt.createAlias("quotaType", "quotaType",CriteriaSpecification.INNER_JOIN);
		dt.createAlias("quotaType.manageDept", "manageDept",CriteriaSpecification.INNER_JOIN);
		dt.createAlias("quotaType.quotaLevel", "quotaLevel",CriteriaSpecification.INNER_JOIN);
		dt.createAlias("quotaCover", "quotaCover",CriteriaSpecification.INNER_JOIN);
		dt.add(Restrictions.eq("manageDept.id",manageDeptId));
		//dt.addOrder(Order.asc("quotaLevel.level"));
		//dt.addOrder(Order.asc("quotaType.name"));
		//dt.addOrder(Order.asc("quotaCover.sort"));
		this.pagingQuery(page, dt);
	}
	*/
	
	/*
	@DataProvider
	public void getQuotaItemCreatorsByManageDept(String manageDeptId,Page<QuotaItemCreator> page,Criteria criteria) throws Exception {
		String hqlString = "from " + QuotaItemCreator.class.getName()
				+ " q where q.quotaType.manageDept.id='" + manageDeptId + "'";
		ParseResult result=this.parseCriteria(criteria, true, "q");
		if (result!=null) {
			hqlString += " and "+result.getAssemblySql().toString();
			this.pagingQuery(page, hqlString, "select count(*) "+hqlString,result.getValueMap());
		}else {
			this.pagingQuery(page, hqlString, "select count(*) "+hqlString);
		}
	}
	*/
	
	
	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsByQuotaCover(
			String quotaCoverId) {
		IUser loginUser = ContextHolder.getLoginUser();
		if (loginUser.isAdministrator()) {
			String hqlString = "from " + QuotaItemCreator.class.getName()
					+ " where quotaCover.id='" + quotaCoverId + "'"
					+" order by quotaType.quotaLevel.level asc,quotaType.name asc,quotaCover.sort asc";
			Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
			return quotaItemCreators;
		}else {
			List<IDept> iDepts=loginUser.getDepts();
			
			String hqlString = "from " + QuotaItemCreator.class.getName()
					+ " where quotaCover.id='" + quotaCoverId + "' and quotaType.manageDept.id='"+iDepts.get(0).getId()
					+"' order by quotaType.quotaLevel.level asc,quotaType.name asc,quotaCover.sort asc";
			Collection<QuotaItemCreator> quotaItemCreators = this.query(hqlString);
			return quotaItemCreators;
		}
	}
	
	//获取口径为下一级口径的分解指标
	@DataProvider
	public Collection<QuotaItemCreator> getSplitQuotaItemCreators(String id){
		QuotaItemCreator thisQuotaItemCreator=getQuotaItemCreator(id);
		QuotaCover thisQuotaCover=thisQuotaItemCreator.getQuotaCover();
		QuotaType thisQuotaType=thisQuotaItemCreator.getQuotaType();
		int year=thisQuotaItemCreator.getYear();
		Collection<QuotaCover> childrenQuotaCovers=quotaCoverDao.getQuotaCoversByFatherCover(thisQuotaCover.getId());
		
		Collection<QuotaItemCreator> splitQuotaItemCreators=new ArrayList<QuotaItemCreator>();
		
		for (QuotaCover childQuotaCover : childrenQuotaCovers) {
			String hqlString="from "+QuotaItemCreator.class.getName()+" where quotaCover.id='"+childQuotaCover.getId()+"'"
					+" and year="+year+" and quotaType.id='"+thisQuotaType.getId()+"'"
					+" order by quotaType.quotaLevel.level asc,quotaType.name asc,quotaCover.sort asc";
			Collection<QuotaItemCreator> quotaItemCreators=this.query(hqlString);
			if (quotaItemCreators.size()>0) {
				splitQuotaItemCreators.addAll(quotaItemCreators);
			}
		}
		return splitQuotaItemCreators;
	}
	
	//获取该指标所有分解指标
	@DataProvider
	public Collection<QuotaItemCreator> getSplitQuotaItemCreatorsTree(String id){
		QuotaItemCreator thisQuotaItemCreator=getQuotaItemCreator(id);
		QuotaCover thisQuotaCover=thisQuotaItemCreator.getQuotaCover();
		QuotaType thisQuotaType=thisQuotaItemCreator.getQuotaType();
		int year=thisQuotaItemCreator.getYear();
		Collection<QuotaCover> childrenQuotaCoversTree=quotaCoverDao.getQuotaCoversTreeByFatherCover(thisQuotaCover.getId(),new ArrayList<QuotaCover>());
		
		Collection<QuotaItemCreator> splitQuotaItemCreatorsTree=new ArrayList<QuotaItemCreator>();
		
		for (QuotaCover childQuotaCover : childrenQuotaCoversTree) {
			String hqlString="from "+QuotaItemCreator.class.getName()+" where quotaCover.id='"+childQuotaCover.getId()+"'"
					+" and year="+year+" and quotaType.id='"+thisQuotaType.getId()+"'"
					+" order by quotaType.quotaLevel.level asc,quotaType.name asc,quotaCover.sort asc";
			Collection<QuotaItemCreator> quotaItemCreators=this.query(hqlString);
			if (quotaItemCreators.size()>0) {
				splitQuotaItemCreatorsTree.addAll(quotaItemCreators);
			}
		}
		return splitQuotaItemCreatorsTree;
	}

	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsByFormula(
			String quotaFormulaId) throws Exception {
		Session session = this.getSessionFactory().openSession();
		List<QuotaItemCreator> quotaItemCreators = session
				.createCriteria(QuotaItemCreator.class)
				.createAlias("quotaFormulas", "q")
				.add(Restrictions.eq("q.id", quotaFormulaId)).list();
		session.flush();
		session.close();
		List<QuotaItemCreator> results = new ArrayList<QuotaItemCreator>();
		for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
			Collection<QuotaPropertyValue> quotaPropertyValues = quotaPropertyValueDao
					.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreator
							.getId());
			String quotaPropertiesNames = "";
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				if (quotaPropertiesNames == "") {
					quotaPropertiesNames = quotaPropertyValue
							.getQuotaProperty().getName();
				} else {
					quotaPropertiesNames = quotaPropertiesNames + ","
							+ quotaPropertyValue.getQuotaProperty().getName();
				}
			}
			QuotaItemCreator targetQuotaItemCreator = EntityUtils
					.toEntity(quotaItemCreator);
			EntityUtils.setValue(targetQuotaItemCreator,
					"quotaPropertiesNames", quotaPropertiesNames);
			results.add(targetQuotaItemCreator);
		}
		return results;
	}
	
	//获取尚未添加属性目标值的指标生成器（根据登陆用户所属部门）
	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsWithoutQuotaPropertyValuesByLoginUserDept() throws Exception{
		Collection<QuotaItemCreator> quotaItemCreators;
		IUser loginUser = ContextHolder.getLoginUser();
		if (loginUser.isAdministrator()) {
			quotaItemCreators=getAll();
		}else {
			List<IDept> iDepts=loginUser.getDepts();
			quotaItemCreators=getQuotaItemCreatorsByManageDept(iDepts.get(0).getId());
		}
		
		Collection<QuotaItemCreator> quotaItemCreatorsWithoutPropertyValues=new ArrayList<QuotaItemCreator>();
		
		for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
			Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreator.getId());
			if (quotaPropertyValues.size()>0) {
				continue;
			}else {
				quotaItemCreatorsWithoutPropertyValues.add(quotaItemCreator);
			}
		}
		return quotaItemCreatorsWithoutPropertyValues;
	}
	
	//获取尚未添加属性目标值的指标生成器（根据管理部门）
	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsWithoutQuotaPropertyValuesByManageDept(String manageDeptId) throws Exception{
		Collection<QuotaItemCreator> quotaItemCreators=getQuotaItemCreatorsByManageDept(manageDeptId);
		Collection<QuotaItemCreator> quotaItemCreatorsWithoutPropertyValues=new ArrayList<QuotaItemCreator>();
		
		for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
			Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreator.getId());
			if (quotaPropertyValues.size()>0) {
				continue;
			}else {
				quotaItemCreatorsWithoutPropertyValues.add(quotaItemCreator);
			}
		}
		return quotaItemCreatorsWithoutPropertyValues;
	}

	//根据指标种类库中的当前在用指标初始化当年口径为顶级口径的指标生成器
	@Expose
	public void createQuotaItemCreatorsByTopCover(){
		QuotaCover topQuotaCover=quotaCoverDao.getTopQuotaCovers().get(0);
		Collection<QuotaType> quotaTypesInUsed=quotaTypeDao.getQuotaTypesInUsedByLoginUserDept();
		
		Calendar calendar=Calendar.getInstance();	
		//获取执行时的年月
		int year=calendar.get(Calendar.YEAR);
		int month=calendar.get(Calendar.MONTH)+1;//calendar的真实月份需要+1,因为calendar的月份从0开始
		
		//当年从2月开始，才可执行上一年度指标建立操作
		if (month>1) {
			Session session=this.getSessionFactory().openSession();
			try {
				for (QuotaType quotaTypeInUsed : quotaTypesInUsed) {
					createQuotaItemCreator(quotaTypeInUsed,topQuotaCover,quotaTypeInUsed.getManageDept(),year,session);
				}
			} catch (Exception e) {
				System.out.print(e.toString());
			}finally{
				session.flush();
				session.close();
			}
		}
	}
	
	//根据选择的口径建立指标生成器
	@Expose
	public void createChildrenQuotaItemCreatorsByQuotaCovers(String quotaItemCreatorId,Collection<QuotaCover> quotaCovers){
		Session session=this.getSessionFactory().openSession();
		QuotaItemCreator quotaItemCreator=getQuotaItemCreator(quotaItemCreatorId);
		QuotaType quotaType=quotaItemCreator.getQuotaType();
		int year=quotaItemCreator.getYear();
		
		try {
			for (QuotaCover quotaCover : quotaCovers) {
				QuotaCover thisQuotaCover=quotaCoverDao.getQuotaCover(quotaCover.getId());
				Collection<DefaultDept> dutyDepts=departmentDao.getDutyDeptsByQuotaCover(thisQuotaCover.getId());
				DefaultDept dutyDept=null;
				for (DefaultDept defaultDept : dutyDepts) {
					dutyDept=defaultDept;
					break;
				}
				createQuotaItemCreator(quotaType, thisQuotaCover, dutyDept, year, session);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	void createQuotaItemCreator(QuotaType quotaType,QuotaCover quotaCover,DefaultDept quotaDutyDept,int year,Session session){
		boolean isExists=false;
		String checkString="from "+QuotaItemCreator.class.getName()+" where quotaType.id='"+quotaType.getId()
				+"' and year="+year+" and quotaCover.id='"+quotaCover.getId()+"'";
		Collection<QuotaItemCreator> checkout=this.query(checkString);
		if (checkout.size()>0) {
			isExists=true;
		}
		if (!isExists) {
			QuotaItemCreator quotaItemCreator=new QuotaItemCreator();
			quotaItemCreator.setName(quotaType.getName());
			quotaItemCreator.setYear(year);
			quotaItemCreator.setQuotaType(quotaType);
			quotaItemCreator.setQuotaCover(quotaCover);
			quotaItemCreator.setQuotaDutyDept(quotaDutyDept);
			session.merge(quotaItemCreator);
			session.flush();
			session.clear();
		}
	}
	
	//根据指标生成器生成具体指标
	@Expose
	public void createQuotaItemsByManageDept(String manageDeptId) throws Exception{
		Session session = this.getSessionFactory().openSession();
		Collection<QuotaItemCreator> quotaItemCreators=getQuotaItemCreatorsByManageDept(manageDeptId);
		
		try {
			for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {	
				Collection<QuotaItem> quotaItems = quotaItemDao.getQuotaItemsByQuotaItemCreator(quotaItemCreator.getId());
				if (quotaItems.size() == 0) {
					String rate = quotaItemCreator.getQuotaType().getRate();
					switch (rate) {
					case "年": {
						QuotaItem quotaItem = new QuotaItem();
						quotaItem.setYear(quotaItemCreator.getYear());
						quotaItem.setQuotaItemCreator(quotaItemCreator);
						quotaItem.setOverTime(false);
						quotaItem.setAllowSubmit(false);
						session.save(quotaItem);
						session.flush();
						session.clear();
						break;
					}
					case "月": {
						Collection<QuotaPropertyValue> quotaPropertyValues = quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreator.getId());
						int monthCount = 12;
						for (int i = 1; i <= monthCount; i++) {
							QuotaItem quotaItem = new QuotaItem();
							quotaItem.setYear(quotaItemCreator.getYear());
							quotaItem.setMonth(i);
							quotaItem.setQuotaItemCreator(quotaItemCreator);
							quotaItem.setOverTime(false);
							quotaItem.setAllowSubmit(false);
							session.save(quotaItem);
							session.flush();
							session.clear();
							for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
								QuotaTargetValue quotaTargetValue = new QuotaTargetValue();
								quotaTargetValue.setQuotaItem(quotaItem);
								quotaTargetValue.setQuotaProperty(quotaPropertyValue.getQuotaProperty());
								quotaTargetValue.setParameterName(quotaPropertyValue.getQuotaProperty().getParameterName()+"_M");
								session.save(quotaTargetValue);
								session.flush();//作用为将session缓存中的数据与数据库同步
								session.clear();//作用就是清除session缓存中的数据，避免实例冲突（session不能有两个相同的实例）
							}
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
	
	//将属性复制到下级指标
	@Expose
	public void copyQuotaPropertiesToChildren(String quotaItemCreatorId){
		Session session=this.getSessionFactory().openSession();
		Collection<QuotaItemCreator> childrenQuotaItemCreatorsTree=getSplitQuotaItemCreatorsTree(quotaItemCreatorId);
		Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreatorId);
		if (quotaPropertyValues.size()>0) {
			Collection<QuotaProperty> quotaProperties=new ArrayList<QuotaProperty>();
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				quotaProperties.add(quotaPropertyValue.getQuotaProperty());
			}
			
			try {
				for (QuotaItemCreator childQuotaItemCreator : childrenQuotaItemCreatorsTree) {
					Collection<QuotaPropertyValue> quotaPropertyValues2=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(childQuotaItemCreator.getId());
					if (quotaPropertyValues2.size()>0) {
						quotaPropertyValueDao.deleteQuotaPropertyValues(quotaPropertyValues2);
					}
					
					for (QuotaProperty quotaProperty : quotaProperties) {
						QuotaPropertyValue quotaPropertyValue=new QuotaPropertyValue();
						quotaPropertyValue.setQuotaProperty(quotaProperty);
						quotaPropertyValue.setQuotaItemCreator(childQuotaItemCreator);
						session.save(quotaPropertyValue);
						session.flush();
						session.clear();
					}
					linkQuotaFormulas(childQuotaItemCreator.getId());
				}
			} catch (Exception e) {
				System.out.print(e.toString());
			}finally{
				session.flush();
				session.close();
			}
		}	
	}

	@DataResolver
	public void saveQuotaItemCreators(Collection<QuotaItemCreator> quotaItemCreators) {
		Session session = this.getSessionFactory().openSession();
		try {
			for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
				EntityState state = EntityUtils.getState(quotaItemCreator);
				QuotaType quotaType = quotaTypeDao.getQuotaType(quotaItemCreator.getQuotaType().getId());
				if (state.equals(EntityState.NEW)) {
					boolean isExists=false;
					String checkString="from "+QuotaItemCreator.class.getName()+" where quotaType.id='"+quotaType.getId()
							+"' and year="+quotaItemCreator.getYear()+" and quotaCover.id='"+quotaItemCreator.getQuotaCover().getId()+"'";
					Collection<QuotaItemCreator> checkout=this.query(checkString);
					if (checkout.size()>0) {
						isExists=true;
					}
					if (!isExists) {
						quotaItemCreator.setName(quotaType.getName());
						quotaItemCreator.setQuotaType(quotaType);
						quotaItemCreator.setQuotaCover(quotaCoverDao.getQuotaCover(quotaItemCreator.getQuotaCover().getId()));
						session.merge(quotaItemCreator);
						session.flush();
					}		
				} else if (state.equals(EntityState.MODIFIED)) {
					QuotaItemCreator thisQuotaItemCreator=getQuotaItemCreator(quotaItemCreator.getId());
					thisQuotaItemCreator.setQuotaType(quotaType);
					thisQuotaItemCreator.setName(quotaType.getName());
					thisQuotaItemCreator.setQuotaCover(quotaCoverDao.getQuotaCover(quotaItemCreator.getQuotaCover().getId()));
					thisQuotaItemCreator.setQuotaDutyDept(departmentDao.getDept(quotaItemCreator.getQuotaDutyDept().getId()));
					thisQuotaItemCreator.setYear(quotaItemCreator.getYear());
					session.merge(thisQuotaItemCreator);
					session.flush();
					
					Collection<QuotaItem> quotaItems=quotaItemDao.getQuotaItemsByQuotaItemCreator(quotaItemCreator.getId());
					resultTableCreator.createOrUpdateResultTable(quotaItems);
				} else if (state.equals(EntityState.DELETED)) {
					QuotaItemCreator thisQuotaItemCreator=getQuotaItemCreator(quotaItemCreator.getId());
					if (thisQuotaItemCreator!=null) {
						Collection<QuotaItemCreator> childrenQuotaItemCreatorsTree=getSplitQuotaItemCreatorsTree(quotaItemCreator.getId());
						if (childrenQuotaItemCreatorsTree.size()>0) {
							for (QuotaItemCreator childQuotaItemCreator : childrenQuotaItemCreatorsTree) {
								deleteQuotaItemCreatorWithCasecade(childQuotaItemCreator, session);
							}
						}
						deleteQuotaItemCreatorWithCasecade(thisQuotaItemCreator, session);
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
	
	//复制上年度指标生成器
	@Expose
	public void copyQuotaItemCreatorsFromLastYear(){
		IUser loginuser = ContextHolder.getLoginUser();
		if (loginuser == null) {
			throw new NoneLoginException("Please login first!");
		}else {
			boolean isAdmin=loginuser.isAdministrator();
			if (isAdmin) {
				Calendar calendar=Calendar.getInstance();
				//获取执行时的年月
				int year=calendar.get(Calendar.YEAR);
				int month=calendar.get(Calendar.MONTH)+1;//calendar的真实月份需要+1,因为calendar的月份从0开始
				
				//当年从2月开始，才可执行上一年度指标复制操作
				if (month>1){
					Collection<QuotaItemCreator> quotaItemCreators=getQuotaItemCreatorsByYear(year-1);
					Session session=this.getSessionFactory().openSession();
					try {
						for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
							//QuotaItemCreator设置year为当前年度
							quotaItemCreator.setYear(year);
							session.merge(quotaItemCreator);
							session.flush();
							session.clear();
							
							//QuotaItem设置year为当前年度
							Collection<QuotaItem> quotaItems=quotaItemDao.getQuotaItemsByQuotaItemCreator(quotaItemCreator.getId());
							for (QuotaItem quotaItem : quotaItems) {
								quotaItem.setYear(year);
								quotaItem.setFinishValue(null);
								quotaItem.setAccumulateValue(null);
								quotaItem.setSameTermValue(null);
								quotaItem.setSameTermAccumulateValue(null);
								quotaItem.setFirstSubmitTime(null);
								quotaItem.setLastSubmitTime(null);
								quotaItem.setRedLightReason(null);
								quotaItem.setOverTime(false);
								quotaItem.setAllowSubmit(false);
								session.merge(quotaItem);
								session.flush();
								session.clear();
							}
						}
					} catch (Exception e) {
						System.out.print(e.toString());
					}finally{
						session.flush();
						session.clear();
					}
				}
			}else {
				System.out.print("非管理员权限不得执行此操作");
			}
		}
	}
	
	@DataResolver
	public void deleteQuotaItemCreators(Collection<QuotaItemCreator> quotaItemCreators) {
		Session session = this.getSessionFactory().openSession();
		try {
			for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
				deleteQuotaItemCreatorWithCasecade(quotaItemCreator, session);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		} finally {
			session.flush();
			session.close();
		}
	}
	
	//级联删除指标生成器
	@DataResolver
	public void deleteQuotaItemCreatorWithCasecade(QuotaItemCreator quotaItemCreator,Session session){
		//级联删除QuotaPropertyValue
		Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreator.getId());
		quotaPropertyValueDao.deleteQuotaPropertyValues(quotaPropertyValues);
		
		//级联删除QuotaItem
		Collection<QuotaItem> quotaItems=quotaItemDao.getQuotaItemsByQuotaItemCreator(quotaItemCreator.getId());
		quotaItemDao.deleteQuotaItems(quotaItems);
		
		quotaItemCreator.setQuotaCover(null);
		quotaItemCreator.setQuotaDutyDept(null);
		quotaItemCreator.setQuotaType(null);
		quotaItemCreator.setQuotaFormulas(null);
		session.delete(quotaItemCreator);
		session.flush();
		session.clear();
	}
	
	//清除上年度指标生成器（只允许管理员清理）
	@Expose
	public void clearQuotaItemCreatorsLastYear(){
		IUser loginuser = ContextHolder.getLoginUser();
		if (loginuser == null) {
			throw new NoneLoginException("Please login first!");
		}else {
			boolean isAdmin=loginuser.isAdministrator();
			if (isAdmin) {
				Calendar calendar=Calendar.getInstance();
				//获取执行时的年月
				int year=calendar.get(Calendar.YEAR);
				int month=calendar.get(Calendar.MONTH)+1;//calendar的真实月份需要+1,因为calendar的月份从0开始
				
				//当年从2月开始，才可执行上一年度指标删除操作
				if (month>1){
					Collection<QuotaItemCreator> quotaItemCreators=getQuotaItemCreatorsByYear(year-1);
					deleteQuotaItemCreators(quotaItemCreators);
				}
			}else {
				System.out.print("非管理员权限不得执行此操作");
			}
		}
	}
	
	//通过指标种类将公式关联到具体指标生成器
	@Expose
	public void linkQuotaFormulas(String quotaItemCreatorId){
		Session session=this.getSessionFactory().openSession();
		try {
			QuotaItemCreator quotaItemCreator=getQuotaItemCreator(quotaItemCreatorId);
			QuotaType quotaType=quotaItemCreator.getQuotaType();
			Collection<QuotaTypeFormulaLink> quotaTypeFormulaLinks=quotaTypeFormulaLinkDao.getQuotaTypeFormulaLinksByQuotaType(quotaType.getId());
			
			Collection<QuotaPropertyValue> thisQuotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItemCreator.getId());
			Set thisParameterSet=new HashSet();
			
			for (QuotaPropertyValue quotaPropertyValue : thisQuotaPropertyValues) {
				thisParameterSet.add(quotaPropertyValue.getQuotaProperty().getParameterName());
				if (quotaType.getRate().equals("月")) {
					thisParameterSet.add(quotaPropertyValue.getQuotaProperty().getParameterName()+"_M");
				}
			}
			
			quotaItemCreator.setQuotaFormulas(null);
			session.merge(quotaItemCreator);
			session.flush();
			session.clear();
			
			Set<QuotaFormula> formulas = new HashSet<QuotaFormula>();
			
			for (QuotaTypeFormulaLink quotaTypeFormulaLink : quotaTypeFormulaLinks) {
				Collection<FormulaParameter> formulaParameters=quotaTypeFormulaLink.getFormulaParameters();
				Set formulaLinkParameterSet=new HashSet();
				for (FormulaParameter formulaParameter : formulaParameters) {
					formulaLinkParameterSet.add(formulaParameter.getParameterName());
				}
				if (thisParameterSet.containsAll(formulaLinkParameterSet)) {
					formulas.add(quotaTypeFormulaLink.getQuotaFormula());
				}
			}
			
			quotaItemCreator.setQuotaFormulas(formulas);
			session.merge(quotaItemCreator);
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
		
	}
	
	//指标生成器按管理部门关联计算公式
	@Expose
	public void linkQuotaFormulasByManageDept(String manageDeptId) throws Exception{
		Collection<QuotaItemCreator> quotaItemCreators=getQuotaItemCreatorsByManageDept(manageDeptId);
		for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
			linkQuotaFormulas(quotaItemCreator.getId());
		}
	}
	
	//获取未完成指标初始化的部门 
	@DataProvider
	public Collection<DefaultDept> getManageDeptsNotFinishInitQuotaItemCreators() throws Exception{
		Collection<DefaultDept> manageDepts=departmentDao.getAll();
		Collection<DefaultDept> deptsNotFinishInitQuotaItemCreators=new ArrayList<DefaultDept>();
		for (DefaultDept manageDept : manageDepts) {
			if (getQuotaItemCreatorsWithoutQuotaPropertyValuesByManageDept(manageDept.getId()).size()>0) {
				deptsNotFinishInitQuotaItemCreators.add(manageDept);
			}
		}
		return deptsNotFinishInitQuotaItemCreators;
	}
	
	//获取未关联公式的指标生成器
	@DataProvider
	public Collection<QuotaItemCreator> getQuotaItemCreatorsWithoutFormulas(){
		Collection<QuotaItemCreator> quotaItemCreatorsWithoutFormulas=new ArrayList<QuotaItemCreator>();
		Collection<QuotaItemCreator> quotaItemCreators=getAll();
		for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
			if (quotaItemCreator.getQuotaFormulas().size()==0) {
				quotaItemCreatorsWithoutFormulas.add(quotaItemCreator);
			}
		}
		return quotaItemCreatorsWithoutFormulas;
	}
	
	//将指标生成器名称更改为与指标种类名称一致
	@Expose
	public void repairQuotaItemCreatorsNames(){
		Session session=this.getSessionFactory().openSession();
		try {
			Collection<QuotaItemCreator> quotaItemCreators=getAll();
			for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
				if (quotaItemCreator.getName().equals(quotaItemCreator.getQuotaType().getName())) {
					continue;
				} else {
					quotaItemCreator.setName(quotaItemCreator.getQuotaType().getName());
					session.merge(quotaItemCreator);
					session.flush();
				}
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
		
	}
	
	//如果指标生成器口径为全口径、母公司、县公司，则责任部门=管理部门（临时方法，以后完善代码后撤销）
	@Expose
	public void repairQuotaItemDutyDept(){
		Session session=this.getSessionFactory().openSession();
		try {
			String hqlString="from "+QuotaItemCreator.class.getName()+" where quotaCover.name='母公司' or quotaCover.name='县公司'";
			Collection<QuotaItemCreator> quotaItemCreators=this.query(hqlString);
			
			for (QuotaItemCreator quotaItemCreator : quotaItemCreators) {
				QuotaItemCreator thisQuotaItemCreator=getQuotaItemCreator(quotaItemCreator.getId());
				thisQuotaItemCreator.setQuotaDutyDept(quotaItemCreator.getQuotaType().getManageDept());
				session.merge(thisQuotaItemCreator);
				session.flush();
				
				Collection<QuotaItem> quotaItems=quotaItemDao.getQuotaItemsByQuotaItemCreator(quotaItemCreator.getId());
				resultTableCreator.createOrUpdateResultTable(quotaItems);
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
