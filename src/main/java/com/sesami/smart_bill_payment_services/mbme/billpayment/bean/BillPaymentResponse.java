package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

import java.util.List;

public class BillPaymentResponse {
	private String deviceTransactionId;
	private String externalTransactionId;
    private String responseCode="";
    private String responseStatus="";
	private String status="";
	private String responseMessage="";
	private String customerMessage="";
    private String internalWebServiceCode="";
    private String internalWebServiceDesc="";
    private List<BillingCommonDynamicResponseField> dynamicResponseFields;
    
    
    
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
	
	public String getResponseStatus() {
		return responseStatus;
	}
	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}
	public List<BillingCommonDynamicResponseField> getDynamicResponseFields() {
		return dynamicResponseFields;
	}
	public void setDynamicResponseFields(List<BillingCommonDynamicResponseField> dynamicResponseFields) {
		this.dynamicResponseFields = dynamicResponseFields;
	}
	
	
	
    
    
		
	
	
}
