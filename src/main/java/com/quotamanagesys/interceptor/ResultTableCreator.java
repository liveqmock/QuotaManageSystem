package com.quotamanagesys.interceptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.Expose;
import com.quotamanagesys.dao.QuotaFormulaResultDao;
import com.quotamanagesys.dao.QuotaFormulaResultValueDao;
import com.quotamanagesys.dao.QuotaItemDao;
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
	
	@Expose
	public void createOrUpdateResultTable(Collection<QuotaItem> quotaItems) throws SQLException{	
		Connection conn=getDBConnection();
		ResultSet rs=null;
		boolean isSuccess=true;

		try {
			rs=getResultSet(conn,"select * from quota_item_view");
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
				boolean isDoUpdate=false;
				for (String quotaItemId : quotaItemIds) {
					if ((quotaItem.getId()).equals(quotaItemId)) {
						isDoUpdate=true;
						String updateString="UPDATE quota_item_view SET 完成值="+quotaItem.getFinishValue()
						+" WHERE 指标id='"+quotaItemId+"'";
						excuteSQL(updateString);
						quotaItemIds.remove(quotaItemId);
						break;
					}
				}
				if (isDoUpdate==false) {
					excuteSQL(getInsertQuotaItemValuesIntoTableString(quotaItemDao.getQuotaItem(quotaItem.getId()),"QUOTA_ITEM_VIEW"));
				}
			}	
		}else {
			excuteSQL(getInitTableString("QUOTA_ITEM_VIEW"));
			for (QuotaItem quotaItem : quotaItems) {
				excuteSQL(getInsertQuotaItemValuesIntoTableString(quotaItem,"QUOTA_ITEM_VIEW"));
			}
		}
		conn.close();
	}
	
	@Expose
	public void deleteItemsFromResultTable(Collection<QuotaItem> quotaItems) throws SQLException{
		Connection conn=getDBConnection();
		ResultSet rs=null;
		boolean isSuccess=true;

		try {
			rs=getResultSet(conn,"select * from quota_item_view");
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
						String deleteString="DELETE FROM quota_item_view WHERE 指标id='"+quotaItemId+"'";
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
	
	public String getInsertQuotaItemValuesIntoTableString(QuotaItem quotaItem,String tableName){
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
				+ "管理部门,"
				+ "责任部门,"
				+ "口径id,"
				+ "口径,"
				+ "维度,"
				+ "完成值";
		
		String professionName=null;
		if (quotaType.getQuotaProfession()==null) {
			professionName=null;
		}else {
			professionName=quotaType.getQuotaProfession().getName();
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
				+"'"+quotaType.getManageDept().getName()+"',"
				+"'"+quotaItemCreator.getQuotaDutyDept().getName()+"',"
				+"'"+quotaItemCreator.getQuotaCover().getId()+"',"
				+"'"+quotaItemCreator.getQuotaCover().getName()+"',"
				+"'"+quotaType.getQuotaDimension().getName()+"',"
				+quotaItem.getFinishValue();
		
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
					dynamicValuesString=quotaFormulaResultValue.getValue();
				}else {
					dynamicColumnsString=dynamicColumnsString+","+quotaFormulaResultValue.getQuotaFormulaResult().getName();
					dynamicValuesString=dynamicValuesString+","+quotaFormulaResultValue.getValue();
				}
			}
		}
		
		if (dynamicColumnsString=="") {
			sqlString=sqlString+staticColumnsString+") VALUES("+staticValuesString+")";
		}else {
			sqlString=sqlString+staticColumnsString+","+dynamicColumnsString+") VALUES("+staticValuesString+","+dynamicValuesString+")";
		}
		return sqlString;
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
				+ "管理部门 VARCHAR(255),"
				+ "责任部门 VARCHAR(255),"
				+ "口径id VARCHAR(255),"
				+ "口径 VARCHAR(255),"
				+ "维度 VARCHAR(255),"
				+ "完成值 VARCHAR(255)";
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
	
	public ResultSet getResultSet(Connection conn,String sql) throws SQLException{
		Statement statement=conn.createStatement();
		ResultSet rs=statement.executeQuery(sql);
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
