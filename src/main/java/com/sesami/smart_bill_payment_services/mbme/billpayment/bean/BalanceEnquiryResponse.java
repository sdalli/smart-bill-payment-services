package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

import java.util.List;

public class BalanceEnquiryResponse {
	private String transactionId;
    private String currencyCode;
    private String responseCode;
    private String responseStatus;
    private String responseMessage;
    private String customerMessage;
    private String internalWebServiceCode;
    private String internalWebServiceDesc;
    private String dueAmount;
    private String minAmount;
    private String maxAmount;
    private String partialPayment;
    private String banknoteCut;
    private String changeHandling;
    private String customerCommission;
    private String commissionPercentage;
    private String commissionValue;
    private String commissionRoundingRules;
    private String serviceChargesVAT;
    private List<BalanceEnquiryDynamicResponseField> dynamicResponseFields;
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getResponseStatus() {
		return responseStatus;
	}
	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	public String getCustomerMessage() {
		return customerMessage;
	}
	public void setCustomerMessage(String customerMessage) {
		this.customerMessage = customerMessage;
	}
	public String getInternalWebServiceCode() {
		return internalWebServiceCode;
	}
	public void setInternalWebServiceCode(String internalWebServiceCode) {
		this.internalWebServiceCode = internalWebServiceCode;
	}
	public String getInternalWebServiceDesc() {
		return internalWebServiceDesc;
	}
	public void setInternalWebServiceDesc(String internalWebServiceDesc) {
		this.internalWebServiceDesc = internalWebServiceDesc;
	}
	public String getDueAmount() {
		return dueAmount;
	}
	public void setDueAmount(String dueAmount) {
		this.dueAmount = dueAmount;
	}
	public String getMinAmount() {
		return minAmount;
	}
	public void setMinAmount(String minAmount) {
		this.minAmount = minAmount;
	}
	public String getMaxAmount() {
		return maxAmount;
	}
	public void setMaxAmount(String maxAmount) {
		this.maxAmount = maxAmount;
	}
	public String getPartialPayment() {
		return partialPayment;
	}
	public void setPartialPayment(String partialPayment) {
		this.partialPayment = partialPayment;
	}
	public String getBanknoteCut() {
		return banknoteCut;
	}
	public void setBanknoteCut(String banknoteCut) {
		this.banknoteCut = banknoteCut;
	}
	public String getChangeHandling() {
		return changeHandling;
	}
	public void setChangeHandling(String changeHandling) {
		this.changeHandling = changeHandling;
	}
	public String getCustomerCommission() {
		return customerCommission;
	}
	public void setCustomerCommission(String customerCommission) {
		this.customerCommission = customerCommission;
	}
	public String getCommissionPercentage() {
		return commissionPercentage;
	}
	public void setCommissionPercentage(String commissionPercentage) {
		this.commissionPercentage = commissionPercentage;
	}
	public String getCommissionValue() {
		return commissionValue;
	}
	public void setCommissionValue(String commissionValue) {
		this.commissionValue = commissionValue;
	}
	public String getCommissionRoundingRules() {
		return commissionRoundingRules;
	}
	public void setCommissionRoundingRules(String commissionRoundingRules) {
		this.commissionRoundingRules = commissionRoundingRules;
	}
	public String getServiceChargesVAT() {
		return serviceChargesVAT;
	}
	public void setServiceChargesVAT(String serviceChargesVAT) {
		this.serviceChargesVAT = serviceChargesVAT;
	}
	public List<BalanceEnquiryDynamicResponseField> getDynamicResponseFields() {
		return dynamicResponseFields;
	}
	public void setDynamicResponseFields(List<BalanceEnquiryDynamicResponseField> dynamicResponseFields) {
		this.dynamicResponseFields = dynamicResponseFields;
	}
    
    
    
}
