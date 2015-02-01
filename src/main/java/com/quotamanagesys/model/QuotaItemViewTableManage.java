package com.quotamanagesys.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.stereotype.Component;

@Component
@Entity
@Table(name = "QUOTA_ITEM_VIEW_TABLE_MANAGE")
public class QuotaItemViewTableManage {

	@Id
	@GeneratedValue(generator = "systemUUID")
	@GenericGenerator(name = "systemUUID", strategy = "org.hibernate.id.UUIDGenerator")// 采用uuid的主键生成策略
	@Column(name = "ID")
	String id;
	@Column(name="TABLE_NAME")
	String tableName;//生成规则为:quota_item_view_xxxx,xxxx为4位年度数字
	@Column(name="SHOW_NAME")
	String showName;//生成规则为:xxxx年指标信息总表,xxxx为4位年度数字
	@Column(name="YEAR")
	int year;//指标信息总表所属年度
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getShowName() {
		return showName;
	}
	public void setShowName(String showName) {
		this.showName = showName;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	
}
