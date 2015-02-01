package com.quotamanagesys.interceptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.apache.tools.ant.taskdefs.optional.javah.Kaffeh;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.Expose;
import com.quotamanagesys.dao.QuotaFormulaResultDao;
import com.quotamanagesys.dao.QuotaFormulaResultValueDao;
import com.quotamanagesys.dao.QuotaItemDao;
import com.quotamanagesys.dao.QuotaItemViewTableManageDao;
import com.quotamanagesys.dao.QuotaPropertyDao;
import com.quotamanagesys.dao.QuotaPropertyValueDao;
import com.quotamanagesys.dao.QuotaTargetValueDao;
import com.quotamanagesys.model.QuotaFormulaResult;
import com.quotamanagesys.model.QuotaFormulaResultValue;
import com.quotamanagesys.model.QuotaItem;
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaProperty;
import com.quotamanagesys.model.QuotaPropertyValue;
import com.quotamanagesys.model.QuotaTargetValue;
import com.quotamanagesys.model.QuotaType;

@Component
public class ResultTableCreator extends HibernateDao{

	@Resource
	QuotaPropertyDao quotaPropertyDao;
	@Resource
	QuotaFormulaResultDao quotaFormulaResultDao;
	@Resource
	QuotaFormulaResultValueDao quotaFormulaResultValueDao;
	@Resource
	QuotaPropertyValueDao quotaPropertyValueDao;
	@Resource
	QuotaItemDao quotaItemDao;
	@Resource
	QuotaTargetValueDao quotaTargetValueDao;
	@Resource
	QuotaItemViewTableManageDao quotaItemViewTableManageDao;
	
	//更新全部指标数据
	@Expose
	public void createOrUpdateAll() throws SQLException{
		Collection<QuotaItem> quotaItems=quotaItemDao.getAll();
		createOrUpdateResultTable(quotaItems);
	}
	
	//更新指标集合对应的数据
	@Expose
	public void createOrUpdateResultTable(Collection<QuotaItem> quotaItems) throws SQLException{	
		Connection conn=getDBConnection();
		ResultSet rs=null;
		boolean isSuccess=true;

		Calendar calendar=Calendar.getInstance();
		
		//获取执行时的年月
		int year=calendar.get(Calendar.YEAR);
		int month=calendar.get(Calendar.MONTH)+1;//calendar的真实月份需要+1,因为calendar的月份从0开始
		
		String tableName="quota_item_view_";
		if (month>1) {
			tableName=tableName+year;
			quotaItemViewTableManageDao.initQuotaItemViewTableManage(year);
		}else {
			tableName=tableName+(year-1);
			quotaItemViewTableManageDao.initQuotaItemViewTableManage(year-1);
		}
		
		try {
			rs=getResultSet(conn,"select * from "+tableName);
		} catch (Exception e) {
			isSuccess=false;
		}
		
		if (isSuccess) {
			ArrayList<String> quotaItemIds=new ArrayList<String>();
			while (rs.next()) {
				String quotaItemId=rs.getString("指标id");
				if (quotaItemId!=null) {
					quotaItemIds.add(quotaItemId);
				}else {
					System.out.print("quotaId is null"+'\n');
				}		
			}
	
			for (QuotaItem quotaItem : quotaItems) {
				boolean isDoUpdate=false;
				for (String quotaItemId : quotaItemIds) {
					if ((quotaItem.getId()).equals(quotaItemId)) {
						isDoUpdate=true;
						String updateString=getUpdateQuotaItemValuesToTableString(quotaItem, tableName);
						excuteSQL(updateString);
						quotaItemIds.remove(quotaItemId);
						break;
					}else {
						continue;
					}
				}
				if (isDoUpdate==false) {
					String insertSqlString=getInsertQuotaItemValuesIntoTableString(quotaItem,tableName);
					if (insertSqlString!=null) {
						excuteSQL(insertSqlString);
					} else {
						System.out.print(quotaItem.getId()+'\n');
					}	
				}
			}	
		}else {
			excuteSQL(getInitTableString(tableName));
			for (QuotaItem quotaItem : quotaItems) {
				String insertSqlString=getInsertQuotaItemValuesIntoTableString(quotaItem,tableName);
				if (insertSqlString!=null) {
					excuteSQL(insertSqlString);
				} else {
					System.out.print(quotaItem.getId()+'\n');
				}
			}
		}
		conn.close();
	}
	
