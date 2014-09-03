package com.quotamanagesys.interceptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.common.event.ClientEvent;
import com.bstek.dorado.data.provider.manager.DataProviderManager;
import com.bstek.dorado.data.type.EntityDataType;
import com.bstek.dorado.data.type.property.BasePropertyDef;
import com.bstek.dorado.data.type.property.PropertyDef;
import com.bstek.dorado.data.variant.Record;
import com.bstek.dorado.view.manager.ViewConfig;
import com.bstek.dorado.view.widget.grid.ColumnGroup;
import com.bstek.dorado.view.widget.grid.DataColumn;
import com.bstek.dorado.view.widget.grid.DataGrid;
import com.bstek.dorado.view.widget.grid.StretchColumnsMode;
import com.quotamanagesys.dao.DepartmentDao;
import com.quotamanagesys.dao.ShowColumnDao;
import com.quotamanagesys.dao.ShowColumnGroupDao;
import com.quotamanagesys.model.ShowColumn;
import com.quotamanagesys.model.ShowColumnGroup;

@Component
public class ResultGridCreator {
	@Resource
	DepartmentDao departmentDao;
	@Resource
	ShowColumnDao showColumnDao;
	@Resource
	ShowColumnGroupDao showColumnGroupDao;
	
	@Autowired
	@Qualifier("dorado.dataProviderManager")
	private DataProviderManager dataProviderManager;

	public void ViewConfigInit(ViewConfig viewConfig) throws Exception{
		PropertyDef propertyDef;
		EntityDataType quotaItemStatus = (EntityDataType) viewConfig.getDataType("QuotaItemStatus");

		Connection conn=getDBConnection();
		ResultSet rs=null;
		ResultSetMetaData rsm = null;
		boolean isSuccess=true;
		
		try {
			rs=getResultSet(conn,"select * from quota_item_view");
			rsm=rs.getMetaData();
		} catch (Exception e) {
			isSuccess=false;
		}
		
		if (isSuccess) {
			for (int i = 1; i <=rsm.getColumnCount(); i++) {
	        	String columnName=rsm.getColumnName(i);
	        	propertyDef = new BasePropertyDef(columnName);
	        	if (columnName=="定業"||columnName=="埖業") {
	        		propertyDef.setDataType(viewConfig.getDataType("int"));
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
		Collection<ShowColumn> showColumnsNotYetLinked=showColumnDao.getShowColumnsNotYetLinked();
		if (showColumnsNotYetLinked.size()>0) {
			for (ShowColumn showColumn : showColumnsNotYetLinked) {
				DataColumn dataColumn=new DataColumn();
				dataColumn.setName(showColumn.getName());
				dataColumn.setProperty(showColumn.getName());
				dataColumn.setCaption(showColumn.getName());
				dataColumn.setWidth(showColumn.getWidth()+"");
				dataColumn.setVisible(showColumn.getVisible());
				dataColumn.setWrappable(showColumn.getWrappable());
				
				if (showColumn.getRender()!=null) {
					ClinetEventImp clinetEventImp=new ClinetEventImp();
					clinetEventImp.setScript(showColumn.getRender().getRenderCode());
					dataColumn.addClientEventListener("onRenderCell",clinetEventImp);
				}
				
				dgQuotaItemStatus.addColumn(dataColumn);
			}
		}
		
		Collection<ShowColumnGroup> showColumnGroups=showColumnGroupDao.getAll();
		if (showColumnGroups.size()>0) {
			for (ShowColumnGroup showColumnGroup : showColumnGroups) {
				ColumnGroup columnGroup=new ColumnGroup();
				columnGroup.setCaption(showColumnGroup.getName());
				
				Collection<ShowColumn> showColumns=showColumnDao.getShowColumnsByGroup(showColumnGroup.getId());
				for (ShowColumn showColumn : showColumns) {
					DataColumn dataColumn=new DataColumn();
					dataColumn.setName(showColumn.getName());
					dataColumn.setProperty(showColumn.getName());
					dataColumn.setCaption(showColumn.getName());
					dataColumn.setWidth(showColumn.getWidth()+"");
					dataColumn.setVisible(showColumn.getVisible());
					dataColumn.setWrappable(showColumn.getWrappable());
					
					if (showColumn.getRender()!=null) {
						ClinetEventImp clinetEventImp=new ClinetEventImp();
						clinetEventImp.setScript(showColumn.getRender().getRenderCode());
						dataColumn.addClientEventListener("onRenderCell",clinetEventImp);
					}

					columnGroup.addColumn(dataColumn);
				}
				
				dgQuotaItemStatus.addColumn(columnGroup);
			}
		}
		
		dgQuotaItemStatus.setShowFilterBar(true);
		dgQuotaItemStatus.setDataSet("dsQuotaItemStatus");
		dgQuotaItemStatus.setStretchColumnsMode(StretchColumnsMode.off);
		dgQuotaItemStatus.setDynaRowHeight(true);
		dgQuotaItemStatus.setFixedColumnCount(1);
	}
	
	@DataProvider
	public ArrayList<Record> getQuotaItemStatusRecord() throws Exception{
		ArrayList<Record> quotaItemStatusRecords=new ArrayList<Record>();
		
		Connection conn=getDBConnection();
		try {
			ResultSet rs=getResultSet(conn,"select * from quota_item_view");
			ResultSetMetaData rsm=rs.getMetaData();
			while (rs.next()) {
				Record record=new Record();
				for (int i = 1; i <=rsm.getColumnCount(); i++) {
					String columnName=rsm.getColumnName(i);
					if (columnName=="定業"||columnName=="埖業"){
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
