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
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillingCommonDynamicResponseField;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.entity.BillInquiryEntity;
import com.sesami.smart_bill_payment_services.mbme.billpayment.repository.BillInquiryRepository;
import com.sesami.smart_bill_payment_services.mbme.token.service.TokenService;
@Service
public class MbmeBillInquiryService {

   private static final Logger logger = LoggerFactory.getLogger(MbmeBillInquiryService.class);
   
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
   
   
   
   public BalanceEnquiryResponse processBillInquiry(BalanceEnquiryRequest balanceEnquiryRequest) throws IOException {
	   logger.info("Processing MBME BillInquiry request for transactionId: {}", balanceEnquiryRequest.getDeviceTransactionId());
	   BalanceEnquiryResponse balanceEnquiryResponse = null;
	   
//	   MbmeBillInquiryRequest reqMbmeBillInquiryRequest = new MbmeBillInquiryRequest();
//	   reqMbmeBillInquiryRequest.setTransactionId(balanceEnquiryRequest.getTransactionId());
//	   reqMbmeBillInquiryRequest.setMerchantId(balanceEnquiryRequest.getMerchantId());
//	   reqMbmeBillInquiryRequest.setMerchantLocation(balanceEnquiryRequest.getMerchantLocation());
//	   reqMbmeBillInquiryRequest.setServiceId(balanceEnquiryRequest.getServiceCode());
//	   reqMbmeBillInquiryRequest.setMethod(balanceEnquiryRequest.getServiceType());
//	   reqMbmeBillInquiryRequest.setLang("en");
//	   if(Objects.nonNull(reqMbmeBillInquiryRequest) && Objects.nonNull(reqMbmeBillInquiryRequest.getServiceId())
//			   && !reqMbmeBillInquiryRequest.getServiceId().isEmpty() && reqMbmeBillInquiryRequest.getServiceId().equalsIgnoreCase("103")) {
//		   reqMbmeBillInquiryRequest.setReqField1(balanceEnquiryRequest.getDynamicRequestFields().get(0).getValue());
//		  // reqMbmeBillInquiryRequest.setReqField2(balanceEnquiryRequest.getDynamicRequestFields().get(1).getValue());
//	   }else if(Objects.nonNull(reqMbmeBillInquiryRequest) && Objects.nonNull(reqMbmeBillInquiryRequest.getServiceId())
//			   && !reqMbmeBillInquiryRequest.getServiceId().isEmpty() && reqMbmeBillInquiryRequest.getServiceId().equalsIgnoreCase("19")) {
//		   reqMbmeBillInquiryRequest.setReqField1(balanceEnquiryRequest.getDynamicRequestFields().get(0).getValue());
//		   reqMbmeBillInquiryRequest.setReqField2(balanceEnquiryRequest.getDynamicRequestFields().get(1).getValue()); 
//	   }
	  
	   //reqMbmeBillInquiryRequest.setReqField1("0551234567");
	   
	   
       HttpHeaders headers = new HttpHeaders();
       headers.setContentType(MediaType.APPLICATION_JSON);
       headers.set("Authorization", "Bearer " + tokenService.getValidToken().getAccessToken());
       ObjectMapper objectMapper = new ObjectMapper();
       String jsonRequest = generateDynamicBillInquiryRequest(balanceEnquiryRequest);		//objectMapper.writeValueAsString(reqMbmeBillInquiryRequest);
       HttpEntity<String> httpRequest = new HttpEntity<>(jsonRequest, headers);

       LocalDateTime requestTimestamp = LocalDateTime.now();

       ResponseEntity<String> response = restTemplate.exchange(billPaymentUrl, HttpMethod.POST, httpRequest, String.class);

       BillInquiryEntity billInquiry = new BillInquiryEntity();
       billInquiry.setTransactionId(balanceEnquiryRequest.getDeviceTransactionId());
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
           
           
           List<BillingCommonDynamicResponseField> balanceEnquiryDynamicResponseField = null;
           balanceEnquiryResponse = new BalanceEnquiryResponse();
           balanceEnquiryResponse.setDeviceTransactionId(balanceEnquiryRequest.getDeviceTransactionId());
           balanceEnquiryResponse.setExternalTransactionId(balanceEnquiryRequest.getExternalTransactionId());
           balanceEnquiryResponse.setCurrencyCode("AED");
           balanceEnquiryResponse.setResponseCode(responseCode);
           balanceEnquiryResponse.setResponseStatus(responseStatus);
           balanceEnquiryResponse.setResponseMessage(responseMessage);
           balanceEnquiryResponse.setInternalWebServiceCode("000");
           balanceEnquiryResponse.setInternalWebServiceDesc("SUCCESS");
           balanceEnquiryResponse.setDueAmount(amount);
           
           // DU -  Service Id = 103 for DU Payment
           if(Objects.nonNull(balanceEnquiryRequest) && Objects.nonNull(balanceEnquiryRequest.getServiceId())
    			   && !balanceEnquiryRequest.getServiceId().isEmpty() && balanceEnquiryRequest.getServiceId().equalsIgnoreCase("103")) {
        	   
        	 //1. //9. middlewareTransactionId  	   
        	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BillingCommonDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField1.setName("amount");
        	   balEnquiryDynamicResponseField_resField1.setValue(rootNode.path("responseData").path("amount").asText());
        	   balEnquiryDynamicResponseField_resField1.setLabel("DUE_AMOUNT");
        	   balEnquiryDynamicResponseField_resField1.setType("currency");
        	   balEnquiryDynamicResponseField_resField1.setVisible(Boolean.TRUE);
        	   
        	 //2. custName
        	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BillingCommonDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField2.setName("customerName");
        	   balEnquiryDynamicResponseField_resField2.setValue(rootNode.path("responseData").path("custName").asText()); //custName
        	   balEnquiryDynamicResponseField_resField2.setLabel("CUSTOMER_NAME");
        	   balEnquiryDynamicResponseField_resField2.setType("text");
        	   balEnquiryDynamicResponseField_resField2.setVisible(Boolean.TRUE);
        	   
        	 //3. accountNumber
        	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BillingCommonDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField3.setName("accountNumber");
        	   balEnquiryDynamicResponseField_resField3.setValue(rootNode.path("responseData").path("accountNumber").asText());
        	   balEnquiryDynamicResponseField_resField3.setLabel("ACCOUNT_NUMBER");
        	   balEnquiryDynamicResponseField_resField3.setType("text");
        	   balEnquiryDynamicResponseField_resField3.setVisible(Boolean.TRUE);
        	   
        	 //4. accountNumber
        	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField4 = new BillingCommonDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField4.setName("apiReturnTransactionId");
        	   balEnquiryDynamicResponseField_resField4.setValue(rootNode.path("responseData").path("resField1").asText());
        	   balEnquiryDynamicResponseField_resField4.setLabel("");
        	   balEnquiryDynamicResponseField_resField4.setType("text");
        	   balEnquiryDynamicResponseField_resField4.setVisible(Boolean.FALSE);
        	   
        	   
        	   //5. middlewareTransactionId
        	   
        	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField5 = new BillingCommonDynamicResponseField();
        	   balEnquiryDynamicResponseField_resField5.setName("middlewareTransactionId");
        	   balEnquiryDynamicResponseField_resField5.setValue(rootNode.path("responseData").path("resField2").asText()); //middlewareTransactionId
        	   balEnquiryDynamicResponseField_resField5.setLabel("");
        	   balEnquiryDynamicResponseField_resField5.setType("text");
        	   balEnquiryDynamicResponseField_resField5.setVisible(Boolean.FALSE);
        	   
        	   balanceEnquiryDynamicResponseField = new ArrayList<BillingCommonDynamicResponseField>(); 
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField1);
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField2);
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField3);
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField4);
        	   
        	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField5);
        	   
        	   
        	   
        	   balanceEnquiryResponse.setDynamicResponseFields(balanceEnquiryDynamicResponseField);
        	   
        	   
        	   balanceEnquiryResponse.setMinAmount("20.00");// "10000.00"
        	   balanceEnquiryResponse.setMaxAmount("10000.00");
        	   balanceEnquiryResponse.setPartialPayment(Boolean.TRUE);
        	   balanceEnquiryResponse.setChangeHandling("credit");
        	   balanceEnquiryResponse.setCustomerCommission(Boolean.TRUE);
        	   balanceEnquiryResponse.setCommissionPercentage("10");
        	   
           }else  if(Objects.nonNull(balanceEnquiryRequest) && Objects.nonNull(balanceEnquiryRequest.getServiceId())
        			   && !balanceEnquiryRequest.getServiceId().isEmpty() && balanceEnquiryRequest.getServiceId().equalsIgnoreCase("19")) {
        	// Service Id = 19 for Etislat	
        	   
            	   balanceEnquiryResponse.setMinAmount("10.00");
            	   balanceEnquiryResponse.setMaxAmount("10000.00");
            	   balanceEnquiryResponse.setPartialPayment(Boolean.TRUE);
            	   balanceEnquiryResponse.setChangeHandling("credit");
            	   balanceEnquiryResponse.setCustomerCommission(Boolean.TRUE);
            	   balanceEnquiryResponse.setCommissionPercentage("0.00");
            	   balanceEnquiryResponse.setCommissionValue("10.50");
            	   balanceEnquiryResponse.setServiceChargesVAT("5.00");
            	   
            	          	   
            	   balanceEnquiryDynamicResponseField = new ArrayList<BillingCommonDynamicResponseField>(); 
            	   // 1. accountNumber
            	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BillingCommonDynamicResponseField();
            	   balEnquiryDynamicResponseField_resField1.setName("accountNumber");
            	   balEnquiryDynamicResponseField_resField1.setValue(rootNode.path("responseData").path("accountNumber").asText());
            	   balEnquiryDynamicResponseField_resField1.setLabel("ACCOUNT_NUMBER");
            	   balEnquiryDynamicResponseField_resField1.setType("text");
            	   balEnquiryDynamicResponseField_resField1.setVisible(Boolean.TRUE);
            	   
            	   //2 . apiReturnTransactionId
            	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BillingCommonDynamicResponseField();
            	   balEnquiryDynamicResponseField_resField2.setName("apiReturnTransactionId");
            	   balEnquiryDynamicResponseField_resField2.setValue(rootNode.path("responseData").path("providerTransactionId").asText());
            	   balEnquiryDynamicResponseField_resField2.setLabel("");
            	   balEnquiryDynamicResponseField_resField2.setType("text");
            	   balEnquiryDynamicResponseField_resField2.setVisible(Boolean.FALSE);
            	   
            	   // 3. amount
            	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BillingCommonDynamicResponseField();
            	   balEnquiryDynamicResponseField_resField3.setName("amount");
            	   balEnquiryDynamicResponseField_resField3.setValue(rootNode.path("responseData").path("amount").asText());
            	   balEnquiryDynamicResponseField_resField3.setLabel("DUE_AMOUNT");
            	   balEnquiryDynamicResponseField_resField3.setType("currency");
            	   balEnquiryDynamicResponseField_resField3.setVisible(Boolean.TRUE);
            	   
            	   //4.  minAmount
            	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField4 = new BillingCommonDynamicResponseField();
            	   balEnquiryDynamicResponseField_resField4.setName("minAmount");
            	  //  balEnquiryDynamicResponseField_resField4.setValue(rootNode.path("responseData").path("resField5").asText()); 
            	   balEnquiryDynamicResponseField_resField4.setValue(balanceEnquiryResponse.getMinAmount()); 
            	   balEnquiryDynamicResponseField_resField4.setLabel("MIN_DEPOSIT_AMOUNT");
            	   balEnquiryDynamicResponseField_resField4.setType("text");
            	   balEnquiryDynamicResponseField_resField4.setVisible(Boolean.TRUE);
            	   
            	   //5. maxAmount
            	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField5 = new BillingCommonDynamicResponseField();
            	   balEnquiryDynamicResponseField_resField5.setName("maxAmount");
            	   // balEnquiryDynamicResponseField_resField5.setValue(rootNode.path("responseData").path("resField6").asText()); //maxAmount
            	   balEnquiryDynamicResponseField_resField5.setValue(balanceEnquiryResponse.getMaxAmount()); //maxAmount
            	   balEnquiryDynamicResponseField_resField5.setLabel("MAX_DEPOSIT_AMOUNT");
            	   balEnquiryDynamicResponseField_resField5.setType("text");
            	   balEnquiryDynamicResponseField_resField5.setVisible(Boolean.TRUE);
            	   
            	  
            	   
//            	   BalanceEnquiryDynamicResponseField balEnquiryDynamicResponseField_resField6 = new BalanceEnquiryDynamicResponseField();
//            	   balEnquiryDynamicResponseField_resField6.setName("maxAmount");
//            	   balEnquiryDynamicResponseField_resField6.setValue(rootNode.path("responseData").path("resField5").asText()); //accountNumber
//            	   balEnquiryDynamicResponseField_resField6.setLabel("MAX_AMOUNT");
//            	   balEnquiryDynamicResponseField_resField6.setType("text");
//            	   balEnquiryDynamicResponseField_resField6.setVisible(Boolean.FALSE);
            	   
            	   
            	   //6. serviceType
            	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField6 = new BillingCommonDynamicResponseField();
            	   balEnquiryDynamicResponseField_resField6.setName("serviceType");
            	   balEnquiryDynamicResponseField_resField6.setValue(rootNode.path("responseData").path("resField1").asText()); //serviceType
            	   balEnquiryDynamicResponseField_resField6.setLabel("");
            	   balEnquiryDynamicResponseField_resField6.setType("text");
            	   balEnquiryDynamicResponseField_resField6.setVisible(Boolean.FALSE);
            	   
            	   
            	   //7. transactionType
            	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField7 = new BillingCommonDynamicResponseField();
            	   balEnquiryDynamicResponseField_resField7.setName("transactionType");
            	   balEnquiryDynamicResponseField_resField7.setValue(rootNode.path("responseData").path("resField2").asText()); //transactionType
            	   balEnquiryDynamicResponseField_resField7.setLabel("");
            	   balEnquiryDynamicResponseField_resField7.setType("text");
            	   balEnquiryDynamicResponseField_resField7.setVisible(Boolean.FALSE);
            	   
            	   //8. transactionTime
            	   
            	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField8 = new BillingCommonDynamicResponseField();
            	   balEnquiryDynamicResponseField_resField8.setName("transactionTime");
            	   balEnquiryDynamicResponseField_resField8.setValue(rootNode.path("responseData").path("resField3").asText()); //transactionType
            	   balEnquiryDynamicResponseField_resField8.setLabel("");
            	   balEnquiryDynamicResponseField_resField8.setType("text");
            	   balEnquiryDynamicResponseField_resField8.setVisible(Boolean.FALSE);
            	   
            	   
            	   //9. replyTime
            	   
            	   BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField9 = new BillingCommonDynamicResponseField();
            	   balEnquiryDynamicResponseField_resField9.setName("replyTime");
            	   balEnquiryDynamicResponseField_resField9.setValue(rootNode.path("responseData").path("resField4").asText()); //transactionType
            	   balEnquiryDynamicResponseField_resField9.setLabel("");
            	   balEnquiryDynamicResponseField_resField9.setType("text");
            	   balEnquiryDynamicResponseField_resField9.setVisible(Boolean.FALSE);
            	              	   
            	   
            	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField1);
            	   
            	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField2);
            	   
            	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField3);
            	   
            	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField4);
            	   
            	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField5);
            	   
            	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField6);
            	   
            	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField7);
            	   
            	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField8);
            	   
            	   balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField9);
            	   
            	   balanceEnquiryResponse.setDynamicResponseFields(balanceEnquiryDynamicResponseField);
               }
           
           
        // AccountNumber field required
           return balanceEnquiryResponse ;
       } else {
    	   balanceEnquiryResponse = new BalanceEnquiryResponse();
    	   balanceEnquiryResponse.setDeviceTransactionId(balanceEnquiryRequest.getDeviceTransactionId());
           balanceEnquiryResponse.setExternalTransactionId(balanceEnquiryRequest.getExternalTransactionId());
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
   
	 
   public static String generateDynamicBillInquiryRequest(BalanceEnquiryRequest balanceEnquiryRequest) {
       ObjectMapper objectMapper = new ObjectMapper();
       ObjectNode rootNode = objectMapper.createObjectNode();

       rootNode.put("transactionId", balanceEnquiryRequest.getExternalTransactionId());
       rootNode.put("merchantId", balanceEnquiryRequest.getMerchantId());
       rootNode.put("serviceId", balanceEnquiryRequest.getServiceId());
       rootNode.put("method", balanceEnquiryRequest.getServiceType());
       rootNode.put("lang", balanceEnquiryRequest.getLanguage());

       if ("103".equals(balanceEnquiryRequest.getServiceId())) {
           rootNode.put("merchantLocation", "DU POSTPAID HQ");
           rootNode.put("reqField1", balanceEnquiryRequest.getDynamicRequestFields().get(0).getValue());
       }else if ("19".equals(balanceEnquiryRequest.getServiceId())) {
           rootNode.put("merchantLocation", "Your Location");
           rootNode.put("reqField1", balanceEnquiryRequest.getDynamicRequestFields().get(0).getValue());
           rootNode.put("reqField2", balanceEnquiryRequest.getDynamicRequestFields().get(1).getValue());
       }  else {
           rootNode.put("merchantLocation", balanceEnquiryRequest.getMerchantLocation());
           rootNode.put("reqField1", balanceEnquiryRequest.getDynamicRequestFields().get(0).getValue());
           rootNode.put("reqField2", balanceEnquiryRequest.getDynamicRequestFields().get(1).getValue());
       }

       return rootNode.toString();
   }

}
