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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.model.DefaultDept;

@Component
@Entity
@Table(name = "QUOTA_COVER")
public class QuotaCover implements Serializable{
	
	@Id
	@GeneratedValue(generator = "systemUUID")
	@GenericGenerator(name = "systemUUID", strategy = "org.hibernate.id.UUIDGenerator")// 采用uuid的主键生成策略
	@Column(name = "ID")
	private String id;
	@Column(name="NAME")
	private String name;//口径名称
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="FATHER_QUOTA_COVER_ID")
	private QuotaCover fatherQuotaCover;//上级口径
	@ManyToMany(fetch = FetchType.EAGER, targetEntity = DefaultDept.class, cascade = CascadeType.ALL)
	@JoinTable(name = "QUOTA_COVER_DUTY_DEPT_MAP", joinColumns = { @JoinColumn(name = "QUOTA_COVER_ID") }, inverseJoinColumns = { @JoinColumn(name = "DUTY_DEPT_ID") })
	private Set<DefaultDept> dutyDepts;//与该口径关联的部门
	@Column(name="SORT")
	private int sort;//排序
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public QuotaCover getFatherQuotaCover() {
		return fatherQuotaCover;
	}
	public void setFatherQuotaCover(QuotaCover fatherQuotaCover) {
		this.fatherQuotaCover = fatherQuotaCover;
	}
	public Set<DefaultDept> getDutyDepts() {
		return dutyDepts;
	}
	public void setDutyDepts(Set<DefaultDept> dutyDepts) {
		this.dutyDepts = dutyDepts;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
}
