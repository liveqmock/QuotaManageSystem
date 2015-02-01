package com.quotamanagesys.interceptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.business.IDept;
import com.bstek.bdf2.core.business.IUser;
import com.bstek.bdf2.core.context.ContextHolder;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.data.provider.Criteria;
import com.bstek.dorado.data.provider.Page;
import com.bstek.dorado.data.provider.manager.DataProviderManager;
import com.bstek.dorado.data.type.EntityDataType;
import com.bstek.dorado.data.type.property.BasePropertyDef;
import com.bstek.dorado.data.type.property.PropertyDef;
import com.bstek.dorado.data.variant.Record;
import com.bstek.dorado.view.manager.ViewConfig;
import com.bstek.dorado.view.widget.Align;
import com.bstek.dorado.view.widget.grid.ColumnGroup;
import com.bstek.dorado.view.widget.grid.DataColumn;
import com.bstek.dorado.view.widget.grid.DataGrid;
import com.bstek.dorado.view.widget.grid.RowNumColumn;
import com.bstek.dorado.view.widget.grid.StretchColumnsMode;
import com.bstek.dorado.view.widget.treegrid.TreeGrid;
import com.bstek.dorado.web.DoradoContext;
import com.quotamanagesys.dao.DepartmentDao;
import com.quotamanagesys.dao.QuotaItemViewTableManageDao;
import com.quotamanagesys.dao.QuotaTypeViewMapDao;
import com.quotamanagesys.dao.ShowColumnDao;
import com.quotamanagesys.dao.ShowColumnGroupDao;
import com.quotamanagesys.model.QuotaItem;
import com.quotamanagesys.model.QuotaItemViewTableManage;
import com.quotamanagesys.model.QuotaTypeViewMap;
import com.quotamanagesys.model.ShowColumn;
import com.quotamanagesys.model.ShowColumnGroup;
import com.quotamanagesys.model.ShowColumnTrigger;
import com.quotamanagesys.tools.CriteriaConvertCore;

@Component
public class ResultGridCreator extends HibernateDao{
	@Resource
	DepartmentDao departmentDao;
	@Resource
	ShowColumnDao showColumnDao;
	@Resource
	ShowColumnGroupDao showColumnGroupDao;
	@Resource
	QuotaTypeViewMapDao quotaTypeViewMapDao;
	@Resource
	QuotaItemViewTableManageDao quotaItemViewTableManageDao;
	@Resource
	CriteriaConvertCore criteriaConvertCore;
	
	@Autowired
	@Qualifier("dorado.dataProviderManager")
	private DataProviderManager dataProviderManager;
	
	public void ViewConfigInit(ViewConfig viewConfig) throws Exception{
		PropertyDef propertyDef;
		EntityDataType quotaItemStatus = (EntityDataType) viewConfig.getDataType("QuotaItemStatus");
		
		int year;
		int month;
		DoradoContext context = DoradoContext.getCurrent();
		if (context.getAttribute(DoradoContext.VIEW, "year")==null) {
			Calendar calendar=Calendar.getInstance();	
			year=calendar.get(Calendar.YEAR);
		} else {
			year=(int) context.getAttribute(DoradoContext.VIEW, "year");
		}
		if (context.getAttribute(DoradoContext.VIEW, "month")==null) {
			Calendar calendar=Calendar.getInstance();	
			//默认显示上月数据
			month=calendar.get(Calendar.MONTH);//calendar的真实月份需要+1,因为calendar的月份从0开始
			if (month==0) {
				year=year-1;
				month=12;
			}
		} else {
			month=(int) context.getAttribute(DoradoContext.VIEW, "month");
		}

		System.out.print(year+","+month);
		
		Connection conn=getDBConnection();
		ResultSet rs=null;
		ResultSetMetaData rsm = null;
		boolean isSuccess=true;

		try {
			QuotaItemViewTableManage quotaItemViewTableManage=quotaItemViewTableManageDao.getItemViewTableManageByYear(year);
			if (quotaItemViewTableManage!=null) {
				rs=getResultSet(conn,"select * from "+quotaItemViewTableManage.getTableName());
				rsm=rs.getMetaData();
			}else {
				isSuccess=false;
				System.out.print("无该年度数据："+year);
			}
		} catch (Exception e) {
			isSuccess=false;
		}
		
		if (isSuccess) {
			for (int i = 1; i <=rsm.getColumnCount(); i++) {
	        	String columnName=rsm.getColumnName(i);
	        	propertyDef = new BasePropertyDef(columnName);
	        	if (columnName.equals("年度")||columnName.equals("月度")) {
	        		propertyDef.setDataType(viewConfig.getDataType("int"));
				}else if (columnName.equals("填报超时")||columnName.equals("提交状态")) {
					propertyDef.setDataType(viewConfig.getDataType("boolean"));
				}else{
					propertyDef.setDataType(viewConfig.getDataType("String"));
				}
	    		propertyDef.setLabel(columnName);
	    		propertyDef.setReadOnly(true);
	    		quotaItemStatus.addPropertyDef(propertyDef);
			}
		}
        conn.close();
	}
	
