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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.stereotype.Component;

@Component
@Entity
@Table(name = "QUOTA_TYPE_RELATION")
public class QuotaTypeRelation implements Serializable{

	@Id
	@GeneratedValue(generator = "systemUUID")
	@GenericGenerator(name = "systemUUID", strategy = "org.hibernate.id.UUIDGenerator")// 采用uuid的主键生成策略
	@Column(name = "ID")
	private String id;
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "QUOTA_TYPE_ID", unique = true)
	private QuotaType mainQuotaType;//主指标种类
	@ManyToMany(fetch = FetchType.EAGER, targetEntity = QuotaType.class, cascade = CascadeType.ALL)
	@JoinTable(name = "SUB_QUOTA_TYPE_MAP", joinColumns = { @JoinColumn(name = "QUOTA_TYPE_RELATION_ID") }, inverseJoinColumns = { @JoinColumn(name = "SUB_QUOTA_TYPE_ID") })
	private Set<QuotaType> subQuotaTypes;//子指标种类
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public QuotaType getMainQuotaType() {
		return mainQuotaType;
	}
	public void setMainQuotaType(QuotaType mainQuotaType) {
		this.mainQuotaType = mainQuotaType;
	}
	public Set<QuotaType> getSubQuotaTypes() {
		return subQuotaTypes;
	}
	public void setSubQuotaTypes(Set<QuotaType> subQuotaTypes) {
		this.subQuotaTypes = subQuotaTypes;
	}
	
}
