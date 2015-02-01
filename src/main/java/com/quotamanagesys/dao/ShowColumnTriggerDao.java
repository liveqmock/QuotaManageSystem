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
import com.quotamanagesys.model.ShowColumnTrigger;

@Component
public class ShowColumnTriggerDao extends HibernateDao {

	@Resource
	ShowColumnDao showColumnDao;
	
	@DataProvider
	public Collection<ShowColumnTrigger> getAll(){
		String hqlString="from "+ShowColumnTrigger.class.getName();
		Collection<ShowColumnTrigger> showColumnTriggers=this.query(hqlString);
		return showColumnTriggers;
	}
	
	@DataProvider
	public ShowColumnTrigger getShowColumnTrigger(String id){
		String hqlString="from "+ShowColumnTrigger.class.getName()+" where id='"+id+"'";
		List<ShowColumnTrigger> showColumnTriggers=this.query(hqlString);
		if (showColumnTriggers.size()>0) {
			return showColumnTriggers.get(0);
		} else {
			return null;
		}
	}
	
	
	@DataResolver
	public void saveShowColumnTriggers(Collection<ShowColumnTrigger> showColumnTriggers){
		Session session=this.getSessionFactory().openSession();
		try {
			for (ShowColumnTrigger showColumnTrigger : showColumnTriggers) {
				EntityState state=EntityUtils.getState(showColumnTrigger);
				if (state.equals(EntityState.NEW)) {
					session.save(showColumnTrigger);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.merge(showColumnTrigger);
				}else if (state.equals(EntityState.DELETED)) {
					Collection<ShowColumn> showColumns=showColumnDao.getShowColumnsByTrigger(showColumnTrigger.getId());
					for (ShowColumn showColumn : showColumns) {
						showColumn.setShowColumnTrigger(null);
						session.merge(showColumn);
						session.flush();
						session.clear();
					}
					session.delete(showColumnTrigger);
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
