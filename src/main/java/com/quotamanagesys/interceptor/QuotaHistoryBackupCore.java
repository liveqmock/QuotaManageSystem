package com.quotamanagesys.interceptor;

import java.util.Calendar;
import java.util.Collection;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.Expose;
import com.quotamanagesys.dao.HistoryValueInsertSQLDao;
import com.quotamanagesys.dao.QuotaHistoryBackupDao;
import com.quotamanagesys.dao.QuotaItemDao;
import com.quotamanagesys.model.HistoryValueInsertSQL;
import com.quotamanagesys.model.QuotaHistoryBackUp;
import com.quotamanagesys.model.QuotaItem;

@Component
public class QuotaHistoryBackupCore extends HibernateDao{
	
	@Resource
	ResultTableCreator resultTableCreator;
	@Resource
	QuotaHistoryBackupDao quotaHistoryBackupDao;
	@Resource
	HistoryValueInsertSQLDao historyValueInsertSQLDao;
	@Resource
	QuotaItemDao quotaItemDao;
	
	//创建指标信息备份记录
	@Expose
	public void createQuotaHistoryBackup(){
		Session session=this.getSessionFactory().openSession();
		try {
			QuotaHistoryBackUp quotaHistoryBackUp=new QuotaHistoryBackUp();
			Calendar calendar=Calendar.getInstance();
			
			//获取执行时的年月
			int year=calendar.get(Calendar.YEAR);
			int month=calendar.get(Calendar.MONTH);
			
			//当年从2月开始，才可执行上一年度指标信息备份操作
			if (month>1){
				Collection<QuotaItem> quotaItems=quotaItemDao.getQuotaItemsByYear(year-1);
				//如果上一年度指标都已清空，则无法执行上一年度指标信息备份操作
				if (quotaItems.size()>0) {
					//如果存在先前建立的备份，则将其删除，以生成最新版本备份
					deleteQuotaHistoryBackup(year-1);
					
					//指标信息备份包含的主要内容为建表语句、插入语句两大块
					String tableName="QUOTA_ITEM_VIEW_"+(year-1);
					String tableCreateSQL=resultTableCreator.getInitTableString(tableName);
					
					quotaHistoryBackUp.setYear(year-1);
					quotaHistoryBackUp.setName((year-1)+"年指标信息");
					quotaHistoryBackUp.setTableName(tableName);
					quotaHistoryBackUp.setTableCreateSQL(tableCreateSQL);
					session.save(quotaHistoryBackUp);
					session.flush();
					session.clear();
					
					for (QuotaItem quotaItem : quotaItems) {
						String valueInsertSQL=resultTableCreator.getInsertQuotaItemValuesIntoTableString(quotaItem,tableName);
						HistoryValueInsertSQL historyValueInsertSQL=new HistoryValueInsertSQL();
						historyValueInsertSQL.setQuotaHistoryBackUp(quotaHistoryBackUp);
						historyValueInsertSQL.setValueInsertSQL(valueInsertSQL);
						session.merge(historyValueInsertSQL);
						session.flush();
						session.clear();
					}
				}else {
					System.out.print("无法找到生成备份的数据");
				}
			}else {
				System.out.print("当前日期不可生成指标信息备份");
			}
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}

	//删除指定年度指标备份记录
	@Expose
	public void deleteQuotaHistoryBackup(int year){
		Session session=this.getSessionFactory().openSession();
		try {
			QuotaHistoryBackUp quotaHistoryBackUp=quotaHistoryBackupDao.getQuotaHistoryBackUpByYear(year);
			Collection<HistoryValueInsertSQL> historyValueInsertSQLs=historyValueInsertSQLDao.getHistoryValueInsertSQLsByHistoryBackup(quotaHistoryBackUp.getId());
			for (HistoryValueInsertSQL historyValueInsertSQL : historyValueInsertSQLs) {
				historyValueInsertSQL.setQuotaHistoryBackUp(null);
				session.delete(historyValueInsertSQL);
				session.flush();
				session.clear();
			}
			session.delete(quotaHistoryBackUp);
		} catch (Exception e) {
			System.out.print(e.toString());
		}finally{
			session.flush();
			session.close();
		}
	}
	
	//根据指标备份记录生成该记录年度对应的指标信息数据表
	@Expose
	public void createQuotaHistoryBackupTable(int year){
		QuotaHistoryBackUp quotaHistoryBackUp=quotaHistoryBackupDao.getQuotaHistoryBackUpByYear(year);
		if (!quotaHistoryBackUp.equals(null)) {
			excuteSQL("DROP TABLE IF EXISTS "+quotaHistoryBackUp.getTableName());
			excuteSQL(quotaHistoryBackUp.getTableCreateSQL());
			Collection<HistoryValueInsertSQL> historyValueInsertSQLs=historyValueInsertSQLDao.getHistoryValueInsertSQLsByHistoryBackup(quotaHistoryBackUp.getId());
			
			for (HistoryValueInsertSQL historyValueInsertSQL : historyValueInsertSQLs) {
				excuteSQL(historyValueInsertSQL.getValueInsertSQL());
			}
		}
	}
	
	//清除所有已经生成的指标信息数据表，释放数据库存储空间
	@Expose
	public void clearQuotaHistoryBackupTables(){
		Collection<QuotaHistoryBackUp> quotaHistoryBackUps=quotaHistoryBackupDao.getAll();
		for (QuotaHistoryBackUp quotaHistoryBackUp : quotaHistoryBackUps) {
			excuteSQL("DROP TABLE IF EXISTS "+quotaHistoryBackUp.getTableName());
		}
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
}