	@Expose
	public void deleteItemsFromResultTable(Collection<QuotaItem> quotaItems) throws SQLException{
		Connection conn=getDBConnection();
		ResultSet rs=null;
		boolean isSuccess=true;

		Calendar calendar=Calendar.getInstance();
		
		//获取执行时的年月
		int year=calendar.get(Calendar.YEAR);
		int month=calendar.get(Calendar.MONTH)+1;//calendar的真实月份需要+1,因为calendar的月份从0开始
		
		String tableName="quota_item_view_";
		if (month>1) {
			tableName=tableName+year;
		}else {
			tableName=tableName+(year-1);
		}
		
		try {
			rs=getResultSet(conn,"select * from "+tableName);
		} catch (Exception e) {
			isSuccess=false;
		}
		
		if (isSuccess) {
			ArrayList<String> quotaItemIds=new ArrayList<String>();
			while (rs.next()) {
				String quotaItemId=rs.getString("指标id");
				quotaItemIds.add(quotaItemId);
			}
			for (QuotaItem quotaItem : quotaItems) {
				for (String quotaItemId : quotaItemIds) {
					if ((quotaItem.getId()).equals(quotaItemId)) {
						String deleteString="DELETE FROM "+tableName+" WHERE 指标id='"+quotaItemId+"'";
						excuteSQL(deleteString);
						quotaItemIds.remove(quotaItemId);
						break;
					}
				}
			}	
		}else {
			System.out.print("数据表不存在，无需执行删除操作");
		}
		conn.close();
	}
	
