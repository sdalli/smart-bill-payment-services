package com.sesami.smart_bill_payment_services.mbme.token.entity;

import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "token_activity")
public class TokenActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String activityType;
    @Column(length = 8000) // Increase the length as needed
    private String requestJson;
    @Column(length = 8000) // Increase the length as needed
    private String responseJson;
    private LocalDateTime timestamp;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getActivityType() {
		return activityType;
	}
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
	public String getRequestJson() {
		return requestJson;
	}
	public void setRequestJson(String requestJson) {
		this.requestJson = requestJson;
	}
	public String getResponseJson() {
		return responseJson;
	}
	public void setResponseJson(String responseJson) {
		this.responseJson = responseJson;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return "TokenActivity [id=" + id + ", activityType=" + activityType + ", requestJson=" + requestJson
				+ ", responseJson=" + responseJson + ", timestamp=" + timestamp + "]";
	}
	public TokenActivity(Long id, String activityType, String requestJson, String responseJson,
			LocalDateTime timestamp) {
		super();
		this.id = id;
		this.activityType = activityType;
		this.requestJson = requestJson;
		this.responseJson = responseJson;
		this.timestamp = timestamp;
	}
	public TokenActivity() {
		super();
		// TODO Auto-generated constructor stub
	}

    // Getters and setters
    
    
}