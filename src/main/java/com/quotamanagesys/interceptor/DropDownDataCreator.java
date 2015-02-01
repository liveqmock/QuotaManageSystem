package com.quotamanagesys.interceptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.business.IDept;
import com.bstek.bdf2.core.business.IUser;
import com.bstek.bdf2.core.context.ContextHolder;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.data.variant.Record;
import com.bstek.dorado.web.DoradoContext;
import com.quotamanagesys.dao.QuotaItemViewTableManageDao;
import com.quotamanagesys.dao.QuotaTypeViewMapDao;
import com.quotamanagesys.model.QuotaItemViewTableManage;
import com.quotamanagesys.model.QuotaTypeViewMap;

@Component
public class DropDownDataCreator extends HibernateDao {

	@Resource
	QuotaItemViewTableManageDao quotaItemViewTableManageDao;
	@Resource
	QuotaTypeViewMapDao quotaTypeViewMapDao;
	
	@DataProvider
	public ArrayList<Record> getDropDownData(String columnName) {
		ArrayList<Record> dropDownDataList = new ArrayList<Record>();

		if (columnName==null) {
			return null;
		} else {
			try {
				int year;
				int month;
				String viewscope;
				DoradoContext context = DoradoContext.getCurrent();
				if (context.getAttribute(DoradoContext.VIEW, "year") == null) {
					Calendar calendar = Calendar.getInstance();
					year = calendar.get(Calendar.YEAR);
				} else {
					year = (int) context.getAttribute(DoradoContext.VIEW, "year");
				}
				if (context.getAttribute(DoradoContext.VIEW, "month") == null) {
					Calendar calendar = Calendar.getInstance();
					month = calendar.get(Calendar.MONTH) + 1;// calendar的真实月份需要+1,因为calendar的月份从0开始
				} else {
					month = (int) context.getAttribute(DoradoContext.VIEW, "month");
				}
				if (context.getAttribute(DoradoContext.VIEW, "viewscope") == null) {
					viewscope = "default";
				} else {
					viewscope = (String) context.getAttribute(DoradoContext.VIEW,
							"viewscope");
				}

				QuotaItemViewTableManage quotaItemViewTableManage = quotaItemViewTableManageDao
						.getItemViewTableManageByYear(year);
				String tableName = quotaItemViewTableManage.getTableName();

				IUser loginuser = ContextHolder.getLoginUser();
				String queryString = null;
				if (loginuser.isAdministrator()) {
					switch (month) {
					case 13:
						queryString = "select "+columnName+" from " + tableName
								+ " where 考核频率='月'"+" GROUP BY "+columnName;
						break;
					case 14:
						queryString = "select "+columnName+" from " + tableName
								+ " where 考核频率='年'"+" GROUP BY "+columnName;
						break;
					case 15:
						queryString = "select "+columnName+" from " + tableName+" GROUP BY "+columnName;
						break;
					default:
						queryString = "select "+columnName+" from " + tableName + " where 月度="
								+ month+" GROUP BY "+columnName;
						break;
					}
				} else {
					List<IDept> idepts = loginuser.getDepts();
					String userId = loginuser.getUsername();
					QuotaTypeViewMap quotaTypeViewMap = quotaTypeViewMapDao
							.getQuotaTypeViewMapByUser(userId);
					if (quotaTypeViewMap != null) {
						String quotaTypeViewMapId = quotaTypeViewMap.getId();
						if (viewscope.equals("default")) {
							switch (month) {
							case 13:
								queryString = "select "+columnName+" from "
										+ tableName
										+ " where 考核频率='月' and 指标种类id in (select QUOTA_TYPE_ID from default_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"
										+ quotaTypeViewMapId + "')"+" GROUP BY "+columnName;
								break;
							case 14:
								queryString = "select "+columnName+" from "
										+ tableName
										+ " where 考核频率='年' and 指标种类id in (select QUOTA_TYPE_ID from default_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"
										+ quotaTypeViewMapId + "')"+" GROUP BY "+columnName;
								break;
							case 15:
								queryString = "select "+columnName+" from "
										+ tableName
										+ " where 指标种类id in (select QUOTA_TYPE_ID from default_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"
										+ quotaTypeViewMapId + "')"+" GROUP BY "+columnName;
								break;
							default:
								queryString = "select "+columnName+" from "
										+ tableName
										+ " where 月度="
										+ month
										+ " and 指标种类id in (select QUOTA_TYPE_ID from default_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"
										+ quotaTypeViewMapId + "')"+" GROUP BY "+columnName;
								break;
							}
						} else if (viewscope.equals("can")) {
							switch (month) {
							case 13:
								queryString = "select "+columnName+" from "
										+ tableName
										+ " where 考核频率='月' and 指标种类id in (select QUOTA_TYPE_ID from can_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"
										+ quotaTypeViewMapId + "')"+" GROUP BY "+columnName;
								break;
							case 14:
								queryString = "select "+columnName+" from "
										+ tableName
										+ " where 考核频率='年' and 指标种类id in (select QUOTA_TYPE_ID from can_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"
										+ quotaTypeViewMapId + "')"+" GROUP BY "+columnName;
								break;
							case 15:
								queryString = "select "+columnName+" from "
										+ tableName
										+ " where 指标种类id in (select QUOTA_TYPE_ID from can_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"
										+ quotaTypeViewMapId + "')"+" GROUP BY "+columnName;
								break;
							default:
								queryString = "select "+columnName+" from "
										+ tableName
										+ " where 月度="
										+ month
										+ " and 指标种类id in (select QUOTA_TYPE_ID from can_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"
										+ quotaTypeViewMapId + "')"+" GROUP BY "+columnName;
								break;
							}
						}
					}
				}

				List resultList = getQueryResults(queryString);
				for (Object result : resultList) {
					Map map = (Map) result;
					Record record=new Record();
					record.set("value",map.get(columnName).toString());	
					dropDownDataList.add(record);
				}
			} catch (Exception e) {
				System.out.print(e.toString());
			}
			
			return dropDownDataList;
		}
	}

	public List getQueryResults(String SQL) {
		Session session = this.getSessionFactory().openSession();
		List resultList = null;
		try {
			resultList = session.createSQLQuery(SQL)
					.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
					.list();
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			session.close();
		}
		return resultList;
	}
}
