package com.quotamanagesys.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.model.DefaultDept;
import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.Expose;
import com.quotamanagesys.dao.DepartmentDao;
import com.quotamanagesys.dao.QuotaItemDao;
import com.quotamanagesys.interceptor.ResultTableCreator;
import com.quotamanagesys.model.QuotaItem;

@Component
public class NotSubmitQuotaItemsOverTimeSetJobByMan extends HibernateDao{

	@Resource
	QuotaItemDao quotaItemDao;
	@Resource  
	DepartmentDao departmentDao;
	@Resource
	ResultTableCreator resultTableCreator;
	
	//对于未提交的指标，超过每月5日后自动置为超时指标
	@Expose
	public void execute(){	
		Collection<DefaultDept> depts=departmentDao.getAll();
		Collection<QuotaItem> notSubmitQuotaItems=new ArrayList<QuotaItem>();
		
		Calendar calendar=Calendar.getInstance();	
		//获取执行时的年月
		//当年
		int year=calendar.get(Calendar.YEAR);
		//上月
		int month=calendar.get(Calendar.MONTH);//calendar的真实月份需要+1,因为calendar的月份从0开始
	
		for (DefaultDept dept : depts) {
			//去年12月情况
			if (month==0) {
				Collection<QuotaItem> yearRateQuotaItemsOverTime=quotaItemDao.getQuotaItemsNotAllowSubmitByManageDept(dept.getId(), "年");
				Collection<QuotaItem> monthRateQuotaItemsOverTime=quotaItemDao.getQuotaItemsNotAllowSubmitByManageDept(dept.getId(), "月");
				if (yearRateQuotaItemsOverTime!=null) {
					notSubmitQuotaItems.addAll(yearRateQuotaItemsOverTime);
				}
				if (monthRateQuotaItemsOverTime!=null) {
					notSubmitQuotaItems.addAll(monthRateQuotaItemsOverTime);
				}
			}else {
				Collection<QuotaItem> monthRateQuotaItemsOverTime=quotaItemDao.getQuotaItemsNotAllowSubmitByManageDept(dept.getId(), "月");
				if (monthRateQuotaItemsOverTime!=null) {
					notSubmitQuotaItems.addAll(monthRateQuotaItemsOverTime);
				}
			}
		}
		
		if (notSubmitQuotaItems.size()>0) {
			Session session=this.getSessionFactory().openSession();
			try {
				for (QuotaItem quotaItem : notSubmitQuotaItems) {
					quotaItem.setOverTime(true);
					session.merge(quotaItem);
					session.flush();
				}
				resultTableCreator.createOrUpdateResultTable(notSubmitQuotaItems);
			} catch (Exception e) {
				System.out.print(e.toString());
			}finally{
				session.flush();
				session.close();
			}
		}
	}

}
