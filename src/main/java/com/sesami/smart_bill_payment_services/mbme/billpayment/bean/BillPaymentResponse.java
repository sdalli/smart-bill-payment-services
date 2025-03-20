package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

public class BillPaymentResponse {
	private String responseCode;
	private String status;
	private String responseMessage;
	private BillPaymentSubResponse responseData;
	
	
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
	public BillPaymentSubResponse getResponseData() {
		return responseData;
	}
	public void setResponseData(BillPaymentSubResponse responseData) {
		this.responseData = responseData;
	}
		
	
	
}
