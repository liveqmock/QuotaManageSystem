package com.quotamanagesys.dao;

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
import com.quotamanagesys.model.ShowColumn;
import com.quotamanagesys.model.ShowColumnGroup;

@Component
public class ShowColumnGroupDao extends HibernateDao {
	
	@Resource
	ShowColumnDao showColumnDao;
	@Resource
	QuotaItemViewTableManageDao quotaItemViewTableManageDao;

	@DataProvider
	public Collection<ShowColumnGroup> getAll(){
		String hqlString="from "+ShowColumnGroup.class.getName()+" order by sort asc";
		Collection<ShowColumnGroup> showColumnGroups=this.query(hqlString);
		return showColumnGroups;
	}
	
	@DataProvider
	public Collection<ShowColumnGroup> getShowColumnGroupsByQuotaItemViewTableManage(String quotaItemViewTableManageId){
		String hqlString="from "+ShowColumnGroup.class.getName()+" where quotaItemViewTableManage.id='"+quotaItemViewTableManageId+"' order by fatherShowColumnGroup.id asc and sort asc";
		Collection<ShowColumnGroup> showColumnGroups=this.query(hqlString);
		return showColumnGroups;
	}
	
	@DataProvider
	public Collection<ShowColumnGroup> getTopLevelShowColumnGroupsByQuotaItemViewTableManage(String quotaItemViewTableManageId){
		String hqlString="from "+ShowColumnGroup.class.getName()+" where fatherShowColumnGroup.id=null and quotaItemViewTableManage.id='"+quotaItemViewTableManageId+"' order by sort asc";
		Collection<ShowColumnGroup> showColumnGroups=this.query(hqlString);
		return showColumnGroups;
	}
	
	@DataProvider
	public ShowColumnGroup getShowColumnGroup(String id){
		String hqlString="from "+ShowColumnGroup.class.getName()+" where id='"+id+"'";
		List<ShowColumnGroup> showColumnGroups=this.query(hqlString);
		if (showColumnGroups.size()>0) {
			return showColumnGroups.get(0);
		}else {
			return null;
		}
	}
	
	@DataProvider
	public ShowColumnGroup getShowColumnGroupByNameAndSortAndQuotaItemViewTableManage(String name,int sort,String quotaItemViewTableManageId){
		String hqlString="from "+ShowColumnGroup.class.getName()+" where name='"+name+"' and sort="+sort+" and quotaItemViewTableManage.id='"+quotaItemViewTableManageId+"'";
		List<ShowColumnGroup> showColumnGroups=this.query(hqlString);
		if (showColumnGroups.size()>0) {
			return showColumnGroups.get(0);
		} else {
			return null;
		}
	}
	
	//获取上一级分组
	@DataProvider
	public ShowColumnGroup getFatherShowColumnGroup(String id){
		String hqlString="from "+ShowColumnGroup.class.getName()+" where id='"+id+"'";
		List<ShowColumnGroup> showColumnGroups=this.query(hqlString);
		if (showColumnGroups.size()>0) {
			return showColumnGroups.get(0).getFatherShowColumnGroup();
		} else {
			return null;
		}
	}
	
	//获取下一级分组
	@DataProvider
	public Collection<ShowColumnGroup> getChildrenShowColumnGroups(String id){
		String hqlString="from "+ShowColumnGroup.class.getName()+" where fatherShowColumnGroup.id='"+id+"' order by sort asc";
		Collection<ShowColumnGroup> showColumnGroups=this.query(hqlString);
		return showColumnGroups;
	}
	
	//递归获取所有上级分组
	public void getFatherTree(ShowColumnGroup showColumnGroup,Collection<ShowColumnGroup> fatherTree){
		ShowColumnGroup father=showColumnGroup.getFatherShowColumnGroup();
		if (father!=null) {
			fatherTree.add(father);
			getFatherTree(father, fatherTree);
		}
	}
	
	/*复制列分组信息，toId为目标表管理器Id,fromId为复制源表管理器id，
	 * 所谓的表管理器为QuotaItemViewTableManage，
	 * 该类负责维护对应年度的指标信息总表，即quota_item_view_xxxx
	 */
	@Expose
	public void copyShowColumnGroupsByQuotaItemViewTableManage(String toId,String fromId){
		Collection<ShowColumnGroup> copys=getShowColumnGroupsByQuotaItemViewTableManage(fromId);
		QuotaItemViewTableManage to=quotaItemViewTableManageDao.getItemViewTableManage(toId);
		if (copys.size()>0) {
			Collection<ShowColumnGroup> thiShowColumnGroups=getShowColumnGroupsByQuotaItemViewTableManage(toId);
			Collection<ShowColumnGroup> news=new ArrayList<ShowColumnGroup>();
			if (thiShowColumnGroups.size()>0) {
				delete(toId, thiShowColumnGroups);
				for (ShowColumnGroup copy : copys) {
					ShowColumnGroup showColumnGroup=new ShowColumnGroup();
					showColumnGroup.setName(copy.getName());
					showColumnGroup.setQuotaItemViewTableManage(to);
					showColumnGroup.setSort(copy.getSort());
					news.add(showColumnGroup);
				}
				add(toId, news);
			} else {
				for (ShowColumnGroup copy : copys) {
					ShowColumnGroup showColumnGroup=new ShowColumnGroup();
					showColumnGroup.setName(copy.getName());
					showColumnGroup.setQuotaItemViewTableManage(to);
					showColumnGroup.setSort(copy.getSort());
					news.add(showColumnGroup);
				}
				add(toId, news);
			}
		} else {
			System.out.print("没有可复制的内容");
		}
		copyShowColumnGroupsRelationByQuotaItemViewTableManage(toId, fromId);
	}
	
