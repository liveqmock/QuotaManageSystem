package com.quotamanagesys.interceptor;

import java.lang.annotation.Annotation;
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
import com.quotamanagesys.dao.DepartmentDao;

@Component
public class DynamicViewTest {
	
	@Resource
	DepartmentDao departmentDao;
	
	@Autowired
	@Qualifier("dorado.dataProviderManager")
	private DataProviderManager dataProviderManager;

	public void kk1(ViewConfig viewConfig) throws Exception{
		PropertyDef propertyDef;
		EntityDataType department = (EntityDataType) viewConfig.getDataType("Department");
		
		try { 
			Connection conn=getDBConnection();
			Statement statement=conn.createStatement();
			ResultSet rs=statement.executeQuery("select * from quota_item_view");
			ResultSetMetaData rsm=rs.getMetaData();
	        for (int i = 1; i <=rsm.getColumnCount(); i++) {
	        	propertyDef = new BasePropertyDef(rsm.getColumnName(i));
	    		propertyDef.setDataType(viewConfig.getDataType("String"));
	    		propertyDef.setLabel(rsm.getColumnName(i));
	    		propertyDef.setReadOnly(true);
	    		department.addPropertyDef(propertyDef);
			}
	        conn.close();
	     }catch(Exception e){
	    	 
	     }
	}
	
	@DataProvider
	public ArrayList<Record> getDepartmentRecord(){
		ArrayList<Record> departmentRecords=new ArrayList<Record>();
		
		try { 
			Connection conn=getDBConnection();
			Statement statement=conn.createStatement();
			ResultSet rs=statement.executeQuery("select * from quota_item_view");
			ResultSetMetaData rsm=rs.getMetaData();
			while (rs.next()) {
				Record record=new Record();
				for (int i = 1; i <=rsm.getColumnCount(); i++) {
					String columnName=rsm.getColumnName(i);
					record.set(columnName,rs.getString(columnName));
				}		
				departmentRecords.add(record);
			}
	        conn.close();
	     }catch(Exception e){
	    	System.out.print(e.toString());
	     }
	
		return departmentRecords;
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
