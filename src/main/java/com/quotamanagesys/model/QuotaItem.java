package com.quotamanagesys.model;

import java.io.Serializable;

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
	@Column(name="SAME_TERM_VALUE")
	private String sameTermValue;//同期值
	@Column(name="FINISH_VALUE")
	private String finishValue;//完成值
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="QUOTA_ITEM_ID")
	private QuotaItem fatherQuotaItem;//上级指标条目
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
	public QuotaItem getFatherQuotaItem() {
		return fatherQuotaItem;
	}
	public void setFatherQuotaItem(QuotaItem fatherQuotaItem) {
		this.fatherQuotaItem = fatherQuotaItem;
	}
}
