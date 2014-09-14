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
@Table(name = "HISTORY_VALUE_INSERT_SQL")
public class HistoryValueInsertSQL implements Serializable{

	@Id
	@GeneratedValue(generator = "systemUUID")
	@GenericGenerator(name = "systemUUID", strategy = "org.hibernate.id.UUIDGenerator")// 采用uuid的主键生成策略
	@Column(name = "ID")
	private String id;
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="QUOTA_HISTORY_BACKUP_ID")
	private QuotaHistoryBackUp quotaHistoryBackUp;
	@Column(name="VALUE_INSERT_SQL")
	private String valueInsertSQL;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public QuotaHistoryBackUp getQuotaHistoryBackUp() {
		return quotaHistoryBackUp;
	}
	public void setQuotaHistoryBackUp(QuotaHistoryBackUp quotaHistoryBackUp) {
		this.quotaHistoryBackUp = quotaHistoryBackUp;
	}
	public String getValueInsertSQL() {
		return valueInsertSQL;
	}
	public void setValueInsertSQL(String valueInsertSQL) {
		this.valueInsertSQL = valueInsertSQL;
	}
	
}
