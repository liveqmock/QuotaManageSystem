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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.stereotype.Component;

import com.bstek.bdf2.core.model.DefaultUser;

@Component
@Entity
@Table(name = "QUOTA_TYPE_VIEW_MAP")
public class QuotaTypeViewMap {

	@Id
	@GeneratedValue(generator = "systemUUID")
	@GenericGenerator(name = "systemUUID", strategy = "org.hibernate.id.UUIDGenerator")// 采用uuid的主键生成策略
	private String id;
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="USER_ID",unique=true)
	private DefaultUser user;
	@ManyToMany(fetch = FetchType.EAGER, targetEntity = QuotaType.class, cascade = CascadeType.ALL)
	@JoinTable(name = "CAN_VIEW_QUOTA_TYPE", joinColumns = { @JoinColumn(name = "QUOTA_TYPE_VIEW_MAP_ID") }, inverseJoinColumns = { @JoinColumn(name = "QUOTA_TYPE_ID") })
	private Set<QuotaType> canViewQuotaTypes;
	@ManyToMany(fetch = FetchType.EAGER, targetEntity = QuotaType.class, cascade = CascadeType.ALL)
	@JoinTable(name = "DEFAULT_VIEW_QUOTA_TYPE", joinColumns = { @JoinColumn(name = "QUOTA_TYPE_VIEW_MAP_ID") }, inverseJoinColumns = { @JoinColumn(name = "QUOTA_TYPE_ID") })
	private Set<QuotaType> defaultViewQuotaTypes;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public DefaultUser getUser() {
		return user;
	}
	public void setUser(DefaultUser user) {
		this.user = user;
	}
	public Set<QuotaType> getCanViewQuotaTypes() {
		return canViewQuotaTypes;
	}
	public void setCanViewQuotaTypes(Set<QuotaType> canViewQuotaTypes) {
		this.canViewQuotaTypes = canViewQuotaTypes;
	}
	public Set<QuotaType> getDefaultViewQuotaTypes() {
		return defaultViewQuotaTypes;
	}
	public void setDefaultViewQuotaTypes(Set<QuotaType> defaultViewQuotaTypes) {
		this.defaultViewQuotaTypes = defaultViewQuotaTypes;
	}
	
}