package com.quotamanagesys.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
import com.quotamanagesys.model.QuotaItemViewTableManage;
import com.quotamanagesys.model.Render;
import com.quotamanagesys.model.ShowColumn;
import com.quotamanagesys.model.ShowColumnGroup;
import com.quotamanagesys.model.ShowColumnTrigger;

@Component
public class ShowColumnDao extends HibernateDao {
	
	@Resource
	ShowColumnGroupDao showColumnGroupDao;
	@Resource
	RenderDao renderDao;
	@Resource
	ShowColumnTriggerDao showColumnTriggerDao;
	@Resource
	QuotaItemViewTableManageDao quotaItemViewTableManageDao;

	@DataProvider
	public Collection<ShowColumn> getAll(){
		String hqlString="from "+ShowColumn.class.getName()+" order by sort asc";
		Collection<ShowColumn> showColumns=this.query(hqlString);
		return showColumns;
	}
	
	@DataProvider
	public Collection<ShowColumn> getShowColumnsByQuotaItemViewTableManage(String quotaItemViewTableManageId){
		if (quotaItemViewTableManageId==null) {
			return null;
		} else {
			String hqlString="from "+ShowColumn.class.getName()+" where quotaItemViewTableManage.id='"+quotaItemViewTableManageId+"' order by sort asc";
			Collection<ShowColumn> showColumns=this.query(hqlString);
			return showColumns;
		}
	}
	
	@DataProvider
	public Collection<ShowColumn> getShowColumnsNotYetLinkedByQuotaItemViewTableManage(String quotaItemViewTableManageId){
		if (quotaItemViewTableManageId==null) {
			return null;
		} else {
			String hqlString="from "+ShowColumn.class.getName()+" where showColumnGroup=null and visible=true and quotaItemViewTableManage.id='"+quotaItemViewTableManageId+"' order by sort asc";
			Collection<ShowColumn> showColumns=this.query(hqlString);
			return showColumns;
		}
	}
	
	@DataProvider
	public Collection<ShowColumn> getShowColumnsByGroup(String showColumnGroupId){
		if (showColumnGroupId==null) {
			return null;
		} else {
			String hqlString="from "+ShowColumn.class.getName()+" where showColumnGroup.id='"+showColumnGroupId+"' order by sort asc";
			Collection<ShowColumn> showColumns=this.query(hqlString);
			return showColumns;
		}
	}
	
	@DataProvider
	public Collection<ShowColumn> getShowColumnsByRender(String renderId){
		if (renderId==null) {
			return null;
		} else {
			String hqlString="from "+ShowColumn.class.getName()+" where render.id='"+renderId+"'";
			Collection<ShowColumn> showColumns=this.query(hqlString);
			return showColumns;
		}
	}
	
