package com.sesami.smart_bill_payment_services.mbme.billpayment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "bill_inquiry_details")
public class BillInquiryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionId;
    private String merchantId;
    private String merchantLocation;
    private String serviceId;
    private String method;
    private String lang;
    private String reqField1;

    @Column(length = 8000)
    private String requestJson;

    @Column(length = 8000)
    private String responseJson;

    private LocalDateTime timestamp;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getMerchantLocation() {
		return merchantLocation;
	}

	public void setMerchantLocation(String merchantLocation) {
		this.merchantLocation = merchantLocation;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getReqField1() {
		return reqField1;
	}

	public void setReqField1(String reqField1) {
		this.reqField1 = reqField1;
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

	public BillInquiryEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BillInquiryEntity(Long id, String transactionId, String merchantId, String merchantLocation,
			String serviceId, String method, String lang, String reqField1, String requestJson, String responseJson,
			LocalDateTime timestamp) {
		super();
		this.id = id;
		this.transactionId = transactionId;
		this.merchantId = merchantId;
		this.merchantLocation = merchantLocation;
		this.serviceId = serviceId;
		this.method = method;
		this.lang = lang;
		this.reqField1 = reqField1;
		this.requestJson = requestJson;
		this.responseJson = responseJson;
		this.timestamp = timestamp;
	}

    // Getters and setters
    
    
    
}
