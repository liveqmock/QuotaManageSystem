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
@Table(name = "QUOTA_TARGET_VALUE")
public class QuotaTargetValue implements Serializable{

	@Id
	@GeneratedValue(generator = "systemUUID")
	@GenericGenerator(name = "systemUUID", strategy = "org.hibernate.id.UUIDGenerator")// 采用uuid的主键生成策略
	@Column(name = "ID")
	private String id;
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="PROPERTY_ID")
	private QuotaProperty quotaProperty;//指标属性种类
	@Column(name="PARAMETER_NAME")
	private String parameterName;//该属性在公式中对应的参数，由quotaProperty.parameterName+"_M"生成月度参数名称
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="QUOTA_ITEM_ID")
	private QuotaItem quotaItem;//关联该目标值的月度指标
	@Column(name="VALUE")
	private double value;//目标值
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public QuotaProperty getQuotaProperty() {
		return quotaProperty;
	}
	public void setQuotaProperty(QuotaProperty quotaProperty) {
		this.quotaProperty = quotaProperty;
	}
	public QuotaItem getQuotaItem() {
		return quotaItem;
	}
	public void setQuotaItem(QuotaItem quotaItem) {
		this.quotaItem = quotaItem;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public String getParameterName() {
		return parameterName;
	}
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

}
