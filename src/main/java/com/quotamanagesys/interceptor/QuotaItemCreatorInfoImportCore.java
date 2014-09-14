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
import com.quotamanagesys.dao.QuotaPropertyValueDao;
import com.quotamanagesys.model.QuotaPropertyValue;

@Component
public class QuotaItemCreatorInfoImportCore extends HibernateDao{
	
	@Resource
	QuotaPropertyValueDao quotaPropertyValueDao;

	@Expose
	public void updateTargetValue() throws SQLException{
		Connection conn=getDBConnection();
		ResultSet rs=null;
		boolean isSuccess=true;

		try {
			rs=getResultSet(conn,"select * from quota_item_creator_targetvalue_update");
		} catch (Exception e) {
			isSuccess=false;
		}
		
		if (isSuccess) {
			Session session=this.getSessionFactory().openSession();
			Collection<QuotaPropertyValue> updateQuotaPropertyValues=new ArrayList<QuotaPropertyValue>();
			
			while (rs.next()) {
				String id=rs.getString("id");
				double targetValue=rs.getDouble("targetValue");
				
				QuotaPropertyValue quotaPropertyValue=quotaPropertyValueDao.getQuotaPropertyValue(id);
				if (!quotaPropertyValue.equals(null)) {
					quotaPropertyValue.setValue(targetValue);
					session.merge(quotaPropertyValue);
					session.flush();
					session.clear();
					updateQuotaPropertyValues.add(quotaPropertyValue);
				}
				
				String clearThisRecord="DELETE FROM quota_item_creator_targetvalue_update WHERE id='"+id+"'";
				excuteSQL(clearThisRecord);
			}
			session.flush();
			session.close();
		}
		conn.close();
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
