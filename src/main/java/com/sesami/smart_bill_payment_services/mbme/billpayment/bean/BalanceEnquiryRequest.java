package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

import java.io.Serializable;
import java.util.List;

public class BalanceEnquiryRequest implements Serializable {

	private static final long serialVersionUID = -7600911099236737745L;
		private String category;
	    private String transactionId;
	    private String deviceId;
	    private String serviceCode;
	    private String serviceId;
	    private String serviceType; // balance / payment / inquiry / 
	    private String merchantId;
	    private String merchantLocation;
	    private String language;
	    private List<BalanceEnquiryDynamicRequestField> dynamicRequestFields;
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
		public List<BalanceEnquiryDynamicRequestField> getDynamicRequestFields() {
			return dynamicRequestFields;
		}
		public void setDynamicRequestFields(List<BalanceEnquiryDynamicRequestField> dynamicRequestFields) {
			this.dynamicRequestFields = dynamicRequestFields;
		}
		public String getServiceType() {
			return serviceType;
		}
		public void setServiceType(String serviceType) {
			this.serviceType = serviceType;
		}
		public String getServiceId() {
			return serviceId;
		}
		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
	    
	    
	    
}
