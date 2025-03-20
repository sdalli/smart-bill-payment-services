package com.sesami.smart_bill_payment_services.mbme.billpayment.service;

import java.io.IOException;
import java.time.LocalDateTime;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryDynamicResponseField;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.MbmeBillInquiryRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.entity.BillInquiryEntity;
import com.sesami.smart_bill_payment_services.mbme.billpayment.repository.BillInquiryRepository;
import com.sesami.smart_bill_payment_services.mbme.token.service.TokenService;
@Service
public class MbmeBillInquiryService_DynamicReqForming {

   private static final Logger logger = LoggerFactory.getLogger(MbmeBillInquiryService_DynamicReqForming.class);
   
   @Autowired
   private BillInquiryRepository billInquiryRepository;

   @Autowired
   private TokenService tokenService;
   
   
   @Autowired
   private RestTemplate restTemplate;
   
   

   @Value("${mbme.api.balance-payment.url}")
   private String billPaymentUrl;

	/*
	 * { "transactionId": "20010****0012340066", "merchantId": "66",
	 * "merchantLocation": "DU POSTPAID HQ", "serviceId": "103", "method":
	 * "balance", "lang": "en", "reqField1": "0551234567" }
	 */
   
   
   
   public BalanceEnquiryResponse processMbmeBillInquiryService(BalanceEnquiryRequest balanceEnquiryRequest) throws IOException {
	   logger.info("Processing MBME BillInquiry request for transactionId: {}", balanceEnquiryRequest.getTransactionId());
	   BalanceEnquiryResponse balanceEnquiryResponse = null;
	   
	   MbmeBillInquiryRequest reqMbmeBillInquiryRequest = new MbmeBillInquiryRequest();
	   reqMbmeBillInquiryRequest.setTransactionId(balanceEnquiryRequest.getTransactionId());
	   reqMbmeBillInquiryRequest.setMerchantId(balanceEnquiryRequest.getMerchantId());
	   reqMbmeBillInquiryRequest.setMerchantLocation(balanceEnquiryRequest.getMerchantLocation());
	   reqMbmeBillInquiryRequest.setServiceId(balanceEnquiryRequest.getServiceCode());
	   reqMbmeBillInquiryRequest.setMethod(balanceEnquiryRequest.getServiceType());
	   reqMbmeBillInquiryRequest.setLang("en");
	   if(Objects.nonNull(reqMbmeBillInquiryRequest) && Objects.nonNull(reqMbmeBillInquiryRequest.getServiceId())
			   && !reqMbmeBillInquiryRequest.getServiceId().isEmpty() && reqMbmeBillInquiryRequest.getServiceId().equalsIgnoreCase("103")) {
		   reqMbmeBillInquiryRequest.setReqField1(balanceEnquiryRequest.getDynamicRequestFields().get(0).getValue());
		  // reqMbmeBillInquiryRequest.setReqField2(balanceEnquiryRequest.getDynamicRequestFields().get(1).getValue());
	   }else if(Objects.nonNull(reqMbmeBillInquiryRequest) && Objects.nonNull(reqMbmeBillInquiryRequest.getServiceId())
			   && !reqMbmeBillInquiryRequest.getServiceId().isEmpty() && reqMbmeBillInquiryRequest.getServiceId().equalsIgnoreCase("19")) {
		   reqMbmeBillInquiryRequest.setReqField1(balanceEnquiryRequest.getDynamicRequestFields().get(0).getValue());
		   reqMbmeBillInquiryRequest.setReqField2(balanceEnquiryRequest.getDynamicRequestFields().get(1).getValue()); 
	   }
	  
	   //reqMbmeBillInquiryRequest.setReqField1("0551234567");
	   
	   
       HttpHeaders headers = new HttpHeaders();
       headers.setContentType(MediaType.APPLICATION_JSON);
       headers.set("Authorization", "Bearer " + tokenService.getValidToken().getAccessToken());
       ObjectMapper objectMapper = new ObjectMapper();
       String jsonRequest = generateDynamicRequest(balanceEnquiryRequest);		//objectMapper.writeValueAsString(reqMbmeBillInquiryRequest);
       HttpEntity<String> httpRequest = new HttpEntity<>(jsonRequest, headers);

       LocalDateTime requestTimestamp = LocalDateTime.now();

       ResponseEntity<String> response = restTemplate.exchange(billPaymentUrl, HttpMethod.POST, httpRequest, String.class);

       BillInquiryEntity billInquiry = new BillInquiryEntity();
       billInquiry.setTransactionId(balanceEnquiryRequest.getTransactionId());
       billInquiry.setMerchantId(balanceEnquiryRequest.getMerchantId());
       billInquiry.setMerchantLocation(balanceEnquiryRequest.getMerchantLocation());
      // billInquiry.setServiceId(request.getServiceId());
       /*billInquiry.setMethod(request.getMethod());
       billInquiry.setLang(request.getLang());
       billInquiry.setReqField1(request.getReqField1());*/
       billInquiry.setRequestJson(jsonRequest);
       billInquiry.setResponseJson(response.getBody());
       billInquiry.setTimestamp(requestTimestamp);
       billInquiryRepository.save(billInquiry);

       if (response.getStatusCode() == HttpStatus.OK) {
    	   // MbmeBillInquiryResponse mbmeBillInquiryResponse= objectMapper.readValue(response.getBody(), MbmeBillInquiryResponse.class);
    	   JsonNode rootNode = objectMapper.readTree(response.getBody());
    	   String responseCode = rootNode.path("responseCode").asText();
           String responseStatus = rootNode.path("status").asText();
           String responseMessage = rootNode.path("responseMessage").asText();
           String amount = rootNode.path("responseData").path("amount").asText();
           
           
           
           balanceEnquiryResponse = new BalanceEnquiryResponse();
           balanceEnquiryResponse.setTransactionId(balanceEnquiryRequest.getTransactionId());
           balanceEnquiryResponse.setCurrencyCode("AED");
           balanceEnquiryResponse.setResponseCode(responseCode);
           balanceEnquiryResponse.setResponseStatus(responseStatus);
           balanceEnquiryResponse.setResponseMessage(responseMessage);
           balanceEnquiryResponse.setInternalWebServiceCode("000");
           balanceEnquiryResponse.setInternalWebServiceDesc("SUCCESS");
           balanceEnquiryResponse.setDueAmount(amount);
           
           
           if(Objects.nonNull(reqMbmeBillInquiryRequest) && Objects.nonNull(reqMbmeBillInquiryRequest.getServiceId())
    			   && !reqMbmeBillInquiryRequest.getServiceId().isEmpty() && reqMbmeBillInquiryRequest.getServiceId().equalsIgnoreCase("19")) {
        	   String providerTransactionId = rootNode.path("responseData").path("providerTransactionId").asText();
        	   String resField1 = rootNode.path("responseData").path("resField1").asText();
        	   String resField2 = rootNode.path("responseData").path("resField2").asText();
        	   String resField3 = rootNode.path("responseData").path("resField3").asText();
        	   String resField4 = rootNode.path("responseData").path("resField4").asText();
        	   String resField5 = rootNode.path("responseData").path("resField5").asText();
        	   String resField6 = rootNode.path("responseData").path("resField6").asText();
        	   
        	   List<BalanceEnquiryDynamicResponseField> balanceEnquiryDynamicResponseField = new ArrayList<BalanceEnquiryDynamicResponseField>(); 
        	   
        	   BalanceEnquiryDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BalanceEnquiryDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField1.setName("resField1");
        	   balEnquiryDynamicResponseField_resField1.setValue(resField1);
        	   
        	   BalanceEnquiryDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BalanceEnquiryDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField2.setName("resField2");
        	   balEnquiryDynamicResponseField_resField2.setValue(resField2);
        	   
        	   BalanceEnquiryDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BalanceEnquiryDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField3.setName("resField3");
        	   balEnquiryDynamicResponseField_resField3.setValue(resField3);
        	   
        	   BalanceEnquiryDynamicResponseField balEnquiryDynamicResponseField_resField4 = new BalanceEnquiryDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField4.setName("resField4");
        	   balEnquiryDynamicResponseField_resField4.setValue(resField4);
        	   
        	   BalanceEnquiryDynamicResponseField balEnquiryDynamicResponseField_resField5 = new BalanceEnquiryDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField5.setName("resField5");
        	   balEnquiryDynamicResponseField_resField5.setValue(resField5);
        	   
        	   BalanceEnquiryDynamicResponseField balEnquiryDynamicResponseField_resField6 = new BalanceEnquiryDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField6.setName("resField6");
        	   balEnquiryDynamicResponseField_resField6.setValue(resField6);
        	   
        	   BalanceEnquiryDynamicResponseField balEnquiryDynamicResponseField_providerTransactionId = new BalanceEnquiryDynamicResponseField();
        	   balEnquiryDynamicResponseField_providerTransactionId.setName("providerTransactionId");
        	   balEnquiryDynamicResponseField_providerTransactionId.setValue(providerTransactionId);
        	   
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField1);
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField2);
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField3);
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField4);
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField5);
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField6);
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_providerTransactionId);
        	   
        	   balanceEnquiryResponse.setDynamicResponseFields(balanceEnquiryDynamicResponseField);
           }
           
           
        // AccountNumber field required
           return balanceEnquiryResponse ;
       } else {
    	   balanceEnquiryResponse = new BalanceEnquiryResponse();
           balanceEnquiryResponse.setTransactionId(balanceEnquiryRequest.getTransactionId());
           balanceEnquiryResponse.setCurrencyCode("AED");
           balanceEnquiryResponse.setResponseCode(response.getStatusCode().toString());
           balanceEnquiryResponse.setResponseStatus("FAILED");
           balanceEnquiryResponse.setResponseMessage("FAILED");
           balanceEnquiryResponse.setInternalWebServiceCode("444");
           balanceEnquiryResponse.setInternalWebServiceDesc("FAILED");
           balanceEnquiryResponse.setDueAmount("0.00");
           
           logger.error("Failed to process MBME BillInquiry request.");
           throw new RuntimeException("Failed to process MBME BillInquiry request");
       }
       
   }
   
	 
   public static String generateDynamicRequest(BalanceEnquiryRequest balanceEnquiryRequest) {
       ObjectMapper objectMapper = new ObjectMapper();
       ObjectNode rootNode = objectMapper.createObjectNode();

       rootNode.put("transactionId", balanceEnquiryRequest.getTransactionId());
       rootNode.put("merchantId", balanceEnquiryRequest.getMerchantId());
       rootNode.put("serviceId", balanceEnquiryRequest.getServiceCode());
       rootNode.put("method", balanceEnquiryRequest.getServiceType());
       rootNode.put("lang", balanceEnquiryRequest.getLanguage());

       if ("19".equals(balanceEnquiryRequest.getServiceCode())) {
           rootNode.put("merchantLocation", "Your Location");
           rootNode.put("reqField1", balanceEnquiryRequest.getDynamicRequestFields().get(0).getValue());
           rootNode.put("reqField2", balanceEnquiryRequest.getDynamicRequestFields().get(1).getValue());
       } else if ("103".equals(balanceEnquiryRequest.getServiceCode())) {
           rootNode.put("merchantLocation", "DU POSTPAID HQ");
           rootNode.put("reqField1", balanceEnquiryRequest.getDynamicRequestFields().get(0).getValue());
       } else {
           rootNode.put("merchantLocation", balanceEnquiryRequest.getMerchantLocation());
           rootNode.put("reqField1", balanceEnquiryRequest.getDynamicRequestFields().get(0).getValue());
           rootNode.put("reqField2", balanceEnquiryRequest.getDynamicRequestFields().get(1).getValue());
       }

       return rootNode.toString();
   }

}
