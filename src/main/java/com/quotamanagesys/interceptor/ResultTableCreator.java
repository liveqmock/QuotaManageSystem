package com.quotamanagesys.interceptor;

import java.util.Collection;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.orm.hibernate.HibernateDao;
import com.bstek.dorado.annotation.Expose;
import com.quotamanagesys.dao.QuotaFormulaResultDao;
import com.quotamanagesys.dao.QuotaFormulaResultValueDao;
import com.quotamanagesys.dao.QuotaItemDao;
import com.quotamanagesys.dao.QuotaPropertyDao;
import com.quotamanagesys.dao.QuotaPropertyValueDao;
import com.quotamanagesys.model.QuotaFormulaResult;
import com.quotamanagesys.model.QuotaFormulaResultValue;
import com.quotamanagesys.model.QuotaItem;
import com.quotamanagesys.model.QuotaItemCreator;
import com.quotamanagesys.model.QuotaProperty;
import com.quotamanagesys.model.QuotaPropertyValue;
import com.quotamanagesys.model.QuotaType;

@Component
public class ResultTableCreator extends HibernateDao{

	@Resource
	QuotaPropertyDao quotaPropertyDao;
	@Resource
	QuotaFormulaResultDao quotaFormulaResultDao;
	@Resource
	QuotaFormulaResultValueDao quotaFormulaResultValueDao;
	@Resource
	QuotaPropertyValueDao quotaPropertyValueDao;
	@Resource
	QuotaItemDao quotaItemDao;
	
	@Expose
	public void creatorResultTable(int year){
		excuteSQL("DROP TABLE IF EXISTS QUOTA_ITEM_VIEW");
		excuteSQL(getInitTableString());
		Collection<QuotaItem> quotaItems=quotaItemDao.getQuotaItemsByYear(year);
		for (QuotaItem quotaItem : quotaItems) {
			excuteSQL(getInsertQuotaItemValuesIntoTableString(quotaItem));
		}
	}
	
	String getInsertQuotaItemValuesIntoTableString(QuotaItem quotaItem){
		QuotaItemCreator quotaItemCreator=quotaItem.getQuotaItemCreator();
		QuotaType quotaType=quotaItemCreator.getQuotaType();
		String sqlString="INSERT INTO QUOTA_ITEM_VIEW(";
		String staticColumnsString;
		String staticValuesString;
		if (quotaItemCreator.getQuotaDimensionTwo()==null) {
			staticColumnsString="指标名称,"
					+"年度,"
					+ "季度,"
					+ "月度,"
					+ "指标专业,"
					+ "指标级别,"
					+ "计量单位,"
					+ "小数位数,"
					+ "考核频率,"
					+ "管理部门,"
					+ "责任部门,"
					+ "口径,"
					+ "一级维度,"
					+ "目标值,"
					+ "完成值";
			staticValuesString="'"+quotaItemCreator.getName()+"',"
					+quotaItem.getYear()+","
					+quotaItem.getQuarter()+","
					+quotaItem.getMonth()+","
					+"'"+quotaType.getQuotaProfession().getProfession()+"',"
					+"'"+quotaType.getQuotaLevel().getQuotaLevel()+"',"
					+"'"+quotaType.getQuotaUnit().getQuotaUnit()+"',"
					+quotaType.getDigit()+","
					+"'"+quotaType.getRate()+"',"
					+"'"+quotaType.getManageDept().getName()+"',"
					+"'"+quotaItemCreator.getQuotaDutyDept().getName()+"',"
					+"'"+quotaItemCreator.getQuotaCover().getName()+"',"
					+"'"+quotaItemCreator.getQuotaDimensionOne().getDimensionName()+"',"
					+quotaItem.getTargetValue()+","
					+quotaItem.getFinishValue();
		}else {
			staticColumnsString="指标名称,"
					+"年度,"
					+ "季度,"
					+ "月度,"
					+ "指标专业,"
					+ "指标级别,"
					+ "计量单位,"
					+ "小数位数,"
					+ "考核频率,"
					+ "管理部门,"
					+ "责任部门,"
					+ "口径,"
					+ "一级维度,"
					+ "二级维度,"
					+ "目标值,"
					+ "完成值";
			staticValuesString="'"+quotaItemCreator.getName()+"',"
					+quotaItem.getYear()+","
					+quotaItem.getQuarter()+","
					+quotaItem.getMonth()+","
					+"'"+quotaType.getQuotaProfession().getProfession()+"',"
					+"'"+quotaType.getQuotaLevel().getQuotaLevel()+"',"
					+"'"+quotaType.getQuotaUnit().getQuotaUnit()+"',"
					+quotaType.getDigit()+","
					+"'"+quotaType.getRate()+"',"
					+"'"+quotaType.getManageDept().getName()+"',"
					+"'"+quotaItemCreator.getQuotaDutyDept().getName()+"',"
					+"'"+quotaItemCreator.getQuotaCover().getName()+"',"
					+"'"+quotaItemCreator.getQuotaDimensionOne().getDimensionName()+"',"
					+"'"+quotaItemCreator.getQuotaDimensionTwo().getDimensionName()+"',"
					+quotaItem.getTargetValue()+","
					+quotaItem.getFinishValue();
		}
		
		
		String dynamicColumnsString="";
		String dynamicValuesString="";
		Collection<QuotaPropertyValue> quotaPropertyValues=quotaPropertyValueDao.getQuotaPropertyValuesByQuotaItemCreator(quotaItem.getQuotaItemCreator().getId());
		if (quotaPropertyValues.size()>0) {
			for (QuotaPropertyValue quotaPropertyValue : quotaPropertyValues) {
				if (dynamicColumnsString=="") {
					dynamicColumnsString=quotaPropertyValue.getQuotaProperty().getName();
					dynamicValuesString=quotaPropertyValue.getValue()+"";
				}else {
					dynamicColumnsString=dynamicColumnsString+","+quotaPropertyValue.getQuotaProperty().getName();
					dynamicValuesString=dynamicValuesString+","+quotaPropertyValue.getValue();
				}
			}
		}
		Collection<QuotaFormulaResultValue> quotaFormulaResultValues=quotaFormulaResultValueDao.getQuotaFormulaResultValuesByQuotaItem(quotaItem.getId());
		if (quotaFormulaResultValues.size()>0) {
			for (QuotaFormulaResultValue quotaFormulaResultValue : quotaFormulaResultValues) {
				if (dynamicColumnsString=="") {
					dynamicColumnsString=quotaFormulaResultValue.getQuotaFormulaResult().getResultName();
					dynamicValuesString=quotaFormulaResultValue.getValue();
				}else {
					dynamicColumnsString=dynamicColumnsString+","+quotaFormulaResultValue.getQuotaFormulaResult().getResultName();
					dynamicValuesString=dynamicValuesString+","+quotaFormulaResultValue.getValue();
				}
			}
		}
		
		if (dynamicColumnsString=="") {
			sqlString=sqlString+staticColumnsString+") VALUES("+staticValuesString+")";
		}else {
			sqlString=sqlString+staticColumnsString+","+dynamicColumnsString+") VALUES("+staticValuesString+","+dynamicValuesString+")";
		}
		return sqlString;
	}
	
