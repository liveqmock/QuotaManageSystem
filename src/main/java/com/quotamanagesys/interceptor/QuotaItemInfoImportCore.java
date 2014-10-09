package com.quotamanagesys.interceptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.Expose;
import com.quotamanagesys.dao.QuotaItemDao;
import com.quotamanagesys.dao.QuotaTargetValueDao;
import com.quotamanagesys.model.QuotaItem;
import com.quotamanagesys.model.QuotaTargetValue;

@Component
public class QuotaItemInfoImportCore extends HibernateDao{

	@Resource
	QuotaItemDao quotaItemDao;
	@Resource
	CalculateCore calculateCore;
	@Resource
	ResultTableCreator resultTableCreator;
	@Resource
	QuotaTargetValueDao quotaTargetValueDao;
	
	@Expose
	public void updateInputValue() throws SQLException{
		Connection conn=getDBConnection();
		ResultSet rs=null;
		boolean isSuccess=true;

		try {
			rs=getResultSet(conn,"select * from quota_item_value_update");
		} catch (Exception e) {
			isSuccess=false;
		}
		
		if (isSuccess) {
			Session session=this.getSessionFactory().openSession();
			Collection<QuotaItem> updateQuotaItems=new ArrayList<QuotaItem>();
			
			while (rs.next()) {
				String quotaItemName=rs.getString("name");
				int year=rs.getInt("year");
				int month=rs.getInt("month");
				String cover=rs.getString("cover");
				
				String hqlString="from "+QuotaItem.class.getName()
						+" where quotaItemCreator.name='"+quotaItemName+"' and year="+year+" and month="+month
						+" and quotaItemCreator.quotaCover.name='"+cover+"'";
				List<QuotaItem> quotaItems=this.query(hqlString);
				if (quotaItems.size()>0) {
					QuotaItem quotaItem=quotaItems.get(0);
					quotaItem.setFinishValue(rs.getString("finishValue"));
					quotaItem.setAccumulateValue(rs.getString("accumulateValue"));
					quotaItem.setSameTermValue(rs.getString("sameTermValue"));
					session.merge(quotaItem);
					session.flush();
					session.clear();
					updateQuotaItems.add(quotaItem);
				}
				String clearThisRecord="DELETE FROM quota_item_value_update WHERE name='"+quotaItemName
						+"' AND year="+year+" AND month="+month+" AND cover='"+cover+"'";
				excuteSQL(clearThisRecord);
			}
			
			calculateCore.calculate(updateQuotaItems);
			resultTableCreator.createOrUpdateResultTable(updateQuotaItems);
			
			session.flush();
			session.close();
			
		}
		conn.close();
	}
	
	@Expose
	public void updateMonthTargetValue() throws SQLException{
		Connection conn=getDBConnection();
		ResultSet rs=null;
		boolean isSuccess=true;

		try {
			rs=getResultSet(conn,"select * from quota_item_targetvalue_update");
		} catch (Exception e) {
			isSuccess=false;
		}
		
		if (isSuccess) {
			Session session=this.getSessionFactory().openSession();
			Collection<QuotaTargetValue> updateQuotaTargetValues=new ArrayList<QuotaTargetValue>();
			
			while (rs.next()) {
				String id=rs.getString("id");
				double targetValue=rs.getDouble("value");
				
				QuotaTargetValue quotaTargetValue=quotaTargetValueDao.getQuotaTargetValue(id);
				if (!quotaTargetValue.equals(null)) {
					quotaTargetValue.setValue(targetValue);
					session.merge(quotaTargetValue);
					session.flush();
					session.clear();
					updateQuotaTargetValues.add(quotaTargetValue);
				}
				
				String clearThisRecord="DELETE FROM quota_item_targetvalue_update WHERE id='"+id+"'";
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