	@DataProvider
	public Collection<ShowColumn> getShowColumnsByTrigger(String triggerId){
		if (triggerId==null) {
			return null;
		} else {
			String hqlString="from "+ShowColumn.class.getName()+" where showColumnTrigger.id='"+triggerId+"'";
			Collection<ShowColumn> showColumns=this.query(hqlString);
			return showColumns;
		}
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
	
	/*复制列配置信息，toId为目标表管理器Id,fromId为复制源表管理器id，
	 * 所谓的表管理器为QuotaItemViewTableManage，
	 * 该类负责维护对应年度的指标信息总表，即quota_item_view_xxxx
	 */
	@Expose
	public void copyShowColumnsByQuotaItemViewTableManage(String toId,String fromId){
		Collection<ShowColumn> copys=getShowColumnsByQuotaItemViewTableManage(fromId);
		QuotaItemViewTableManage to=quotaItemViewTableManageDao.getItemViewTableManage(toId);
		Collection<ShowColumn> thisShowColumns=getShowColumnsByQuotaItemViewTableManage(toId);
		if (copys.size()>0) {
			if (thisShowColumns.size()>0) {
				Session session=this.getSessionFactory().openSession();
				try {
					for (ShowColumn thisShowColumn : thisShowColumns) {
						for (ShowColumn copyColumn : copys) {
							if (thisShowColumn.getName().equals(copyColumn.getName())) {
								thisShowColumn.setAlias(copyColumn.getAlias());
								thisShowColumn.setAlign(copyColumn.getAlign());
								thisShowColumn.setRender(copyColumn.getRender());
								
								ShowColumnGroup copyShowColumnGroup=copyColumn.getShowColumnGroup();
								if (copyShowColumnGroup!=null) {
									
									ShowColumnGroup thisShowColumnGroup=showColumnGroupDao.getShowColumnGroupByNameAndSortAndQuotaItemViewTableManage(copyShowColumnGroup.getName(),copyShowColumnGroup.getSort(),toId);
									if (thisShowColumnGroup!=null) {
										thisShowColumn.setShowColumnGroup(thisShowColumnGroup);
									}
								}
								
								thisShowColumn.setShowColumnTrigger(copyColumn.getShowColumnTrigger());
								thisShowColumn.setSort(copyColumn.getSort());
								thisShowColumn.setVisible(copyColumn.getVisible());
								thisShowColumn.setWidth(copyColumn.getWidth());
								thisShowColumn.setWrappable(copyColumn.getWrappable());
								session.merge(thisShowColumn);
								session.flush();
								session.clear();
								break;
							}else {
								continue;
							}
						}
					}
				} catch (Exception e) {
					System.out.print(e.toString());
				}finally{
					session.flush();
					session.close();
				}
			} else {
				System.out.print("目标对象为空，请核实是否完成字段抽取");
			}
		} else {
			System.out.print("没有可可复制的内容");
		}
	}
	
	@Expose
	public void initShowColumnsFromTable(String quotaItemViewTableManageId){
		Connection conn=getDBConnection();
		ResultSet rs=null;
		ResultSetMetaData rsm = null;
		boolean isSuccess=true;
		
		QuotaItemViewTableManage quotaItemViewTableManage=quotaItemViewTableManageDao.getItemViewTableManage(quotaItemViewTableManageId);
		
		try {
			String tableName=quotaItemViewTableManage.getTableName();
			rs=getResultSet(conn,"select * from "+tableName);
			rsm=rs.getMetaData();
		} catch (Exception e) {
			isSuccess=false;
		}
		
		if (isSuccess) {
			delete(quotaItemViewTableManageId, getShowColumnsByQuotaItemViewTableManage(quotaItemViewTableManageId));
			
			Session session=this.getSessionFactory().openSession();
			try {
				for (int i = 1; i <= rsm.getColumnCount(); i++) {
					ShowColumn showColumn=new ShowColumn();
					showColumn.setName(rsm.getColumnName(i));
					showColumn.setAlias(rsm.getColumnName(i));
					showColumn.setWidth(70);
					showColumn.setVisible(true);
					showColumn.setWrappable(true);
					showColumn.setAlign("left");
					showColumn.setSort(i);
					showColumn.setQuotaItemViewTableManage(quotaItemViewTableManage);
					session.merge(showColumn);
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
	public void saveShowColumns(String quotaItemViewTableManageId,Collection<ShowColumn> showColumns){
		Collection<ShowColumn> adds=new ArrayList<ShowColumn>();
		Collection<ShowColumn> updates=new ArrayList<ShowColumn>();
		Collection<ShowColumn> deletes=new ArrayList<ShowColumn>();
		
		for (ShowColumn showColumn : showColumns) {
			EntityState state=EntityUtils.getState(showColumn);
			if (state.equals(EntityState.NEW)) {
				adds.add(showColumn);
			}else if (state.equals(EntityState.MODIFIED)) {
				updates.add(showColumn);
			}else if (state.equals(EntityState.DELETED)) {
				deletes.add(showColumn);
			}
		}
		if (adds.size()>0) {
			//add(quotaItemViewTableManageId,adds);
		}
		if (updates.size()>0) {
			update(quotaItemViewTableManageId,updates);
		}
		if (deletes.size()>0) {
			delete(quotaItemViewTableManageId,deletes);
		}
	}
	
	@DataResolver
	public void update(String quotaItemViewTableManageId,Collection<ShowColumn> showColumns){
		Session session=this.getSessionFactory().openSession();
		QuotaItemViewTableManage quotaItemViewTableManage=quotaItemViewTableManageDao.getItemViewTableManage(quotaItemViewTableManageId);
		try {
			for (ShowColumn showColumn : showColumns) {
				ShowColumn thisShowColumn=getShowColumn(showColumn.getId());
				Render render=showColumn.getRender();
				if (render!=null) {
					thisShowColumn.setRender(renderDao.getRender(render.getId()));
				}
				ShowColumnTrigger showColumnTrigger=showColumn.getShowColumnTrigger();
				if (showColumnTrigger!=null) {
					thisShowColumn.setShowColumnTrigger(showColumnTriggerDao.getShowColumnTrigger(showColumnTrigger.getId()));
				}
				thisShowColumn.setAlias(showColumn.getAlias());
				thisShowColumn.setSort(showColumn.getSort());
				thisShowColumn.setVisible(showColumn.getVisible());
				thisShowColumn.setWrappable(showColumn.getWrappable());
				thisShowColumn.setWidth(showColumn.getWidth());
				thisShowColumn.setAlign(showColumn.getAlign());
				thisShowColumn.setQuotaItemViewTableManage(quotaItemViewTableManage);
				session.merge(thisShowColumn);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@DataProvider
	public void delete(String quotaItemViewTableManageId,Collection<ShowColumn> showColumns){
		Session session=this.getSessionFactory().openSession();
		try {
			for (ShowColumn showColumn : showColumns) {
				showColumn.setShowColumnGroup(null);
				showColumn.setRender(null);
				showColumn.setShowColumnTrigger(null);
				showColumn.setQuotaItemViewTableManage(null);
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