	String getInitTableString(){
		String sqlString="CREATE TABLE QUOTA_ITEM_VIEW(";
		String staticColumnsString="指标名称 VARCHAR(255),"
				+ "年度 INT,"
				+ "季度 INT,"
				+ "月度 INT,"
				+ "指标专业 VARCHAR(255),"
				+ "指标级别 VARCHAR(255),"
				+ "计量单位 VARCHAR(255),"
				+ "小数位数 INT,"
				+ "考核频率 VARCHAR(255),"
				+ "管理部门 VARCHAR(255),"
				+ "责任部门 VARCHAR(255),"
				+ "口径 VARCHAR(255),"
				+ "一级维度 VARCHAR(255),"
				+ "二级维度 VARCHAR(255),"
				+ "目标值 VARCHAR(255),"
				+ "完成值 VARCHAR(255)";
		String dynamicColumnsString="";
		Collection<QuotaProperty> quotaProperties=quotaPropertyDao.getAll();
		if (quotaProperties.size()>0) {
			for (QuotaProperty quotaProperty : quotaProperties) {
				if (dynamicColumnsString=="") {
					dynamicColumnsString=quotaProperty.getName()+" VARCHAR(255)";
				}else {
					dynamicColumnsString=dynamicColumnsString+","+quotaProperty.getName()+" VARCHAR(255)";
				}
			}
		}	
		Collection<QuotaFormulaResult> quotaFormulaResults=quotaFormulaResultDao.getAll();
		if (quotaFormulaResults.size()>0) {
			for (QuotaFormulaResult quotaFormulaResult : quotaFormulaResults) {
				if (dynamicColumnsString=="") {
					dynamicColumnsString=quotaFormulaResult.getResultName()+" VARCHAR(255)";
				}else {
					dynamicColumnsString=dynamicColumnsString+","+quotaFormulaResult.getResultName()+" VARCHAR(255)";
				}
			}
		}
		if (dynamicColumnsString=="") {
			sqlString=sqlString+staticColumnsString+dynamicColumnsString+"}";
		}else {
			sqlString=sqlString+staticColumnsString+","+dynamicColumnsString+")";
		}
		return sqlString;
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
