package com.quotamanagesys.model;

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
@Table(name = "FORMULA_PARAMETER")
public class FormulaParameter {

	@Id
	@GeneratedValue(generator = "systemUUID")
	@GenericGenerator(name = "systemUUID", strategy = "org.hibernate.id.UUIDGenerator")// 采用uuid的主键生成策略
	private String id;
	@Column(name="RATE")
	private String rate;
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="QUOTA_PROPERTY_ID")
	private QuotaProperty quotaProperty;
	@Column(name="PARAMETER_NAME")
	private String parameterName;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRate() {
		return rate;
	}
	public void setRate(String rate) {
		this.rate = rate;
	}
	public String getParameterName() {
		return parameterName;
	}
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	public QuotaProperty getQuotaProperty() {
		return quotaProperty;
	}
	public void setQuotaProperty(QuotaProperty quotaProperty) {
		this.quotaProperty = quotaProperty;
	}
}
