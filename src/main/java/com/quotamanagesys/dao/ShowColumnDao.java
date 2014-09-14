package com.quotamanagesys.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.DataResolver;
import com.bstek.dorado.annotation.Expose;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.Render;
import com.quotamanagesys.model.ShowColumn;
import com.quotamanagesys.model.ShowColumnGroup;

@Component
public class ShowColumnDao extends HibernateDao {
	
	@Resource
	ShowColumnGroupDao showColumnGroupDao;
	@Resource
	RenderDao renderDao;

	@DataProvider
	public Collection<ShowColumn> getAll(){
		String hqlString="from "+ShowColumn.class.getName()+" order by sort asc";
		Collection<ShowColumn> showColumns=this.query(hqlString);
		return showColumns;
	}
	
	@DataProvider
	public Collection<ShowColumn> getShowColumnsNotYetLinked(){
		String hqlString="from "+ShowColumn.class.getName()+" where showColumnGroup=null and visible=true order by sort asc";
		Collection<ShowColumn> showColumns=this.query(hqlString);
		return showColumns;
	}
	
	@DataProvider
	public Collection<ShowColumn> getShowColumnsByGroup(String showColumnGroupId){
		String hqlString="from "+ShowColumn.class.getName()+" where showColumnGroup.id='"+showColumnGroupId+"' order by sort asc";
		Collection<ShowColumn> showColumns=this.query(hqlString);
		return showColumns;
	}
	
	@DataProvider
	public Collection<ShowColumn> getShowColumnsByRender(String renderId){
		String hqlString="from "+ShowColumn.class.getName()+" where render.id='"+renderId+"'";
		Collection<ShowColumn> showColumns=this.query(hqlString);
		return showColumns;
	}
	
	@DataProvider
	public ShowColumn getShowColumn(String id){
		String hqlString="from "+ShowColumn.class.getName()+" where id='"+id+"'";
		List<ShowColumn> showColumns=this.query(hqlString);
		if (showColumns.size()>0) {
			return showColumns.get(0);
		}else {
			return null;
		}
	}
	
	@Expose
	public void initShowColumnsFromTable(){
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
			deleteShowColumns(getAll());
			
			Session session=this.getSessionFactory().openSession();
			try {
				for (int i = 1; i <= rsm.getColumnCount(); i++) {
					ShowColumn showColumn=new ShowColumn();
					showColumn.setName(rsm.getColumnName(i));
					showColumn.setAlias(rsm.getColumnName(i));
					showColumn.setWidth(70);
					showColumn.setVisible(true);
					showColumn.setWrappable(true);
					showColumn.setSort(i);
					session.save(showColumn);
				}
				conn.close();
			} catch (Exception e) {
				System.out.print(e.toString());
			}finally{
				session.flush();
				session.close();
			}
		}
	}
	
	@DataResolver
	public void updateShowColumns(Collection<ShowColumn> showColumns){
		Session session=this.getSessionFactory().openSession();
		try {
			for (ShowColumn showColumn : showColumns) {
				ShowColumn thisShowColumn=getShowColumn(showColumn.getId());
				Render render=showColumn.getRender();
				if (render!=null) {
					thisShowColumn.setRender(renderDao.getRender(render.getId()));
				}
				thisShowColumn.setAlias(showColumn.getAlias());
				thisShowColumn.setSort(showColumn.getSort());
				thisShowColumn.setVisible(showColumn.getVisible());
				thisShowColumn.setWrappable(showColumn.getWrappable());
				thisShowColumn.setWidth(showColumn.getWidth());
				session.merge(thisShowColumn);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@DataResolver
	public void saveShowColumnsWithGroup(Collection<ShowColumn> showColumns,String showColumnGroupId){
		Session session=this.getSessionFactory().openSession();
		ShowColumnGroup showColumnGroup=showColumnGroupDao.getShowColumnGroup(showColumnGroupId);
		try {
			for (ShowColumn showColumn : showColumns) {
				ShowColumn thisShowColumn=getShowColumn(showColumn.getId());
				EntityState state=EntityUtils.getState(showColumn);
				if (state.equals(EntityState.DELETED)) {
					thisShowColumn.setShowColumnGroup(null);
					session.merge(thisShowColumn);
				}else {
					thisShowColumn.setShowColumnGroup(showColumnGroup);
					session.merge(thisShowColumn);
				}
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@DataProvider
	public void deleteShowColumns(Collection<ShowColumn> showColumns){
		Session session=this.getSessionFactory().openSession();
		try {
			for (ShowColumn showColumn : showColumns) {
				showColumn.setShowColumnGroup(null);
				showColumn.setRender(null);
				session.delete(showColumn);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@DataResolver
	public void excuteHQL(String HQL) {
		Session session = this.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			Query query = session.createQuery(HQL);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
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
