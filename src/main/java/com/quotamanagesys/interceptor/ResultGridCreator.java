package com.quotamanagesys.interceptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.data.provider.manager.DataProviderManager;
import com.bstek.dorado.data.type.EntityDataType;
import com.bstek.dorado.data.type.property.BasePropertyDef;
import com.bstek.dorado.data.type.property.PropertyDef;
import com.bstek.dorado.data.variant.Record;
import com.bstek.dorado.view.manager.ViewConfig;
import com.bstek.dorado.view.widget.grid.DataColumn;
import com.bstek.dorado.view.widget.grid.DataGrid;
import com.quotamanagesys.dao.DepartmentDao;

@Component
public class ResultGridCreator {
	@Resource
	DepartmentDao departmentDao;
	
	@Autowired
	@Qualifier("dorado.dataProviderManager")
	private DataProviderManager dataProviderManager;

	public void dataTypeDefine(ViewConfig viewConfig) throws Exception{
		PropertyDef propertyDef;
		EntityDataType quotaItemStatus = (EntityDataType) viewConfig.getDataType("QuotaItemStatus");
		
		Connection conn=getDBConnection();
		try { 
			Statement statement=conn.createStatement();
			ResultSet rs=statement.executeQuery("select * from quota_item_view");
			ResultSetMetaData rsm=rs.getMetaData();
	        for (int i = 1; i <=rsm.getColumnCount(); i++) {
	        	String columnName=rsm.getColumnName(i);
	        	propertyDef = new BasePropertyDef(columnName);
	        	if (columnName=="年度"||columnName=="季度"||columnName=="月度") {
	        		propertyDef.setDataType(viewConfig.getDataType("int"));
				}else{
					propertyDef.setDataType(viewConfig.getDataType("String"));
				}
	    		propertyDef.setLabel(columnName);
	    		propertyDef.setReadOnly(true);
	    		quotaItemStatus.addPropertyDef(propertyDef);
			}
	        conn.close();
	     }catch(Exception e){
	    	System.out.print(e.toString());
	     }finally{
	    	conn.close();
	     }
	}
	
	@DataProvider
	public ArrayList<Record> getQuotaItemStatusRecord() throws Exception{
		ArrayList<Record> quotaItemStatusRecords=new ArrayList<Record>();
		
		Connection conn=getDBConnection();
		try {
			Statement statement=conn.createStatement();
			ResultSet rs=statement.executeQuery("select * from quota_item_view");
			ResultSetMetaData rsm=rs.getMetaData();
			while (rs.next()) {
				Record record=new Record();
				for (int i = 1; i <=rsm.getColumnCount(); i++) {
					String columnName=rsm.getColumnName(i);
					if (columnName=="年度"||columnName=="季度"||columnName=="月度"){
						record.set(columnName,rs.getInt(columnName));
					}else {
						record.set(columnName,rs.getString(columnName));
					}	
				}		
				quotaItemStatusRecords.add(record);
			}
	        conn.close();
	     }catch(Exception e){
	    	System.out.print(e.toString());
	     }finally{
	    	conn.close();
	     }
		return quotaItemStatusRecords;
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