	public void DataGridInit(DataGrid dgQuotaItemStatus) throws SQLException{		
		int year;
		int month;
		DoradoContext context = DoradoContext.getCurrent();
		if (context.getAttribute(DoradoContext.VIEW, "year")==null) {
			Calendar calendar=Calendar.getInstance();	
			year=calendar.get(Calendar.YEAR);
		} else {
			year=(int) context.getAttribute(DoradoContext.VIEW, "year");
		}
		if (context.getAttribute(DoradoContext.VIEW, "month")==null) {
			Calendar calendar=Calendar.getInstance();	
			//默认显示上月数据
			month=calendar.get(Calendar.MONTH);//calendar的真实月份需要+1,因为calendar的月份从0开始
			if (month==0) {
				year=year-1;
				month=12;
			}
		} else {
			month=(int) context.getAttribute(DoradoContext.VIEW, "month");
		}

		QuotaItemViewTableManage quotaItemViewTableManage=quotaItemViewTableManageDao.getItemViewTableManageByYear(year);
		if (quotaItemViewTableManage!=null) {
			Collection<ShowColumn> showColumnsNotYetLinked=showColumnDao.getShowColumnsNotYetLinkedByQuotaItemViewTableManage(quotaItemViewTableManage.getId());
			RowNumColumn rowNumColumn=new RowNumColumn();
			rowNumColumn.setWidth("35");
			dgQuotaItemStatus.addColumn(rowNumColumn);
			
			if (showColumnsNotYetLinked.size()>0) {
				for (ShowColumn showColumn : showColumnsNotYetLinked) {
					DataColumn dataColumn=new DataColumn();
					dataColumn.setName(showColumn.getName());
					dataColumn.setProperty(showColumn.getName());
					dataColumn.setCaption(showColumn.getAlias());
					dataColumn.setWidth(showColumn.getWidth()+"");
					dataColumn.setVisible(showColumn.getVisible());
					dataColumn.setWrappable(showColumn.getWrappable());
					setDataColumTrigger(showColumn,dataColumn);

					String align=showColumn.getAlign();
					switch (align) {
					case "left":
						dataColumn.setAlign(Align.left);
						break;
					case "center":
						dataColumn.setAlign(Align.center);
						break;
					case "right":
						dataColumn.setAlign(Align.right);
						break;
					default:
						dataColumn.setAlign(Align.left);
						break;
					}
					
					if (showColumn.getRender()!=null) {
						ClinetEventImp clinetEventImp=new ClinetEventImp();
						clinetEventImp.setScript(showColumn.getRender().getRenderCode());
						dataColumn.addClientEventListener("onRenderCell",clinetEventImp);
					}
					
					dgQuotaItemStatus.addColumn(dataColumn);
				}
			}
			
			//获取顶层列分组
			Collection<ShowColumnGroup> topShowColumnGroups=showColumnGroupDao.getTopLevelShowColumnGroupsByQuotaItemViewTableManage(quotaItemViewTableManage.getId());
			if (topShowColumnGroups.size()>0) {
				for (ShowColumnGroup topShowColumnGroup : topShowColumnGroups) {
					ColumnGroup columnGroup=new ColumnGroup();
					initColumnGroupsContent(topShowColumnGroup, columnGroup);
					dgQuotaItemStatus.addColumn(columnGroup);
				}
			}
			
			dgQuotaItemStatus.setShowFilterBar(true);
			dgQuotaItemStatus.setDataSet("dsQuotaItemStatus");
			dgQuotaItemStatus.setStretchColumnsMode(StretchColumnsMode.off);
			dgQuotaItemStatus.setDynaRowHeight(false);
			dgQuotaItemStatus.setFixedColumnCount(2);
		} else {
			System.out.print("无该年度数据："+year);
		}
		
	}
	
