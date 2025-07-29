package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

import java.util.List;

public class BillPaymentRequest {
	private String bankName;
	private String bankCode;
	private String category;
	private String deviceTransactionId;
	private String externalTransactionId;
	private String deviceId;
	private String serviceCode;
    private String serviceId;
    private String serviceType;
    private String merchantId;
    private String merchantLocation;
    private String language;
    private String paymentMode;
    private String paidAmount;
    private List<DynamicRequestField> dynamicRequestFields;
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getDeviceTransactionId() {
		return deviceTransactionId;
	}
	public void setDeviceTransactionId(String deviceTransactionId) {
		this.deviceTransactionId = deviceTransactionId;
	}
	public String getExternalTransactionId() {
		return externalTransactionId;
	}
	public void setExternalTransactionId(String externalTransactionId) {
		this.externalTransactionId = externalTransactionId;
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
	public String getPaymentMode() {
		return paymentMode;
	}
	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
	
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	public String getPaidAmount() {
		return paidAmount;
	}
	public void setPaidAmount(String paidAmount) {
		this.paidAmount = paidAmount;
	}
	public List<DynamicRequestField> getDynamicRequestFields() {
		return dynamicRequestFields;
	}
	public void setDynamicRequestFields(List<DynamicRequestField> dynamicRequestFields) {
		this.dynamicRequestFields = dynamicRequestFields;
	}
	
    // Getters and setters
    
    
    
}
