package com.sesami.smart_bill_payment_services.mbme.token.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "bill_payments_activity")
public class BillPaymentActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String activityType;
    private String requestJson;
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
		return "BillPaymentActivity [id=" + id + ", activityType=" + activityType + ", requestJson=" + requestJson
				+ ", responseJson=" + responseJson + ", timestamp=" + timestamp + "]";
	}
	public BillPaymentActivity(Long id, String activityType, String requestJson, String responseJson,
			LocalDateTime timestamp) {
		super();
		this.id = id;
		this.activityType = activityType;
		this.requestJson = requestJson;
		this.responseJson = responseJson;
		this.timestamp = timestamp;
	}
	public BillPaymentActivity() {
		super();
		// TODO Auto-generated constructor stub
	}

    // Getters and setters
    
    
}