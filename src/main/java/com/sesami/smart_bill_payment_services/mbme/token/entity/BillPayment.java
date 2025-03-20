package com.sesami.smart_bill_payment_services.mbme.token.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "bill_payments")
public class BillPayment {
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
    private String responseCode;
    private String status;
    private String responseMessage;
    private String accountNumber;
    private String amount;
    private String custName;
    private String resField1;
    private String resField2;
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
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getCustName() {
		return custName;
	}
	public void setCustName(String custName) {
		this.custName = custName;
	}
	public String getResField1() {
		return resField1;
	}
	public void setResField1(String resField1) {
		this.resField1 = resField1;
	}
	public String getResField2() {
		return resField2;
	}
	public void setResField2(String resField2) {
		this.resField2 = resField2;
	}
	public BillPayment(Long id, String transactionId, String merchantId, String merchantLocation, String serviceId,
			String method, String lang, String reqField1, String responseCode, String status, String responseMessage,
			String accountNumber, String amount, String custName, String resField1, String resField2) {
		super();
		this.id = id;
		this.transactionId = transactionId;
		this.merchantId = merchantId;
		this.merchantLocation = merchantLocation;
		this.serviceId = serviceId;
		this.method = method;
		this.lang = lang;
		this.reqField1 = reqField1;
		this.responseCode = responseCode;
		this.status = status;
		this.responseMessage = responseMessage;
		this.accountNumber = accountNumber;
		this.amount = amount;
		this.custName = custName;
		this.resField1 = resField1;
		this.resField2 = resField2;
	}
	@Override
	public String toString() {
		return "BillPayment [id=" + id + ", transactionId=" + transactionId + ", merchantId=" + merchantId
				+ ", merchantLocation=" + merchantLocation + ", serviceId=" + serviceId + ", method=" + method
				+ ", lang=" + lang + ", reqField1=" + reqField1 + ", responseCode=" + responseCode + ", status="
				+ status + ", responseMessage=" + responseMessage + ", accountNumber=" + accountNumber + ", amount="
				+ amount + ", custName=" + custName + ", resField1=" + resField1 + ", resField2=" + resField2 + "]";
	}
	public BillPayment() {
		super();
		// TODO Auto-generated constructor stub
	}

    // Getters and setters
    
    
}