	public String getUpdateQuotaItemValuesToTableString(QuotaItem quotaItem,String tableName){
		//更新完成值、累计值、去年同期值
		String firstSubmitTime="";
		if (quotaItem.getFirstSubmitTime()!=null) { 
			firstSubmitTime=(quotaItem.getFirstSubmitTime().getYear()+1900)+"-"
					+(quotaItem.getFirstSubmitTime().getMonth()+1)+"-"
					+quotaItem.getFirstSubmitTime().getDate();
		}
		String lastSubmitTime="";
		if (quotaItem.getLastSubmitTime()!=null) {
			lastSubmitTime=(quotaItem.getLastSubmitTime().getYear()+1900)+"-"
					+(quotaItem.getLastSubmitTime().getMonth()+1)+"-"
					+quotaItem.getLastSubmitTime().getDate();
		}
		String usernameOfLastSubmit="";
		if (quotaItem.getUsernameOfLastSubmit()!=null) {
			usernameOfLastSubmit=quotaItem.getUsernameOfLastSubmit();
		}
		String redLightReason="";
		if (quotaItem.getRedLightReason()!=null) {
			redLightReason=quotaItem.getRedLightReason();
		}
		
		String staticSetString;
		QuotaItemCreator quotaItemCreator=quotaItem.getQuotaItemCreator();
		QuotaType quotaType=quotaItemCreator.getQuotaType();
		
		if (quotaItem.isAllowSubmit()==false) {
			staticSetString="指标名称='"+quotaType.getName()+"'"
					+ ",指标专业='"+quotaType.getName()+"'"
					+ ",指标级别='"+quotaType.getQuotaLevel().getName()+"'"
					+ ",计量单位='"+quotaType.getQuotaUnit().getName()+"'"
					+ ",小数位数="+quotaType.getDigit()
					+ ",管理部门id='"+quotaType.getManageDept().getId()+"'"
					+ ",管理部门='"+quotaType.getManageDept().getName()+"'"
					+ ",责任部门id='"+quotaItemCreator.getQuotaDutyDept().getId()+"'"
					+ ",责任部门='"+quotaItemCreator.getQuotaDutyDept().getName()+"'"
					+ ",口径id='"+quotaItemCreator.getQuotaCover().getId()+"'"
					+ ",口径='"+quotaItemCreator.getQuotaCover().getName()+"'"
					+ ",维度id='"+quotaType.getQuotaDimension().getId()+"'"
					+ ",维度='"+quotaType.getQuotaDimension().getName()+"'"
					+",完成值=''"
					+",累计值=''"
					+",去年同期值=''"
					+",去年同期累计值=''"
					+",初次填报时间=''"
					+",最后更新时间=''"
					+",填报人=''"
					+",异动原因=''"
					+",提交状态="+quotaItem.isAllowSubmit()
					+",填报超时="+quotaItem.isOverTime();
		}else {
			staticSetString="指标名称='"+quotaType.getName()+"'"
					+ ",指标专业='"+quotaType.getName()+"'"
					+ ",指标级别='"+quotaType.getQuotaLevel().getName()+"'"
					+ ",计量单位='"+quotaType.getQuotaUnit().getName()+"'"
					+ ",小数位数="+quotaType.getDigit()
					+ ",管理部门id='"+quotaType.getManageDept().getId()+"'"
					+ ",管理部门='"+quotaType.getManageDept().getName()+"'"
					+ ",责任部门id='"+quotaItemCreator.getQuotaDutyDept().getId()+"'"
					+ ",责任部门='"+quotaItemCreator.getQuotaDutyDept().getName()+"'"
					+ ",口径id='"+quotaItemCreator.getQuotaCover().getId()+"'"
					+ ",口径='"+quotaItemCreator.getQuotaCover().getName()+"'"
					+ ",维度id='"+quotaType.getQuotaDimension().getId()+"'"
					+ ",维度='"+quotaType.getQuotaDimension().getName()+"'"
					+",完成值="+quotaItem.getFinishValue()
					+",累计值="+quotaItem.getAccumulateValue()
					+",去年同期值="+quotaItem.getSameTermValue()
					+",去年同期累计值="+quotaItem.getSameTermAccumulateValue()
					+",初次填报时间='"+firstSubmitTime+"'"
					+",最后更新时间='"+lastSubmitTime+"'"
					+",填报人='"+usernameOfLastSubmit+"'"
					+",异动原因='"+redLightReason+"'"
					+",提交状态="+quotaItem.isAllowSubmit()
					+",填报超时="+quotaItem.isOverTime();
		}

		String dynamicSetString="";
		//更新计算结果
		Collection<QuotaFormulaResultValue> quotaFormulaResultValues=quotaFormulaResultValueDao.getQuotaFormulaResultValuesByQuotaItem(quotaItem.getId());
		for (QuotaFormulaResultValue quotaFormulaResultValue : quotaFormulaResultValues) {
			if (quotaItem.isAllowSubmit()==false) {
				//未提交指标计算结果设为#
				dynamicSetString=dynamicSetString+","+quotaFormulaResultValue.getQuotaFormulaResult().getName()
						+"='#'";
			}else {
				dynamicSetString=dynamicSetString+","+quotaFormulaResultValue.getQuotaFormulaResult().getName()
						+"="+quotaFormulaResultValue.getValue();
			}
		}
		//更新月度目标值
		Collection<QuotaTargetValue> quotaTargetValues=quotaTargetValueDao.getQuotaTargetValuesByQuotaItem(quotaItem.getId());
		for (QuotaTargetValue quotaTargetValue : quotaTargetValues) {
			dynamicSetString=dynamicSetString+","+quotaTargetValue.getQuotaProperty().getName()+"_月度"
					+"="+quotaTargetValue.getValue();
		}
		//更新年度目标值
		Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItem.getQuotaItemCreator().getId());
		for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
			dynamicSetString=dynamicSetString+","+quotaPropertyValue.getQuotaProperty().getName()
					+"="+quotaPropertyValue.getValue();
		}
		
		String updateString="UPDATE "+tableName+" SET "+staticSetString+dynamicSetString+" WHERE 指标id='"+quotaItem.getId()+"'";
		
		return updateString;
	}
	
	public String getInsertQuotaItemValuesIntoTableString(QuotaItem quotaItem,String tableName){
		try {
			QuotaItemCreator quotaItemCreator=quotaItem.getQuotaItemCreator();
			QuotaType quotaType=quotaItemCreator.getQuotaType();
			String sqlString="INSERT INTO "+tableName+"(";
			String staticColumnsString;
			String staticValuesString;
			staticColumnsString="指标id,"
					+"指标名称,"
					+"年度,"
					+ "月度,"
					+ "指标专业,"
					+ "指标种类id,"
					+ "指标级别,"
					+ "计量单位,"
					+ "小数位数,"
					+ "考核频率,"
					+ "管理部门id,"
					+ "管理部门,"
					+ "责任部门id,"
					+ "责任部门,"
					+ "口径id,"
					+ "口径,"
					+ "维度id,"
					+ "维度,"
					+ "完成值,"
					+ "累计值,"
					+ "去年同期值,"
					+ "去年同期累计值,"
					+ "初次填报时间,"
					+ "最后更新时间,"
					+ "填报人,"
					+ "异动原因,"
					+ "提交状态,"
					+ "填报超时";
			
			String professionName=null;
			if (quotaType.getQuotaProfession()==null) {
				professionName=null;
			}else {
				professionName=quotaType.getQuotaProfession().getName();
			}
			
			String firstSubmitTime="";
			if (quotaItem.getFirstSubmitTime()!=null) { 
				firstSubmitTime=(quotaItem.getFirstSubmitTime().getYear()+1900)+"年"
						+(quotaItem.getFirstSubmitTime().getMonth()+1)+"月"
						+quotaItem.getFirstSubmitTime().getDate()+"日";
			}
			String lastSubmitTime="";
			if (quotaItem.getLastSubmitTime()!=null) {
				lastSubmitTime=(quotaItem.getLastSubmitTime().getYear()+1900)+"年"
						+(quotaItem.getLastSubmitTime().getMonth()+1)+"月"
						+quotaItem.getLastSubmitTime().getDate()+"日";
			}
			String usernameOfLastSubmit="";
			if (quotaItem.getUsernameOfLastSubmit()!=null) {
				usernameOfLastSubmit=quotaItem.getUsernameOfLastSubmit();
			}
			String redLightReason="";
			if (quotaItem.getRedLightReason()!=null) {
				redLightReason=quotaItem.getRedLightReason();
			}
			
			staticValuesString="'"+quotaItem.getId()+"',"
					+"'"+quotaItemCreator.getName()+"',"
					+quotaItem.getYear()+","
					+quotaItem.getMonth()+","
					+"'"+professionName+"',"
					+"'"+quotaType.getId()+"',"
					+"'"+quotaType.getQuotaLevel().getName()+"',"
					+"'"+quotaType.getQuotaUnit().getName()+"',"
					+quotaType.getDigit()+","
					+"'"+quotaType.getRate()+"',"
					+"'"+quotaType.getManageDept().getId()+"',"
					+"'"+quotaType.getManageDept().getName()+"',"
					+"'"+quotaItemCreator.getQuotaDutyDept().getId()+"',"
					+"'"+quotaItemCreator.getQuotaDutyDept().getName()+"',"
					+"'"+quotaItemCreator.getQuotaCover().getId()+"',"
					+"'"+quotaItemCreator.getQuotaCover().getName()+"',"
					+"'"+quotaType.getQuotaDimension().getId()+"',"
					+"'"+quotaType.getQuotaDimension().getName()+"',";
					
			
			if (quotaItem.isAllowSubmit()==false) {
				staticValuesString=staticValuesString+"'','','','','','','','',"
						+quotaItem.isAllowSubmit()+","
						+quotaItem.isOverTime();
			}else {
				staticValuesString=staticValuesString+quotaItem.getFinishValue()+","
						+quotaItem.getAccumulateValue()+","
						+quotaItem.getSameTermValue()+","
						+quotaItem.getSameTermAccumulateValue()+","
						+"'"+firstSubmitTime+"',"
						+"'"+lastSubmitTime+"',"
						+"'"+usernameOfLastSubmit+"',"
						+"'"+redLightReason+"',"
						+quotaItem.isAllowSubmit()+","
						+quotaItem.isOverTime();
			}	
			
			String dynamicColumnsString="";
			String dynamicValuesString="";
			Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItem.getQuotaItemCreator().getId());
			Collection<QuotaTargetValue> quotaTargetValues=quotaTargetValueDao.getQuotaTargetValuesByQuotaItem(quotaItem.getId());
			
			if (quotaPropertyValues.size()>0) {
				for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
					if (dynamicColumnsString=="") {
						dynamicColumnsString=quotaPropertyValue.getQuotaProperty().getName();
						dynamicValuesString=quotaPropertyValue.getValue()+"";
					}else {
						dynamicColumnsString=dynamicColumnsString+","+quotaPropertyValue.getQuotaProperty().getName();
						dynamicValuesString=dynamicValuesString+","+quotaPropertyValue.getValue();
					}
				}
			}
			
			if (quotaTargetValues.size()>0) {
				for (QuotaTargetValue quotaTargetValue : quotaTargetValues) {
					if (dynamicColumnsString=="") {
						dynamicColumnsString=quotaTargetValue.getQuotaProperty().getName()+"_月度";
						dynamicValuesString=quotaTargetValue.getValue()+"";
					}else {
						dynamicColumnsString=dynamicColumnsString+","+quotaTargetValue.getQuotaProperty().getName()+"_月度";
						dynamicValuesString=dynamicValuesString+","+quotaTargetValue.getValue();
					}
				}
			}
			
			Collection<QuotaFormulaResultValue> quotaFormulaResultValues=quotaFormulaResultValueDao.getQuotaFormulaResultValuesByQuotaItem(quotaItem.getId());
			if (quotaFormulaResultValues.size()>0) {
				for (QuotaFormulaResultValue quotaFormulaResultValue : quotaFormulaResultValues) {
					if (dynamicColumnsString=="") {
						dynamicColumnsString=quotaFormulaResultValue.getQuotaFormulaResult().getName();
						if (quotaItem.isAllowSubmit()==false) {
							//未提交指标计算结果设为#
							dynamicValuesString="'#'";
						} else {
							dynamicValuesString=quotaFormulaResultValue.getValue();
						}	
					}else {
						dynamicColumnsString=dynamicColumnsString+","+quotaFormulaResultValue.getQuotaFormulaResult().getName();
						if (quotaItem.isAllowSubmit()==false) {
							//未提交指标计算结果设为#
							dynamicValuesString=dynamicValuesString+",'#'";
						} else {
							dynamicValuesString=dynamicValuesString+","+quotaFormulaResultValue.getValue();
						}
					}
				}
			}
			
			if (dynamicColumnsString=="") {
				sqlString=sqlString+staticColumnsString+") VALUES("+staticValuesString+")";
			}else {
				sqlString=sqlString+staticColumnsString+","+dynamicColumnsString+") VALUES("+staticValuesString+","+dynamicValuesString+")";
			}
			return sqlString;
		} catch (Exception e) {
			System.out.print(quotaItem.getId()+"抛出异常为："+e.toString()+'\n');
			return null;
		}
	}
	
	public String getInitTableString(String tableName){
		String sqlString="CREATE TABLE "+tableName+"(";
		String staticColumnsString="指标id VARCHAR(255),"
				+ "指标名称 VARCHAR(255),"
				+ "年度 INT,"
				+ "月度 INT,"
				+ "指标专业 VARCHAR(255),"
				+ "指标种类id VARCHAR(255),"
				+ "指标级别 VARCHAR(255),"
				+ "计量单位 VARCHAR(255),"
				+ "小数位数 INT,"
				+ "考核频率 VARCHAR(255),"
				+ "管理部门id VARCHAR(255),"
				+ "管理部门 VARCHAR(255),"
				+ "责任部门id VARCHAR(255),"
				+ "责任部门 VARCHAR(255),"
				+ "口径id VARCHAR(255),"
				+ "口径 VARCHAR(255),"
				+ "维度id VARCHAR(255),"
				+ "维度 VARCHAR(255),"
				+ "完成值 VARCHAR(255),"
				+ "累计值 VARCHAR(255),"
				+ "去年同期值 VARCHAR(255),"
				+ "去年同期累计值 VARCHAR(255),"
				+ "初次填报时间 VARCHAR(255),"
				+ "最后更新时间 VARCHAR(255),"
				+ "填报人 VARCHAR(255),"
				+ "异动原因 VARCHAR(1500),"
				+ "提交状态 BIT(1),"
				+ "填报超时 BIT(1)";
		String dynamicColumnsString="";
		Collection<QuotaProperty> quotaProperties=quotaPropertyDao.getAll();
		if (quotaProperties.size()>0) {
			for (QuotaProperty quotaProperty : quotaProperties) {
				if (dynamicColumnsString=="") {
					dynamicColumnsString=quotaProperty.getName()+" VARCHAR(255),"+quotaProperty.getName()+"_月度"+" VARCHAR(255)";
				}else {
					dynamicColumnsString=dynamicColumnsString+","+quotaProperty.getName()+" VARCHAR(255),"+quotaProperty.getName()+"_月度"+" VARCHAR(255)";
				}
			}
		}	
		Collection<QuotaFormulaResult> quotaFormulaResults=quotaFormulaResultDao.getAll();
		if (quotaFormulaResults.size()>0) {
			for (QuotaFormulaResult quotaFormulaResult : quotaFormulaResults) {
				if (dynamicColumnsString=="") {
					dynamicColumnsString=quotaFormulaResult.getName()+" VARCHAR(255)";
				}else {
					dynamicColumnsString=dynamicColumnsString+","+quotaFormulaResult.getName()+" VARCHAR(255)";
				}
			}
		}
		if (dynamicColumnsString=="") {
			sqlString=sqlString+staticColumnsString+dynamicColumnsString+"}";
		}else {
			sqlString=sqlString+staticColumnsString+","+dynamicColumnsString+")";
		}
		return sqlString;
	}
	
	public void excuteSQL(String SQL) {
		Session session = this.getSessionFactory().openSession();
		try {
			session.createSQLQuery(SQL).executeUpdate();
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			session.flush();
			session.close();
		}
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
	
	public ResultSet getResultSet(Connection conn,String sql) throws SQLException{
		PreparedStatement statement=conn.prepareStatement(sql);
		ResultSet rs=statement.executeQuery();
		return rs;
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
