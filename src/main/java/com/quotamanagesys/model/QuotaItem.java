package com.quotamanagesys.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.stereotype.Component;

@Component
@Entity
@Table(name = "QUOTA_ITEM")
public class QuotaItem implements Serializable{

	@Id
	@GeneratedValue(generator = "systemUUID")
	@GenericGenerator(name = "systemUUID", strategy = "org.hibernate.id.UUIDGenerator")// 采用uuid的主键生成策略
	@Column(name = "ID")
	private String id;
	@Column(name="YEAR")
	private int year;//指标年度
	@Column(name="MONTH")
	private int month;//指标月度
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="QUOTA_ITEM_CREATOR_ID")
	private QuotaItemCreator quotaItemCreator;//指标生成器
	@Column(name="ACCUMULATE_VALUE")
	private String accumulateValue;//累计值
	@Column(name="SAME_TERM_ACCUMULATE_VALUE")
	private String sameTermAccumulateValue;//同期累计值
	@Column(name="SAME_TERM_VALUE")
	private String sameTermValue;//同期值
	@Column(name="FINISH_VALUE")
	private String finishValue;//完成值 	
	@Column(name="FIRST_SUBMIT_TIME")
	private Date firstSubmitTime;//第一次填写时间
	@Column(name="LAST_SUBMIT_TIME")
	private Date lastSubmitTime;//最后一次填写时间
	@Column(name="USERNAME_OF_SUBMIT")
	private String usernameOfLastSubmit;//最后一次填写的人员的姓名
	@Column(name="IS_OVERTIME")
	private boolean overTime;//是否超时
	@Column(name="REDLIGHT_REASON")
	private String redLightReason;//异动原因
	@Column(name="IS_ALLOW_SUBMIT")
	private boolean allowSubmit;//是否允许提交，提交动作为：更新对应quota_item_view中的数值，提交条件为：填写值全部填写完毕，且亮红灯的指标异动原因已填写
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public QuotaItemCreator getQuotaItemCreator() {
		return quotaItemCreator;
	}
	public void setQuotaItemCreator(QuotaItemCreator quotaItemCreator) {
		this.quotaItemCreator = quotaItemCreator;
	}
	public String getAccumulateValue() {
		return accumulateValue;
	}
	public void setAccumulateValue(String accumulateValue) {
		this.accumulateValue = accumulateValue;
	}
	public String getSameTermAccumulateValue() {
		return sameTermAccumulateValue;
	}
	public void setSameTermAccumulateValue(String sameTermAccumulateValue) {
		this.sameTermAccumulateValue = sameTermAccumulateValue;
	}
	public String getSameTermValue() {
		return sameTermValue;
	}
	public void setSameTermValue(String sameTermValue) {
		this.sameTermValue = sameTermValue;
	}
	public String getFinishValue() {
		return finishValue;
	}
	public void setFinishValue(String finishValue) {
		this.finishValue = finishValue;
	}
	public Date getFirstSubmitTime() {
		return firstSubmitTime;
	}
	public void setFirstSubmitTime(Date firstSubmitTime) {
		this.firstSubmitTime = firstSubmitTime;
	}
	public Date getLastSubmitTime() {
		return lastSubmitTime;
	}
	public void setLastSubmitTime(Date lastSubmitTime) {
		this.lastSubmitTime = lastSubmitTime;
	}
	public String getUsernameOfLastSubmit() {
		return usernameOfLastSubmit;
	}
	public void setUsernameOfLastSubmit(String usernameOfLastSubmit) {
		this.usernameOfLastSubmit = usernameOfLastSubmit;
	}
	public boolean isOverTime() {
		return overTime;
	}
	public void setOverTime(boolean overTime) {
		this.overTime = overTime;
	}
	public String getRedLightReason() {
		return redLightReason;
	}
	public void setRedLightReason(String redLightReason) {
		this.redLightReason = redLightReason;
	}
	public boolean isAllowSubmit() {
		return allowSubmit;
	}
	public void setAllowSubmit(boolean allowSubmit) {
		this.allowSubmit = allowSubmit;
	}
}
