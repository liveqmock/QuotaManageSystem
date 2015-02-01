package com.quotamanagesys.interceptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.business.IUser;
import com.bstek.bdf2.core.context.ContextHolder;
import com.bstek.bdf2.core.exception.NoneLoginException;
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
			Collection<QuotaItem> calculateQuotaItems=new ArrayList<QuotaItem>();
			
			Calendar calendar=Calendar.getInstance();
			int currentYear=calendar.get(Calendar.YEAR);//
			int currentMonth=calendar.get(Calendar.MONTH)+1;//calendar的真实月份需要+1,因为calendar的月份从0开始
			int currentDate=calendar.get(Calendar.DATE);//
			Date thisDate=calendar.getTime();
			IUser loginuser = ContextHolder.getLoginUser();
			
			while (rs.next()) {
				String quotaItemName=rs.getString("name");
				int year=rs.getInt("year");
				int month=rs.getInt("month");
				String cover=rs.getString("cover");
				String rate=rs.getString("rate");
				
				//只能导入上月指标，年度指标只允许在第二年1月导入
				boolean isAllowImport=true;
				
				if (rate.equals("年")) {
					if (currentMonth==1) {
						if (year!=(currentYear-1)) {
							isAllowImport=false;
						}
					}else {
						isAllowImport=false;
					}
				}else if (rate.equals("月")) {
					if (currentMonth==1) {
						if (year!=(currentYear-1)) {
							isAllowImport=false;
						}else {
							if (month!=12) {
								isAllowImport=false;
							}
						}
					}else {
						if (year!=currentYear) {
							isAllowImport=false;
						}else {
							if (month!=(currentMonth-1)) {
								isAllowImport=false;
							}
						}
					}
				}
				
				
				if (isAllowImport==true) {
					String hqlString="from "+QuotaItem.class.getName()
							+" where quotaItemCreator.name='"+quotaItemName+"' and year="+year+" and month="+month
							+" and quotaItemCreator.quotaCover.name='"+cover+"'";
					List<QuotaItem> quotaItems=this.query(hqlString);
					if (quotaItems.size()>0) {
						boolean isWantToBeCalculated=false;
						boolean isCanUpdate=true;
						QuotaItem quotaItem=quotaItems.get(0);

						if (rs.getString("finishValue")!=null) {
							boolean isAvailable=true;
							try {
								double finishValue=Double.parseDouble(rs.getString("finishValue"));
							} catch (NumberFormatException e) {
								System.out.print(e.toString());
								isAvailable=false;
							}
							if (isAvailable) {
								if (quotaItem.getFinishValue()==null) {
									quotaItem.setFinishValue(rs.getString("finishValue"));
									isWantToBeCalculated=true;
								}else {
									if (!quotaItem.getFinishValue().equals(rs.getString("finishValue"))) {
										quotaItem.setFinishValue(rs.getString("finishValue"));
										isWantToBeCalculated=true;
									}
								}
							}else {
								isCanUpdate=false;
								System.out.print("finishValue值不合法");
							}	
						}else {
							isCanUpdate=false;
						}
						
						if (rs.getString("accumulateValue")!=null) {
							boolean isAvailable=true;
							try {
								double accumulateValue=Double.parseDouble(rs.getString("accumulateValue"));
							} catch (NumberFormatException e) {
								System.out.print(e.toString());
								isAvailable=false;
							}
							if (isAvailable) {
								if (quotaItem.getAccumulateValue()==null) {
									quotaItem.setAccumulateValue(rs.getString("accumulateValue"));
									isWantToBeCalculated=true;
								} else {
									if (!quotaItem.getAccumulateValue().equals(rs.getString("accumulateValue"))) {
										quotaItem.setAccumulateValue(rs.getString("accumulateValue"));
										isWantToBeCalculated=true;
									}
								}
							}else {
								isCanUpdate=false;
								System.out.print("accumulateValue值不合法");
							}
						}else {
							isCanUpdate=false;
						}

						if (rs.getString("sameTermValue")!=null) {
							boolean isAvailable=true;
							try {
								double sameTermValue=Double.parseDouble(rs.getString("sameTermValue"));
							} catch (NumberFormatException e) {
								System.out.print(e.toString());
								isAvailable=false;
							}
							if (isAvailable) {
								if (quotaItem.getSameTermValue()==null) {
									quotaItem.setSameTermValue(rs.getString("sameTermValue"));
									isWantToBeCalculated=true;
								} else {
									if (!quotaItem.getSameTermValue().equals(rs.getString("sameTermValue"))) {
										quotaItem.setSameTermValue(rs.getString("sameTermValue"));
										isWantToBeCalculated=true;
									}
								}
							}else {
								isCanUpdate=false;
								System.out.print("sameTermValue值不合法");
							}
						}else {
							isCanUpdate=false;
						}

						if (rs.getString("sameTermAccumulateValue")!=null) {
							boolean isAvailable=true;
							try {
								double sameTermAccumulateValue=Double.parseDouble(rs.getString("sameTermAccumulateValue"));
							} catch (NumberFormatException e) {
								System.out.print(e.toString());
								isAvailable=false;
							}
							if (isAvailable) {
								if (quotaItem.getSameTermAccumulateValue()==null) {
									quotaItem.setSameTermAccumulateValue(rs.getString("sameTermAccumulateValue"));
									isWantToBeCalculated=true;
								} else {
									if (!quotaItem.getSameTermAccumulateValue().equals(rs.getString("sameTermAccumulateValue"))) {
										quotaItem.setSameTermAccumulateValue(rs.getString("sameTermAccumulateValue"));
										isWantToBeCalculated=true;
									}
								}
							}else {
								isCanUpdate=false;
								System.out.print("sameTermAccumulateValue值不合法");
							}
						}else {
							isCanUpdate=false;
						}

						//填写超时状态设置、记录第一次填写时间、最后一次填写时间、最后填写人员姓名
						if (quotaItem.getFirstSubmitTime()==null) {
							quotaItem.setFirstSubmitTime(thisDate);
						}
						quotaItem.setLastSubmitTime(thisDate);
						if (loginuser == null) {
							throw new NoneLoginException("Please login first!");
						}else {
							quotaItem.setUsernameOfLastSubmit(loginuser.getCname());
						}
						if (quotaItem.getQuotaItemCreator().getQuotaType().getRate().equals("年")) {
							if ((currentMonth<=1)&&(quotaItem.getYear()==(currentYear-1))) {
								if (currentDate>5) {
									quotaItem.setOverTime(true);
								}
							}
						}else if (quotaItem.getQuotaItemCreator().getQuotaType().getRate().equals("月")) {
							if ((quotaItem.getMonth()==(currentMonth-1))&&(quotaItem.getYear()==currentYear)) {
								if (currentDate>5) {
									quotaItem.setOverTime(true);
								}
							}else if ((quotaItem.getMonth()<(currentMonth-1))&&(quotaItem.getYear()==currentYear)) {
								quotaItem.setOverTime(true);
							}
						}
						
						if (rs.getString("redLightReason")!=null) {
							quotaItem.setRedLightReason(rs.getString("redLightReason"));
						}

						if (isCanUpdate) {
							session.merge(quotaItem);
							session.flush();
							session.clear();
							if (isWantToBeCalculated) {
								calculateQuotaItems.add(quotaItem);
							}
							updateQuotaItems.add(quotaItem);
						}
				}
				
				}
				String clearThisRecord="DELETE FROM quota_item_value_update WHERE name='"+quotaItemName
						+"' AND year="+year+" AND month="+month+" AND cover='"+cover+"'";
				excuteSQL(clearThisRecord);
			}
			
			calculateCore.calculate(calculateQuotaItems);
			quotaItemDao.setAllowSubmitStatus(updateQuotaItems);
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
			ArrayList<QuotaItem> updateQuotaItems=new ArrayList<QuotaItem>();
			
			while (rs.next()) {
				String id=rs.getString("id");
				double targetValue=rs.getDouble("value");
				
				QuotaTargetValue quotaTargetValue=quotaTargetValueDao.getQuotaTargetValue(id);
				if (quotaTargetValue!=null) {
					quotaTargetValue.setValue(targetValue);
					session.merge(quotaTargetValue);
					session.flush();
					session.clear();
					updateQuotaItems.add(quotaTargetValue.getQuotaItem());
				}
				
				String clearThisRecord="DELETE FROM quota_item_targetvalue_update WHERE id='"+id+"'";
				excuteSQL(clearThisRecord);
			}
			
			for ( int i = 0 ; i < updateQuotaItems.size() - 1 ; i ++ ) {  
			    for ( int j = updateQuotaItems.size() - 1 ; j > i; j -- ) {  
			      if (updateQuotaItems.get(j).getId().equals(updateQuotaItems.get(i).getId())) {  
			    	  updateQuotaItems.remove(j);  
			      }   
			    }   
			}
			
			calculateCore.calculate(updateQuotaItems);
			quotaItemDao.setAllowSubmitStatus(updateQuotaItems);
			resultTableCreator.createOrUpdateResultTable(updateQuotaItems);
			
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
