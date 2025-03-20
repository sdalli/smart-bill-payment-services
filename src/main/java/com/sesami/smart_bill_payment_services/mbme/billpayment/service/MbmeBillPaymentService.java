package com.sesami.smart_bill_payment_services.mbme.billpayment.service;

import java.io.IOException;
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
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillPaymentSubResponse;
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
    
    public BillPaymentResponse processMbmeBillPayment(BillPaymentRequest billPaymentRequest) throws IOException {
    	logger.info("Processing processMbmeBillPayment request for transactionId: {}", billPaymentRequest.getTransactionId());
    	BillPaymentResponse billPaymentResponse = null;
    	MbmeBillPaymentRequest mbmeBillPaymentService = new MbmeBillPaymentRequest();
    	mbmeBillPaymentService.setTransactionId(billPaymentRequest.getTransactionId());
    	mbmeBillPaymentService.setMerchantId(billPaymentRequest.getMerchantId());
    	mbmeBillPaymentService.setMerchantLocation(billPaymentRequest.getMerchantLocation());
    	mbmeBillPaymentService.setMethod(billPaymentRequest.getMethod());
    	mbmeBillPaymentService.setServiceId(billPaymentRequest.getServiceCode());
    	mbmeBillPaymentService.setPaymentMode(billPaymentRequest.getPaymentMode());
    	mbmeBillPaymentService.setPaidAmount(billPaymentRequest.getAmount());
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
  	   }
    	
    	// String url = "https://{{URL}}/du/bill/payment";
    	 HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         headers.set("Authorization", "Bearer " + tokenService.getValidToken().getAccessToken());
         ObjectMapper objectMapper = new ObjectMapper();
         String jsonRequest = objectMapper.writeValueAsString(mbmeBillPaymentService);
         HttpEntity<String> httpRequest = new HttpEntity<>(jsonRequest, headers);

        // LocalDateTime requestTimestamp = LocalDateTime.now();

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
        	
        	
        	JsonNode rootNode = objectMapper.readTree(response.getBody());
     	    String responseCode = rootNode.path("responseCode").asText();
            String responseStatus = rootNode.path("status").asText();
            String responseMessage = rootNode.path("responseMessage").asText();
            //String amount = rootNode.path("responseData").path("transactionId").asText();
            
            billPaymentResponse = new BillPaymentResponse();
        	billPaymentResponse.setResponseCode(responseCode);
        	billPaymentResponse.setResponseMessage(responseStatus);
        	billPaymentResponse.setStatus(responseMessage);
        	
        	BillPaymentSubResponse billPaymentSubResponse = null;
       
        	if(Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
      			   && !mbmeBillPaymentService.getServiceId().isEmpty() && mbmeBillPaymentService.getServiceId().equalsIgnoreCase("103")) {
        		billPaymentSubResponse = new BillPaymentSubResponse();
        		
        		billPaymentSubResponse.setTransactionId(rootNode.path("responseData").path("transactionId").asText());
            	billPaymentSubResponse.setAmountPaid(rootNode.path("responseData").path("amountPaid").asText());
            	billPaymentSubResponse.setProviderTransactionId(rootNode.path("responseData").path("providerTransactionId").asText());
            	
            	billPaymentSubResponse.setResField1(rootNode.path("responseData").path("resField1").asText());
            	
            	
            	billPaymentResponse.setResponseData(billPaymentSubResponse);
            	
        	}else   if(Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
     			   && !mbmeBillPaymentService.getServiceId().isEmpty() && mbmeBillPaymentService.getServiceId().equalsIgnoreCase("19")) {
            	
        		billPaymentSubResponse = new BillPaymentSubResponse();
        		
        		billPaymentSubResponse.setTransactionId(rootNode.path("responseData").path("transactionId").asText());
            	billPaymentSubResponse.setAmountPaid(rootNode.path("responseData").path("amountPaid").asText());
            	billPaymentSubResponse.setProviderTransactionId(rootNode.path("responseData").path("providerTransactionId").asText());
            	
            	billPaymentSubResponse.setResField1(rootNode.path("responseData").path("resField1").asText());
            	billPaymentSubResponse.setResField2(rootNode.path("responseData").path("resField2").asText());
            	billPaymentSubResponse.setResField3(rootNode.path("responseData").path("resField3").asText());
            	billPaymentSubResponse.setResField4(rootNode.path("responseData").path("resField4").asText());
            	billPaymentSubResponse.setResField5(rootNode.path("responseData").path("resField5").asText());
            	billPaymentSubResponse.setResField6(rootNode.path("responseData").path("resField6").asText());
            	
            	billPaymentResponse.setResponseData(billPaymentSubResponse);
            	
          
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