	//复制ShowColumnGroup之间的关系
	@Expose
	public void copyShowColumnGroupsRelationByQuotaItemViewTableManage(String toId,String fromId){
		Session session=this.getSessionFactory().openSession();
		Collection<ShowColumnGroup> targets=getShowColumnGroupsByQuotaItemViewTableManage(toId);
		
		try {
			for (ShowColumnGroup target : targets) {
				String hqlString="from "+ShowColumnGroup.class.getName()+" where name='"+target.getName()+"'"
						+" and sort="+target.getSort()
						+" and quotaItemViewTableManage.id='"+fromId+"'";
				List<ShowColumnGroup> copys=this.query(hqlString);
				if (copys.size()>0) {
					ShowColumnGroup copyFatherShowColumnGroup=copys.get(0).getFatherShowColumnGroup();
					if (copyFatherShowColumnGroup!=null) {
						String hqlString2="from "+ShowColumnGroup.class.getName()+" where name='"+copyFatherShowColumnGroup.getName()+"'"
								+" and sort="+copyFatherShowColumnGroup.getSort()
								+" and quotaItemViewTableManage.id='"+toId+"'";
						List<ShowColumnGroup> targetFathers=this.query(hqlString2);
						if (targetFathers.size()>0) {
							target.setFatherShowColumnGroup(targetFathers.get(0));
							session.merge(target);
							session.flush();
							session.clear();
						}
					}
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
	public void saveShowColumnGroups(String quotaItemViewTableManageId,Collection<ShowColumnGroup> showColumnGroups){
		Collection<ShowColumnGroup> adds=new ArrayList<ShowColumnGroup>();
		Collection<ShowColumnGroup> updates=new ArrayList<ShowColumnGroup>();
		Collection<ShowColumnGroup> deletes=new ArrayList<ShowColumnGroup>();
		
		for (ShowColumnGroup showColumnGroup : showColumnGroups) {
			EntityState state=EntityUtils.getState(showColumnGroup);
			if (state.equals(EntityState.NEW)) {
				adds.add(showColumnGroup);
			}else if (state.equals(EntityState.MODIFIED)) {
				updates.add(showColumnGroup);
			}else if (state.equals(EntityState.DELETED)) {
				deletes.add(showColumnGroup);
			}
		}
		if (adds.size()>0) {
			add(quotaItemViewTableManageId,adds);
		}
		if (updates.size()>0) {
			update(quotaItemViewTableManageId,updates);
		}
		if (deletes.size()>0) {
			delete(quotaItemViewTableManageId,deletes);
		}
	}
	
	
	@Expose
	public void add(String quotaItemViewTableManageId,Collection<ShowColumnGroup> showColumnGroups){
		Session session=this.getSessionFactory().openSession();
		QuotaItemViewTableManage quotaItemViewTableManage=quotaItemViewTableManageDao.getItemViewTableManage(quotaItemViewTableManageId);
		try {
			for (ShowColumnGroup showColumnGroup : showColumnGroups) {
				showColumnGroup.setQuotaItemViewTableManage(quotaItemViewTableManage);
				session.merge(showColumnGroup);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@Expose
	public void update(String quotaItemViewTableManageId,Collection<ShowColumnGroup> showColumnGroups){
		Session session=this.getSessionFactory().openSession();
		QuotaItemViewTableManage quotaItemViewTableManage=quotaItemViewTableManageDao.getItemViewTableManage(quotaItemViewTableManageId);
		try {
			for (ShowColumnGroup showColumnGroup : showColumnGroups) {
				ShowColumnGroup thisShowColumnGroup=getShowColumnGroup(showColumnGroup.getId());
				thisShowColumnGroup.setName(showColumnGroup.getName());
				thisShowColumnGroup.setSort(showColumnGroup.getSort());
				ShowColumnGroup fatherShowColumnGroup=showColumnGroup.getFatherShowColumnGroup();
				if (fatherShowColumnGroup!=null) {
					thisShowColumnGroup.setFatherShowColumnGroup(getShowColumnGroup(fatherShowColumnGroup.getId()));
				}	
				thisShowColumnGroup.setQuotaItemViewTableManage(quotaItemViewTableManage);
				session.merge(thisShowColumnGroup);
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	@Expose
	public void delete(String quotaItemViewTableManageId,Collection<ShowColumnGroup> showColumnGroups){
		Session session=this.getSessionFactory().openSession();
		try {
			for (ShowColumnGroup showColumnGroup : showColumnGroups) {
				Collection<ShowColumn> showColumns=showColumnDao.getShowColumnsByGroup(showColumnGroup.getId());
				for (ShowColumn showColumn : showColumns) {
					showColumn.setShowColumnGroup(null);
					session.merge(showColumn);
					session.flush();
					session.clear();
				}
				Collection<ShowColumnGroup> children=getChildrenShowColumnGroups(showColumnGroup.getId());
				for (ShowColumnGroup child : children) {
					child.setFatherShowColumnGroup(null);
					session.merge(child);
					session.flush();
					session.clear();
				}
				ShowColumnGroup thisShowColumnGroup=getShowColumnGroup(showColumnGroup.getId());
				thisShowColumnGroup.setQuotaItemViewTableManage(null);
				session.delete(thisShowColumnGroup);
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
}
