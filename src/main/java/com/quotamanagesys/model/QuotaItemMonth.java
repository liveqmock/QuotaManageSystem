package com.quotamanagesys.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.stereotype.Component;

public class QuotaItemMonth implements Serializable{
	
	@Id
	@GeneratedValue(generator = "systemUUID")
	@GenericGenerator(name = "systemUUID", strategy = "org.hibernate.id.UUIDGenerator")// 采用uuid的主键生成策略
	@Column(name = "ID")
	private String id;
	@Column(name="ITEM_NAME")
	private String quotaItemName;//指标名称
	@Column(name="YEAR")
	private int year;//年度
	@Column(name="MONTH")
	private int month;//月度
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="QUOTA_COVER_ID")
	private QuotaCover quotaCover;//口径
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="QUOTA_TYPE_ID")
	private QuotaType quotaType;//指标种类
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="DIMENSION_ONE_ID")
	private QuotaDimensionOne quotaDimensionOne;//一维
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="DIMENSION_TWO_ID")
	private QuotaDimensionTwo quotaDimensionTwo;//二维
	@ManyToMany(fetch = FetchType.EAGER, targetEntity = QuotaProperty.class, cascade = CascadeType.ALL)
	@JoinTable(name = "PROPERTY_QUOTA_ITEM_MONTH_MAP", joinColumns = { @JoinColumn(name = "QUOTA_ITEM_MONTH_ID") }, inverseJoinColumns = { @JoinColumn(name = "QUOTA_PROPERTY_ID") })
	private Set<QuotaProperty> quotaProperties;//指标属性
	@OneToMany(fetch = FetchType.EAGER, targetEntity = QuotaPropertyValue.class, cascade = CascadeType.ALL)
	@JoinColumns(value = { @JoinColumn(name = "QUOTA_ITEM_ID", referencedColumnName = "ID")})
	private Set<QuotaPropertyValue> quotaPropertyValues;//指标属性目标值
	@Column(name="TARGET_VALUE")
	private double targetValue;//当月目标值
	@Column(name="VALUE")
	private double value;//完成值
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getQuotaItemName() {
		return quotaItemName;
	}
	public void setQuotaItemName(String quotaItemName) {
		this.quotaItemName = quotaItemName;
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
	public QuotaCover getQuotaCover() {
		return quotaCover;
	}
	public void setQuotaCover(QuotaCover quotaCover) {
		this.quotaCover = quotaCover;
	}
	public QuotaType getQuotaType() {
		return quotaType;
	}
	public void setQuotaType(QuotaType quotaType) {
		this.quotaType = quotaType;
	}
	public QuotaDimensionOne getQuotaDimensionOne() {
		return quotaDimensionOne;
	}
	public void setQuotaDimensionOne(QuotaDimensionOne quotaDimensionOne) {
		this.quotaDimensionOne = quotaDimensionOne;
	}
	public QuotaDimensionTwo getQuotaDimensionTwo() {
		return quotaDimensionTwo;
	}
	public void setQuotaDimensionTwo(QuotaDimensionTwo quotaDimensionTwo) {
		this.quotaDimensionTwo = quotaDimensionTwo;
	}
	public Set<QuotaProperty> getQuotaProperties() {
		return quotaProperties;
	}
	public void setQuotaProperties(Set<QuotaProperty> quotaProperties) {
		this.quotaProperties = quotaProperties;
	}
	public Set<QuotaPropertyValue> getQuotaPropertyValues() {
		return quotaPropertyValues;
	}
	public void setQuotaPropertyValues(Set<QuotaPropertyValue> quotaPropertyValues) {
		this.quotaPropertyValues = quotaPropertyValues;
	}
	public double getTargetValue() {
		return targetValue;
	}
	public void setTargetValue(double targetValue) {
		this.targetValue = targetValue;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	
}
