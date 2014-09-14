package com.quotamanagesys.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.DataProvider;
import com.quotamanagesys.model.QuotaHistoryBackUp;

@Component
public class QuotaHistoryBackupDao extends HibernateDao {

	@DataProvider
	public Collection<QuotaHistoryBackUp> getAll(){
		String hqlString="from "+QuotaHistoryBackUp.class.getName()+" order by year asc";
		Collection<QuotaHistoryBackUp> quotaHistoryBackUps=this.query(hqlString);
		return quotaHistoryBackUps;
	}
	
	@DataProvider
	public QuotaHistoryBackUp getQuotaHistoryBackUp(String id){
		String hqlString="from "+QuotaHistoryBackUp.class.getName()+" where id='"+id+"'";
		List<QuotaHistoryBackUp> quotaHistoryBackUps=this.query(hqlString);
		if (quotaHistoryBackUps.size()>0) {
			return quotaHistoryBackUps.get(0);
		}else {
			return null;
		}
	}
	
	@DataProvider
	public QuotaHistoryBackUp getQuotaHistoryBackUpByYear(int year){
		String hqlString="from "+QuotaHistoryBackUp.class.getName()+" where year="+year;
		List<QuotaHistoryBackUp> quotaHistoryBackUps=this.query(hqlString);
		if (quotaHistoryBackUps.size()>0) {
			return quotaHistoryBackUps.get(0);
		}else {
			return null;
		}
	}
}
