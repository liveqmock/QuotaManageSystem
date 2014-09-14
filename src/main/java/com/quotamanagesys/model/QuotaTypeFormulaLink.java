package com.quotamanagesys.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.stereotype.Component;

@Component
@Entity
@Table(name = "QUOTA_TYPE_FORMULA_LINK")
public class QuotaTypeFormulaLink {
	
	@Id
	@GeneratedValue(generator = "systemUUID")
	@GenericGenerator(name = "systemUUID", strategy = "org.hibernate.id.UUIDGenerator")// 采用uuid的主键生成策略
	private String id;
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="QUOTA_TYPE_ID")
	private QuotaType quotaType;
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="QUOTA_FORMULA_ID")
	private QuotaFormula quotaFormula;
	@ManyToMany(fetch = FetchType.EAGER, targetEntity = FormulaParameter.class, cascade = CascadeType.ALL)
	@JoinTable(name = "PARAMETER_QUOTA_TYPE_MAP", joinColumns = { @JoinColumn(name = "QUOTA_FORMULA_LINK_ID") }, inverseJoinColumns = { @JoinColumn(name = "FORMULA_PARAMETER_ID") })
	private Set<FormulaParameter> formulaParameters;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public QuotaType getQuotaType() {
		return quotaType;
	}
	public void setQuotaType(QuotaType quotaType) {
		this.quotaType = quotaType;
	}
	public QuotaFormula getQuotaFormula() {
		return quotaFormula;
	}
	public void setQuotaFormula(QuotaFormula quotaFormula) {
		this.quotaFormula = quotaFormula;
	}
	public Set<FormulaParameter> getFormulaParameters() {
		return formulaParameters;
	}
	public void setFormulaParameters(Set<FormulaParameter> formulaParameters) {
		this.formulaParameters = formulaParameters;
	}
	
}
