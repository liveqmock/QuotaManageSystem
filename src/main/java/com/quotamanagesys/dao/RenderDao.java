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
import com.quotamanagesys.model.Render;
import com.quotamanagesys.model.ShowColumn;

@Component
public class RenderDao extends HibernateDao {
	
	@Resource
	ShowColumnDao showColumnDao;

	@DataProvider
	public Collection<Render> getAll(){
		String hqlString="from "+Render.class.getName();
		Collection<Render> renders=this.query(hqlString);
		return renders;
	}
	
	@DataProvider
	public Collection<Render> getRendersByType(String type){
		String hqlString="from "+Render.class.getName()+" where type='"+type+"'";
		Collection<Render> renders=this.query(hqlString);
		return renders;
	}
	
	@DataProvider
	public Render getRender(String id){
		String hqlString="from "+Render.class.getName()+" where id='"+id+"'";
		List<Render> renders=this.query(hqlString);
		if (renders.size()>0) {
			return renders.get(0);
		}else{
			return null;
		}
	}
	
	@DataResolver
	public void saveRenders(Collection<Render> renders){
		Session session=this.getSessionFactory().openSession();
		try {
			for (Render render : renders) {
				EntityState state=EntityUtils.getState(render);
				if (state.equals(EntityState.NEW)) {
					session.save(render);
				}else if (state.equals(EntityState.MODIFIED)) {
					session.merge(render);
				}else if (state.equals(EntityState.DELETED)) {
					Collection<ShowColumn> showColumns=showColumnDao.getShowColumnsByRender(render.getId());
					for (ShowColumn showColumn : showColumns) {
						showColumn.setRender(null);
						session.merge(showColumn);
						session.flush();
						session.clear();
					}
					session.delete(render);
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