	void initColumnGroupsContent(ShowColumnGroup thisShowColumnGroup,ColumnGroup columnGroup){
		columnGroup.setCaption(thisShowColumnGroup.getName());

		Collection<ShowColumn> showColumns=showColumnDao.getShowColumnsByGroup(thisShowColumnGroup.getId());
		for (ShowColumn showColumn : showColumns) {
			DataColumn dataColumn=new DataColumn();
			dataColumn.setName(showColumn.getName());
			dataColumn.setProperty(showColumn.getName());
			dataColumn.setCaption(showColumn.getAlias());
			dataColumn.setWidth(showColumn.getWidth()+"");
			dataColumn.setVisible(showColumn.getVisible());
			dataColumn.setWrappable(showColumn.getWrappable());
			setDataColumTrigger(showColumn,dataColumn);
			
			String align=showColumn.getAlign();
			switch (align) {
			case "left":
				dataColumn.setAlign(Align.left);
				break;
			case "center":
				dataColumn.setAlign(Align.center);
				break;
			case "right":
				dataColumn.setAlign(Align.right);
				break;
			default:
				dataColumn.setAlign(Align.left);
				break;
			}
			
			if (showColumn.getRender()!=null) {
				ClinetEventImp clinetEventImp=new ClinetEventImp();
				clinetEventImp.setScript(showColumn.getRender().getRenderCode());
				dataColumn.addClientEventListener("onRenderCell",clinetEventImp);
			}

			columnGroup.addColumn(dataColumn);
		}
		
		Collection<ShowColumnGroup> childShowColumnGroups=showColumnGroupDao.getChildrenShowColumnGroups(thisShowColumnGroup.getId());
		for (ShowColumnGroup childShowColumnGroup : childShowColumnGroups) {
			ColumnGroup childColumnGroup=new ColumnGroup();
			childColumnGroup.setCaption(childShowColumnGroup.getName());
			columnGroup.addColumn(childColumnGroup);
			initColumnGroupsContent(childShowColumnGroup, childColumnGroup);
		}
	}
	
	void setDataColumTrigger(ShowColumn showColumn,DataColumn dataColumn){
		ShowColumnTrigger showColumnTrigger=showColumn.getShowColumnTrigger();
		if (showColumnTrigger!=null) {
			dataColumn.setTrigger(showColumnTrigger.getName());
		}
	}
	
