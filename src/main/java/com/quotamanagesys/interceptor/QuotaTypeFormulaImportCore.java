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
import com.quotamanagesys.dao.FormulaParameterDao;
import com.quotamanagesys.dao.QuotaFormulaDao;
import com.quotamanagesys.dao.QuotaTypeDao;
import com.quotamanagesys.dao.QuotaTypeFormulaLinkDao;
import com.quotamanagesys.model.FormulaParameter;
import com.quotamanagesys.model.QuotaFormula;
import com.quotamanagesys.model.QuotaType;

@Component
public class QuotaTypeFormulaImportCore extends HibernateDao {

	@Resource
	QuotaTypeDao quotaTypeDao;
	@Resource
	QuotaFormulaDao quotaFormulaDao;
	@Resource
	FormulaParameterDao formulaParameterDao;
	@Resource
	QuotaTypeFormulaLinkDao quotaTypeFormulaLinkDao;
	
	@Expose
	public void updateQuotaTypesFormulas() throws SQLException{
		Connection conn=getDBConnection();
		ResultSet rs=null;
		boolean isSuccess=true;

		try {
			rs=getResultSet(conn,"select * from quota_type_formula_update");
		} catch (Exception e) {
			isSuccess=false;
		}
		
		if (isSuccess) {
			while (rs.next()) {
				String quotaTypeName=rs.getString("quotaTypeName");
				String formulaTypeName=rs.getString("formulaTypeName");
				String formula=rs.getString("formula");
				String formulaParametersString=rs.getString("formulaParameters");
				
				String[] formulaParametersStringList=formulaParametersString.split(";");
				Collection<FormulaParameter> formulaParameters=new ArrayList<FormulaParameter>();
				for (String formulaParameterString: formulaParametersStringList) {
					FormulaParameter formulaParameter=formulaParameterDao.getFormulaParameterByParameterName(formulaParameterString);
					formulaParameters.add(formulaParameter);
				}
				
				QuotaType quotaType=quotaTypeDao.getQuotaTypeByName(quotaTypeName);
				
				String hqlString="from "+QuotaFormula.class.getName()+" where quotaFormulaResult.name='"+formulaTypeName+"'"
						+" and formula='"+formula+"'";
				List<QuotaFormula> quotaFormulas=this.query(hqlString);
				if (quotaFormulas.size()>0) {
					QuotaFormula quotaFormula=quotaFormulas.get(0);
					quotaTypeFormulaLinkDao.saveQuotaTypeFormulaLink(quotaType.getId(), quotaFormulas);
					quotaTypeFormulaLinkDao.saveQuotaFormulaLinkParameters(formulaParameters, quotaType.getId(), quotaFormula.getId());
				}	
				
				String clearThisRecord="DELETE FROM quota_type_formula_update WHERE quotaTypeName='"+quotaTypeName
						+"' AND formulaTypeName='"+formulaTypeName+"' AND formula='"+formula+"'";
				excuteSQL(clearThisRecord);
			}
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
