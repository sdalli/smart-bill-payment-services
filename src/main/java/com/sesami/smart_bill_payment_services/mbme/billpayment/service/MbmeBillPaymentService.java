package com.sesami.smart_bill_payment_services.mbme.billpayment.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillPaymentRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillPaymentResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillingCommonDynamicResponseField;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.MbmeBillPaymentRequest;
import com.sesami.smart_bill_payment_services.mbme.token.service.TokenService;

@Service
public class MbmeBillPaymentService {
	private static final Logger logger = LoggerFactory.getLogger(MbmeBillPaymentService.class);
    
//	@Autowired
//    private BillPaymentRepository billPaymentRepository;

	@Autowired
	private TokenService tokenService;
	   
	   
	@Autowired
	private RestTemplate restTemplate;

	@Value("${mbme.api.balance-payment.url}")
	private String billPaymentUrl;
    
    public BillPaymentResponse processBillPayment(BillPaymentRequest billPaymentRequest) throws IOException {
    	logger.info("Processing processMbmeBillPayment request for transactionId: {}", billPaymentRequest.getDeviceTransactionId());
    	BillPaymentResponse billPaymentResponse = null;
    	MbmeBillPaymentRequest mbmeBillPaymentService = new MbmeBillPaymentRequest();
    	mbmeBillPaymentService.setTransactionId(billPaymentRequest.getExternalTransactionId());
    	mbmeBillPaymentService.setMerchantId(billPaymentRequest.getMerchantId());
    	mbmeBillPaymentService.setMerchantLocation(billPaymentRequest.getMerchantLocation());
    	mbmeBillPaymentService.setMethod(billPaymentRequest.getMethod());
    	mbmeBillPaymentService.setServiceId(billPaymentRequest.getServiceId());
    	mbmeBillPaymentService.setPaymentMode(billPaymentRequest.getPaymentMode());
    	mbmeBillPaymentService.setPaidAmount(billPaymentRequest.getPaidAmount());
    	mbmeBillPaymentService.setLang("en");
    	
    	
    	mbmeBillPaymentService.setReqField1(billPaymentRequest.getDynamicRequestFields().get(0).getValue());
    	mbmeBillPaymentService.setReqField2(billPaymentRequest.getDynamicRequestFields().get(0).getName());
    	
    	 if(Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
  			   && !mbmeBillPaymentService.getServiceId().isEmpty() && mbmeBillPaymentService.getServiceId().equalsIgnoreCase("103")) {
    		 mbmeBillPaymentService.setReqField1(billPaymentRequest.getDynamicRequestFields().get(0).getValue());
  		  // reqMbmeBillInquiryRequest.setReqField2(balanceEnquiryRequest.getDynamicRequestFields().get(1).getValue());
  	   }else if(Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
  			   && !mbmeBillPaymentService.getServiceId().isEmpty() && mbmeBillPaymentService.getServiceId().equalsIgnoreCase("19")) {
  		 mbmeBillPaymentService.setReqField1(billPaymentRequest.getDynamicRequestFields().get(0).getValue());
  		mbmeBillPaymentService.setReqField2(billPaymentRequest.getDynamicRequestFields().get(1).getValue()); 
  		mbmeBillPaymentService.setReqField3(billPaymentRequest.getDynamicRequestFields().get(2).getValue()); 
  		mbmeBillPaymentService.setReqField4(billPaymentRequest.getDynamicRequestFields().get(3).getValue()); 
  	   }
    	
    	 String requestJson= null;
    	 if(Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
    			   && !mbmeBillPaymentService.getServiceId().isEmpty() && mbmeBillPaymentService.getServiceId().equalsIgnoreCase("103")) {
    		requestJson = "{\r\n"
    				+ "    \"transactionId\": \""+billPaymentRequest.getExternalTransactionId()+"\",\r\n"
    				+ "    \"merchantId\": \""+billPaymentRequest.getMerchantId()+"\",\r\n"
    				+ "    \"merchantLocation\": \""+billPaymentRequest.getMerchantLocation()+"\",\r\n"
    				+ "    \"method\": \"pay\",\r\n"
    				+ "    \"serviceId\": \""+billPaymentRequest.getServiceId()+"\",\r\n"
    				+ "    \"paymentMode\": \"Cash\",\r\n"
    				+ "    \"paidAmount\":\""+billPaymentRequest.getPaidAmount()+"\",\r\n"
    				+ "    \"lang\": \"en\",\r\n"
    				+ "    \"reqField1\": \""+billPaymentRequest.getDynamicRequestFields().get(0).getValue()+"\",\r\n"
    				+ "    \"reqField2\": \"CREDIT_ACCOUNT_PAY\"\r\n"
    				+ "}";
    		 
    	 }else if(Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
    			   && !mbmeBillPaymentService.getServiceId().isEmpty() && mbmeBillPaymentService.getServiceId().equalsIgnoreCase("19")) {
    		 requestJson="{\r\n"
    					+ "    \"transactionId\": \""+billPaymentRequest.getExternalTransactionId()+"\",\r\n"
    					+ "    \"merchantId\": \""+billPaymentRequest.getMerchantId()+"\",\r\n"
    					+ "    \"merchantLocation \": \""+billPaymentRequest.getMerchantLocation()+"\",\r\n"
    					+ "    \"serviceId\": \""+billPaymentRequest.getServiceId()+"\",\r\n"
    					+ "    \"method\": \"pay\",\r\n"
    					+ "    \"paymentMode\": \"Cash\",\r\n"
    					+ "    \"paidAmount\":\""+billPaymentRequest.getPaidAmount()+"\",\r\n"
    					+ "    \"lang\": \"en\",\r\n"
    					+ "    \"reqField1\": \""+billPaymentRequest.getDynamicRequestFields().get(0).getValue()+"\",\r\n"
    					+ "    \"reqField2\": \""+billPaymentRequest.getDynamicRequestFields().get(1).getValue()+"\",\r\n"
    					+ "    \"reqField3\": \""+billPaymentRequest.getDynamicRequestFields().get(2).getValue()+"\",\r\n"
    					+ "    \"reqField4\": \""+billPaymentRequest.getDynamicRequestFields().get(3).getValue()+"\"\r\n"
    					+ "}";
    	 }else {
    	 }
    	 

    	// String url = "https://{{URL}}/du/bill/payment";
    	 HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         headers.set("Authorization", "Bearer " + tokenService.getValidToken().getAccessToken());
         ObjectMapper objectMapper = new ObjectMapper();
        // String jsonRequest = objectMapper.writeValueAsString(mbmeBillPaymentService);
         HttpEntity<String> httpRequest = new HttpEntity<>(requestJson, headers);

        // LocalDateTime requestTimestamp = LocalDateTime.now();
         logger.info("Processing processMbmeBillPayment request for MBME Bill payment service ::: {}", requestJson);
         ResponseEntity<String> response = restTemplate.exchange(billPaymentUrl, HttpMethod.POST, httpRequest, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
//        	MbmeBillPaymentResponse mbmeBillPaymentResponse = objectMapper.readValue(response.getBody(), MbmeBillPaymentResponse.class);
//        	billPaymentResponse = new BillPaymentResponse();
//        	billPaymentResponse.setResponseCode(mbmeBillPaymentResponse.getResponseCode());
//        	billPaymentResponse.setResponseMessage(mbmeBillPaymentResponse.getResponseMessage());
//        	billPaymentResponse.setStatus(mbmeBillPaymentResponse.getStatus());
//        	BillPaymentSubResponse billPaymentSubResponse = new BillPaymentSubResponse();
//        	billPaymentSubResponse.setTransactionId(mbmeBillPaymentResponse.getResponseData().getTransactionId());
//        	billPaymentSubResponse.setAmountPaid(mbmeBillPaymentResponse.getResponseData().getAmountPaid());
//        	billPaymentSubResponse.setProviderTransactionId(mbmeBillPaymentResponse.getResponseData().getProviderTransactionId());
//        	billPaymentSubResponse.setResField1(mbmeBillPaymentResponse.getResponseData().getResField1());
//        	
//        	billPaymentResponse.setResponseData(billPaymentSubResponse);
        	
        	 logger.info("Processing processMbmeBillPayment Response for MBME Bill payment service ::: {}", response.getBody().toString());
        	JsonNode rootNode = objectMapper.readTree(response.getBody());
     	    String responseCode = rootNode.path("responseCode").asText();
            String responseStatus = rootNode.path("status").asText();
            String responseMessage = rootNode.path("responseMessage").asText();
            //String amount = rootNode.path("responseData").path("transactionId").asText();
            
            billPaymentResponse = new BillPaymentResponse();
            
            billPaymentResponse.setDeviceTransactionId(billPaymentRequest.getDeviceTransactionId());
            billPaymentResponse.setExternalTransactionId(billPaymentRequest.getExternalTransactionId());
            billPaymentResponse.setResponseCode(responseCode);
        	billPaymentResponse.setResponseStatus(responseStatus);
        	billPaymentResponse.setResponseMessage(responseMessage);
        	billPaymentResponse.setStatus(responseMessage);
        	
        	List<BillingCommonDynamicResponseField> balanceEnquiryDynamicResponseField = null;
        	if(Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
      			   && !mbmeBillPaymentService.getServiceId().isEmpty() && mbmeBillPaymentService.getServiceId().equalsIgnoreCase("103")) {
        		
        	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BillingCommonDynamicResponseField();
          	   balEnquiryDynamicResponseField_resField1.setName("amountPaid");
          	   balEnquiryDynamicResponseField_resField1.setValue(rootNode.path("responseData").path("amountPaid").asText());
          	   balEnquiryDynamicResponseField_resField1.setLabel("");
          	   balEnquiryDynamicResponseField_resField1.setType("text");
          	   balEnquiryDynamicResponseField_resField1.setVisible(Boolean.FALSE);
          	   
          	      	  
          	   
          	   
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField2.setName("apiReturnTransactionId");
				balEnquiryDynamicResponseField_resField2.setValue(rootNode.path("responseData").path("providerTransactionId").asText());
				balEnquiryDynamicResponseField_resField2.setLabel("");
				balEnquiryDynamicResponseField_resField2.setType("text");
				balEnquiryDynamicResponseField_resField2.setVisible(Boolean.FALSE);
        		
				balanceEnquiryDynamicResponseField = new ArrayList<BillingCommonDynamicResponseField>();            	
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField1);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField2);
            	billPaymentResponse.setDynamicResponseFields(balanceEnquiryDynamicResponseField);
            	
        	}else   if(Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
     			   && !mbmeBillPaymentService.getServiceId().isEmpty() && mbmeBillPaymentService.getServiceId().equalsIgnoreCase("19")) {
            	
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField1.setName("externaltransactionId");
				balEnquiryDynamicResponseField_resField1
						.setValue(rootNode.path("responseData").path("transactionId").asText());
				balEnquiryDynamicResponseField_resField1.setLabel("");
				balEnquiryDynamicResponseField_resField1.setType("text");
				balEnquiryDynamicResponseField_resField1.setVisible(Boolean.FALSE);

				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField2.setName("amountPaid");
				balEnquiryDynamicResponseField_resField2
						.setValue(rootNode.path("responseData").path("amountPaid").asText());
				balEnquiryDynamicResponseField_resField2.setLabel("");
				balEnquiryDynamicResponseField_resField2.setType("text");
				balEnquiryDynamicResponseField_resField2.setVisible(Boolean.FALSE);
				
				
				// 3 . apiReturnTransactionId
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField3.setName("apiReturnTransactionId");
				balEnquiryDynamicResponseField_resField3
						.setValue(rootNode.path("responseData").path("providerTransactionId").asText());
				balEnquiryDynamicResponseField_resField3.setLabel("");
				balEnquiryDynamicResponseField_resField3.setType("text");
				balEnquiryDynamicResponseField_resField3.setVisible(Boolean.FALSE);
				
				//4. transactionBillerStatus
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField4 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField4.setName("transactionBillerStatus");
				balEnquiryDynamicResponseField_resField4
						.setValue(rootNode.path("responseData").path("resField1").asText());
				balEnquiryDynamicResponseField_resField4.setLabel("");
				balEnquiryDynamicResponseField_resField4.setType("text");
				balEnquiryDynamicResponseField_resField4.setVisible(Boolean.FALSE);
				
				
				//5. serviceType
         	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField5 = new BillingCommonDynamicResponseField();
         	   balEnquiryDynamicResponseField_resField5.setName("serviceType");
         	   balEnquiryDynamicResponseField_resField5.setValue(rootNode.path("responseData").path("resField2").asText()); //serviceType
         	   balEnquiryDynamicResponseField_resField5.setLabel("");
         	   balEnquiryDynamicResponseField_resField5.setType("text");
         	   balEnquiryDynamicResponseField_resField5.setVisible(Boolean.FALSE);
				
				
				
         	  //6. transactionType
        	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField6 = new BillingCommonDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField6.setName("transactionType");
        	   balEnquiryDynamicResponseField_resField6.setValue(rootNode.path("responseData").path("resField3").asText()); //transactionType
        	   balEnquiryDynamicResponseField_resField6.setLabel("");
        	   balEnquiryDynamicResponseField_resField6.setType("text");
        	   balEnquiryDynamicResponseField_resField6.setVisible(Boolean.FALSE);
				
				
        	   //7. transactionTime
        	   
        	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField7 = new BillingCommonDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField7.setName("transactionTime");
        	   balEnquiryDynamicResponseField_resField7.setValue(rootNode.path("responseData").path("resField4").asText()); //transactionTime
        	   balEnquiryDynamicResponseField_resField7.setLabel("");
        	   balEnquiryDynamicResponseField_resField7.setType("text");
        	   balEnquiryDynamicResponseField_resField7.setVisible(Boolean.FALSE);
        	   
        	   
        	   //8. replyTime
        	   
        	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField8 = new BillingCommonDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField8.setName("replyTime");
        	   balEnquiryDynamicResponseField_resField8.setValue(rootNode.path("responseData").path("resField5").asText()); //replyTime
        	   balEnquiryDynamicResponseField_resField8.setLabel("");
        	   balEnquiryDynamicResponseField_resField8.setType("text");
        	   balEnquiryDynamicResponseField_resField8.setVisible(Boolean.FALSE);
				
			
        	   //9. middlewareTransactionId
        	   
        	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField9 = new BillingCommonDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField9.setName("middlewareTransactionId");
        	   balEnquiryDynamicResponseField_resField9.setValue(rootNode.path("responseData").path("resField6").asText()); //middlewareTransactionId
        	   balEnquiryDynamicResponseField_resField9.setLabel("");
        	   balEnquiryDynamicResponseField_resField9.setType("text");
        	   balEnquiryDynamicResponseField_resField9.setVisible(Boolean.FALSE);
				

				balanceEnquiryDynamicResponseField = new ArrayList<BillingCommonDynamicResponseField>();
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField1);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField2);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField3);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField4);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField5);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField6);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField7);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField8);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField9);
				
				
				billPaymentResponse.setDynamicResponseFields(balanceEnquiryDynamicResponseField);
        		
