package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

public class MbmeBillPaymentRequest {
	  	private String transactionId;
	    private String merchantId;
	    private String merchantLocation;
	    private String method;
	    private String serviceId;
	    private String paymentMode;
	    private String paidAmount;
	    private String lang;
	    private String reqField1;
	    private String reqField2;
	    private String reqField3;
	    private String reqField4;
	    
// 		Transaction Posting
//	    "transactionId": "2001011****12340066",
//	    "merchantId": "66",
//	    "merchantLocation": "DU POSTPAID HQ",
//	    "method": "pay",
//	    "serviceId": "103",
//	    "paymentMode": "Cash",
//	    "paidAmount":"408.35",
//	    "lang": "en",
//	    "reqField1": "1006658918",
//	    "reqField2": "CREDIT_ACCOUNT_PAY"
    

	    // Getters and setters
	    
	    
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
		public String getMethod() {
			return method;
		}
		public void setMethod(String method) {
			this.method = method;
		}
		public String getServiceId() {
			return serviceId;
		}
		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
		public String getPaymentMode() {
			return paymentMode;
		}
		public void setPaymentMode(String paymentMode) {
			this.paymentMode = paymentMode;
		}
		public String getPaidAmount() {
			return paidAmount;
		}
		public void setPaidAmount(String paidAmount) {
			this.paidAmount = paidAmount;
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
		public String getReqField2() {
			return reqField2;
		}
		public void setReqField2(String reqField2) {
			this.reqField2 = reqField2;
		}
		public String getReqField3() {
			return reqField3;
		}
		public void setReqField3(String reqField3) {
			this.reqField3 = reqField3;
		}
		public String getReqField4() {
			return reqField4;
		}
		public void setReqField4(String reqField4) {
			this.reqField4 = reqField4;
		}
	   
	    
}
