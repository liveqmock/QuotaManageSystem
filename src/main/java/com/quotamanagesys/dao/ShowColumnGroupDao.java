package com.quotamanagesys.dao;

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
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.quotamanagesys.model.ShowColumn;
import com.quotamanagesys.model.ShowColumnGroup;

@Component
public class ShowColumnGroupDao extends HibernateDao {
	
	@Resource
	ShowColumnDao showColumnDao;

	@DataProvider
	public Collection<ShowColumnGroup> getAll(){
		String hqlString="from "+ShowColumnGroup.class.getName()+" order by sort asc";
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
	
	@DataResolver
	public void saveShowColumnGroups(Collection<ShowColumnGroup> showColumnGroups){
		Session session=this.getSessionFactory().openSession();
		try {
			for (ShowColumnGroup showColumnGroup : showColumnGroups) {
				EntityState state=EntityUtils.getState(showColumnGroup);
				if (state.equals(EntityState.NEW)) {
					session.save(showColumnGroup);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.update(showColumnGroup);
				}else if (state.equals(EntityState.DELETED)) {
					Collection<ShowColumn> showColumns=showColumnDao.getShowColumnsByGroup(showColumnGroup.getId());
					for (ShowColumn showColumn : showColumns) {
						showColumn.setShowColumnGroup(null);
						session.merge(showColumn);
						session.flush();
						session.clear();
					}
					session.delete(showColumnGroup);
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
}