//        		billPaymentSubResponse.setTransactionId(rootNode.path("responseData").path("transactionId").asText());
//            	billPaymentSubResponse.setAmountPaid(rootNode.path("responseData").path("amountPaid").asText());
//            	billPaymentSubResponse.setProviderTransactionId(rootNode.path("responseData").path("providerTransactionId").asText());
//            	
//            	billPaymentSubResponse.setResField1(rootNode.path("responseData").path("resField1").asText());
//            	billPaymentSubResponse.setResField2(rootNode.path("responseData").path("resField2").asText());
//            	billPaymentSubResponse.setResField3(rootNode.path("responseData").path("resField3").asText());
//            	billPaymentSubResponse.setResField4(rootNode.path("responseData").path("resField4").asText());
//            	billPaymentSubResponse.setResField5(rootNode.path("responseData").path("resField5").asText());
//            	billPaymentSubResponse.setResField6(rootNode.path("responseData").path("resField6").asText());
            	
            	
          
            }else {
             	
            }
        	return billPaymentResponse;
        	
//            BillPayment duBillPayment = new BillPayment();
//            duBillPayment.setTransactionId(billPaymentRequest.getTransactionId());
//            duBillPayment.setMerchantId(billPaymentRequest.getMerchantId());
//            duBillPayment.setMerchantLocation(billPaymentRequest.getMerchantLocation());
//            duBillPayment.setServiceId(billPaymentRequest.getServiceCode());
//            duBillPayment.setMethod(billPaymentRequest.getMethod());
//            duBillPayment.setLang(billPaymentRequest.getLanguage());
//            duBillPayment.setReqField1(request.getReqField1());
//            duBillPayment.setResponseCode(duBillPaymentResponse.getResponseCode());
//            duBillPayment.setStatus(duBillPaymentResponse.getStatus());
//            duBillPayment.setResponseMessage(duBillPaymentResponse.getResponseMessage());
//            duBillPayment.setAccountNumber(duBillPaymentResponse.getResponseData().getAccountNumber());
//            duBillPayment.setAmount(duBillPaymentResponse.getResponseData().getAmount());
//            duBillPayment.setCustName(duBillPaymentResponse.getResponseData().getCustName());
//            duBillPayment.setResField1(duBillPaymentResponse.getResponseData().getResField1());
//            duBillPayment.setResField2(duBillPaymentResponse.getResponseData().getResField2());
//            logger.info("DuBillPayment processed and saved successfully.");
// 				billPaymentRepository.save(duBillPayment);
        } else {
        	
            logger.error("Failed to process processMbmeBillPayment request.");
            throw new RuntimeException("Failed to process processMbmeBillPayment request");
        }
        
       
    }
    
    
    
}