package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

import java.util.List;

public class BillPaymentRequest {
	private String category;
	private String transactionId;
	private String deviceId;
	private String serviceCode;
    private String serviceId;
    private String serviceType;
    private String merchantId;
    private String merchantLocation;
    private String language;
    private String method;
    private String paymentMode;
    private String amount;
    private List<DynamicRequestField> dynamicRequestFields;
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getServiceCode() {
		return serviceCode;
	}
	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
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
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public List<DynamicRequestField> getDynamicRequestFields() {
		return dynamicRequestFields;
	}
	public void setDynamicRequestFields(List<DynamicRequestField> dynamicRequestFields) {
		this.dynamicRequestFields = dynamicRequestFields;
	}
	public String getPaymentMode() {
		return paymentMode;
	}
	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	

    // Getters and setters
    
    
    
}
