package com.quotamanagesys.dao;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.quotamanagesys.model.HistoryValueInsertSQL;

@Component
public class HistoryValueInsertSQLDao extends HibernateDao {

	@DataProvider
	public Collection<HistoryValueInsertSQL> getAll(){
		String hqlString="from "+HistoryValueInsertSQL.class.getName();
		Collection<HistoryValueInsertSQL> historyValueInsertSQLs=this.query(hqlString);
		return historyValueInsertSQLs;
	}
	
	@DataProvider
	public Collection<HistoryValueInsertSQL> getHistoryValueInsertSQLsByHistoryBackup(String quotaHistoryBackupId){
		String hqlString="from "+HistoryValueInsertSQL.class.getName()+" where quotaHistoryBackUp.id='"+quotaHistoryBackupId+"'";
		Collection<HistoryValueInsertSQL> historyValueInsertSQLs=this.query(hqlString);
		return historyValueInsertSQLs;
	}
}
