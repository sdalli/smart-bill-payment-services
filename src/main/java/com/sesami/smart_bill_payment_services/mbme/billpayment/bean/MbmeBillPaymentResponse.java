package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

public class MbmeBillPaymentResponse {
	
	
	
	 	private String responseCode;
	    private String status;
	    private String responseMessage;
	    private MbmePaymentSubResponse responseData;
	    
	    
	   /* {
	        "responseCode": "000",
	        "status": "SUCCESS",
	        "responseMessage": "SUCCESS",
	        "responseData": {
	                "transactionId":"200101120012340066",
	                "amountPaid": "",
	                "providerTransactionId": "",
	                "resField1": "ck48ql8lfagwvegujckp6fvfvb48"
	        }
	    }*/
	    
	    
	    
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
		public MbmePaymentSubResponse getResponseData() {
			return responseData;
		}
		public void setResponseData(MbmePaymentSubResponse responseData) {
			this.responseData = responseData;
		}
	    
	    
}