	@DataProvider
	public void getQuotaItemStatusRecord(Page<Record> page,Criteria criteria) throws SQLException{
		if (page!=null) {
			ArrayList<Record> quotaItemStatusRecords=new ArrayList<Record>();
			int pageSize=page.getPageSize();
			int pageNo=page.getPageNo();
			
			int firstIndex=(pageNo-1)*pageSize;
			int lastIndex=pageNo*pageSize;
			
			String filterString=criteriaConvertCore.convertToSQLString(criteria);
			if (!filterString.equals("")) {
				filterString=" and ("+filterString+")";
			}
			
			Connection conn=getDBConnection();
			try {
				int year;
				int month;
				String viewscope;
				DoradoContext context = DoradoContext.getCurrent();
				
				if (context.getAttribute(DoradoContext.VIEW, "year")==null) {
					Calendar calendar=Calendar.getInstance();	
					year=calendar.get(Calendar.YEAR);
				} else {
					year=(int) context.getAttribute(DoradoContext.VIEW, "year");
				}
				if (context.getAttribute(DoradoContext.VIEW, "month")==null) {
					Calendar calendar=Calendar.getInstance();	
					//默认显示上月数据
					month=calendar.get(Calendar.MONTH);//calendar的真实月份需要+1,因为calendar的月份从0开始
					if (month==0) {
						year=year-1;
						month=12;
					}
				} else {
					month=(int) context.getAttribute(DoradoContext.VIEW, "month");
				}
				
				if (context.getAttribute(DoradoContext.VIEW, "viewscope")==null) {
					viewscope="default";
				} else {
					viewscope=(String)context.getAttribute(DoradoContext.VIEW, "viewscope");
				}
				
				QuotaItemViewTableManage quotaItemViewTableManage=quotaItemViewTableManageDao.getItemViewTableManageByYear(year);
				if (quotaItemViewTableManage!=null) {
					String tableName=quotaItemViewTableManage.getTableName();
					ResultSet rs=getResultSet(conn,"select * from "+tableName);
					ResultSetMetaData rsm=rs.getMetaData();
					
					IUser loginuser = ContextHolder.getLoginUser();
					String queryString = null;
					if (loginuser.isAdministrator()) {
						switch (month) {
						case 13:
							queryString="select * from "+tableName+" where 考核频率='月'"+filterString;
							break;
						case 14:
							queryString="select * from "+tableName+" where 考核频率='年'"+filterString;
							break;
						case 15:
							if (filterString.equals("")) {
								queryString="select * from "+tableName+filterString;
							}else{
								queryString="select * from "+tableName+" where 考核频率 like '%%'"+filterString;
							}
							break;
						default:
							queryString="select * from "+tableName+" where 月度="+month+filterString;
							break;
						}
					}else {
						List<IDept> idepts=loginuser.getDepts();
						String userId=loginuser.getUsername();
						QuotaTypeViewMap quotaTypeViewMap=quotaTypeViewMapDao.getQuotaTypeViewMapByUser(userId);
						if (quotaTypeViewMap!=null) {
							String quotaTypeViewMapId=quotaTypeViewMap.getId();
							if (viewscope.equals("default")) {
								switch (month) {
								case 13:
									queryString="select * from "+tableName+" where 考核频率='月' and 指标种类id in (select QUOTA_TYPE_ID from default_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"+quotaTypeViewMapId+"')"+filterString;
									break;
								case 14:
									queryString="select * from "+tableName+" where 考核频率='年' and 指标种类id in (select QUOTA_TYPE_ID from default_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"+quotaTypeViewMapId+"')"+filterString;
									break;
								case 15:
									queryString="select * from "+tableName+" where 指标种类id in (select QUOTA_TYPE_ID from default_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"+quotaTypeViewMapId+"')"+filterString;
									break;
								default:
									queryString="select * from "+tableName+" where 月度="+month+" and 指标种类id in (select QUOTA_TYPE_ID from default_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"+quotaTypeViewMapId+"')"+filterString;
									break;
								}
							}else if (viewscope.equals("can")) {
								switch (month) {
								case 13:
									queryString="select * from "+tableName+" where 考核频率='月' and 指标种类id in (select QUOTA_TYPE_ID from can_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"+quotaTypeViewMapId+"')"+filterString;
									break;
								case 14:
									queryString="select * from "+tableName+" where 考核频率='年' and 指标种类id in (select QUOTA_TYPE_ID from can_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"+quotaTypeViewMapId+"')"+filterString;
									break;
								case 15:
									queryString="select * from "+tableName+" where 指标种类id in (select QUOTA_TYPE_ID from can_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"+quotaTypeViewMapId+"')"+filterString;
									break;
								default:
									queryString="select * from "+tableName+" where 月度="+month+" and 指标种类id in (select QUOTA_TYPE_ID from can_view_quota_type where QUOTA_TYPE_VIEW_MAP_ID='"+quotaTypeViewMapId+"')"+filterString;
									break;
								}
							}
						}
					}

					List resultList=getQueryResults(queryString);
					int i=0;
					int j=0;
					for (Object result : resultList) {
						if (i<firstIndex) {
							i++;
							j++;
						}else {
							i=firstIndex;
							if (j<lastIndex) {
								Map map=(Map) result;
								Record record=new Record();
								for (int k = 1; k <=rsm.getColumnCount(); k++) {
									String columnName=rsm.getColumnName(k);
									record.set(columnName,map.get(columnName));
								}		
								quotaItemStatusRecords.add(record);
								j++;
							}
						}
					}
					
					page.setEntities(quotaItemStatusRecords);
					page.setEntityCount(resultList.size());
			        conn.close();  
				} else {
					System.out.print("无该年度数据："+year);
				}
		     }catch(Exception e){
		    	System.out.print(e.toString());
		     }finally{
		    	conn.close();
		     }
		}else {
			System.out.print("page为空");
		}	
	}
	
	public ResultSet getResultSet(Connection conn,String sql) throws SQLException{
		PreparedStatement statement=conn.prepareStatement(sql);
		ResultSet rs=statement.executeQuery();
		return rs;
	}
	
	public List getQueryResults(String SQL) {
		Session session=this.getSessionFactory().openSession();
		List resultList = null;
		try {
			resultList=session.createSQLQuery(SQL).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			session.close();
		}
		return resultList;
	}

	public Connection getDBConnection(){
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/quotamanagesysdb?useUnicode=true&amp;characterEncoding=UTF-8";
		String user = "root"; 
		String password = "abcd1234";
		try { 
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(url, user, password);
			return conn;
	     }catch(Exception e){
	    	System.out.print(e.toString());
	    	return null;
	     }
	}
}
