package com.sesami.smart_bill_payment_services.mbme.billpayment.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
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
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.DynamicRequestField;
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
		logger.info("Processing processMbmeBillPayment request for transactionId: {}",
				billPaymentRequest.getDeviceTransactionId());
		BillPaymentResponse billPaymentResponse = null;
		MbmeBillPaymentRequest mbmeBillPaymentService = new MbmeBillPaymentRequest();
		mbmeBillPaymentService.setTransactionId(billPaymentRequest.getExternalTransactionId());
		mbmeBillPaymentService.setMerchantId(billPaymentRequest.getMerchantId());
		mbmeBillPaymentService.setMerchantLocation(billPaymentRequest.getMerchantLocation());
		mbmeBillPaymentService.setMethod(billPaymentRequest.getServiceType());
		mbmeBillPaymentService.setServiceId(billPaymentRequest.getServiceId());
		mbmeBillPaymentService.setPaymentMode(billPaymentRequest.getPaymentMode());
		mbmeBillPaymentService.setPaidAmount(billPaymentRequest.getPaidAmount());
		mbmeBillPaymentService.setLang("en");

		mbmeBillPaymentService.setReqField1(billPaymentRequest.getDynamicRequestFields().get(0).getValue());
		mbmeBillPaymentService.setReqField2(billPaymentRequest.getDynamicRequestFields().get(0).getName());

		// TODO :
//    	 if(Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
//  			   && !mbmeBillPaymentService.getServiceId().isEmpty() && mbmeBillPaymentService.getServiceId().equalsIgnoreCase("103")) {
//    		 mbmeBillPaymentService.setReqField1(billPaymentRequest.getDynamicRequestFields().get(0).getValue());
//  		  // reqMbmeBillInquiryRequest.setReqField2(balanceEnquiryRequest.getDynamicRequestFields().get(1).getValue());
//  	   }else if(Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
//  			   && !mbmeBillPaymentService.getServiceId().isEmpty() && mbmeBillPaymentService.getServiceId().equalsIgnoreCase("19")) {
//  		 mbmeBillPaymentService.setReqField1(billPaymentRequest.getDynamicRequestFields().get(0).getValue());
//  		mbmeBillPaymentService.setReqField2(billPaymentRequest.getDynamicRequestFields().get(1).getValue()); 
//  		mbmeBillPaymentService.setReqField3(billPaymentRequest.getDynamicRequestFields().get(2).getValue()); 
//  		mbmeBillPaymentService.setReqField4(billPaymentRequest.getDynamicRequestFields().get(3).getValue()); 
//  	   }

		String requestJson = createRequestJson(billPaymentRequest);

		HttpHeaders headers = createHeaders();

		ObjectMapper objectMapper = new ObjectMapper();

		HttpEntity<String> httpRequest = new HttpEntity<>(requestJson, headers);

		// LocalDateTime requestTimestamp = LocalDateTime.now();
		logger.info("Processing processMbmeBillPayment request for MBME Bill payment service ::: {}", requestJson);
		ResponseEntity<String> response = restTemplate.exchange(billPaymentUrl, HttpMethod.POST, httpRequest,
				String.class);

		if (response.getStatusCode() == HttpStatus.OK) {

			logger.info("Processing processMbmeBillPayment Response for MBME Bill payment service ::: {}",
					response.getBody().toString());
			JsonNode rootNode = objectMapper.readTree(response.getBody());
			String responseCode = rootNode.path("responseCode").asText();
			String responseStatus = rootNode.path("status").asText();
			String responseMessage = rootNode.path("responseMessage").asText();
			// String amount = rootNode.path("responseData").path("transactionId").asText();

			billPaymentResponse = new BillPaymentResponse();

			billPaymentResponse.setDeviceTransactionId(billPaymentRequest.getDeviceTransactionId());
			billPaymentResponse.setExternalTransactionId(billPaymentRequest.getExternalTransactionId());
			billPaymentResponse.setResponseCode(responseCode);
			billPaymentResponse.setResponseStatus(responseStatus);
			billPaymentResponse.setResponseMessage(responseMessage);
			billPaymentResponse.setStatus(responseMessage);

//			String serviceId = mbmeBillPaymentService.getServiceId();
//            if (serviceId != null) {
//                billPaymentResponse.setDynamicResponseFields(prepareDynamicResponseFields(rootNode, serviceId));
//            }
			
			List<BillingCommonDynamicResponseField> balanceEnquiryDynamicResponseField = null;
			if (Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
					&& !mbmeBillPaymentService.getServiceId().isEmpty()
					&& mbmeBillPaymentService.getServiceId().equalsIgnoreCase("103")) {
				
//				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BillingCommonDynamicResponseField();
//				balEnquiryDynamicResponseField_resField1.setName("amountPaid");
//				balEnquiryDynamicResponseField_resField1
//						.setValue(rootNode.path("responseData").path("amountPaid").asText());
//				balEnquiryDynamicResponseField_resField1.setLabel("");
//				balEnquiryDynamicResponseField_resField1.setType("text");
//				balEnquiryDynamicResponseField_resField1.setVisible(Boolean.FALSE);
				
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField1.setName("externaltransactionId");
				balEnquiryDynamicResponseField_resField1
						.setValue(billPaymentRequest.getExternalTransactionId());
				balEnquiryDynamicResponseField_resField1.setLabel("");
				balEnquiryDynamicResponseField_resField1.setType("text");
				balEnquiryDynamicResponseField_resField1.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField1.setExport(Boolean.TRUE);

				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField2.setName("amountPaid");
				balEnquiryDynamicResponseField_resField2
						.setValue(rootNode.path("responseData").path("amountPaid").asText());
				balEnquiryDynamicResponseField_resField2.setLabel("");
				balEnquiryDynamicResponseField_resField2.setType("text");
				balEnquiryDynamicResponseField_resField2.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField2.setExport(Boolean.FALSE);

				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField3.setName("apiReturnTransactionId");
				balEnquiryDynamicResponseField_resField3
						.setValue(rootNode.path("responseData").path("providerTransactionId").asText());
				balEnquiryDynamicResponseField_resField3.setLabel("");
				balEnquiryDynamicResponseField_resField3.setType("text");
				balEnquiryDynamicResponseField_resField3.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField3.setExport(Boolean.FALSE);

				balanceEnquiryDynamicResponseField = new ArrayList<BillingCommonDynamicResponseField>();
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField1);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField2);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField3);
				
				billPaymentResponse.setDynamicResponseFields(balanceEnquiryDynamicResponseField);

			} else if (Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
					&& !mbmeBillPaymentService.getServiceId().isEmpty()
					&& mbmeBillPaymentService.getServiceId().equalsIgnoreCase("1")) {
				// 1  # DU TOP -Prepaid Recharge
				// TODO : 
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField1.setName("externaltransactionId");
				balEnquiryDynamicResponseField_resField1
						.setValue(billPaymentRequest.getExternalTransactionId());
				balEnquiryDynamicResponseField_resField1.setLabel("");
				balEnquiryDynamicResponseField_resField1.setType("text");
				balEnquiryDynamicResponseField_resField1.setVisible(Boolean.TRUE);
				balEnquiryDynamicResponseField_resField1.setExport(Boolean.TRUE);
				
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField2.setName("middlewareTransactionId");
				balEnquiryDynamicResponseField_resField2
						.setValue(rootNode.path("responseData").path("resField2").asText()); // middlewareTransactionId
				balEnquiryDynamicResponseField_resField2.setLabel("MIDDLEWARE_TRANSACTION_ID");
				balEnquiryDynamicResponseField_resField2.setType("text");
				balEnquiryDynamicResponseField_resField2.setVisible(Boolean.TRUE);
				balEnquiryDynamicResponseField_resField2.setExport(Boolean.TRUE);
				
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField3.setName("apiReturnTransactionId");
				balEnquiryDynamicResponseField_resField3
						.setValue(rootNode.path("responseData").path("resField1").asText());
				balEnquiryDynamicResponseField_resField3.setLabel("");
				balEnquiryDynamicResponseField_resField3.setType("text");
				balEnquiryDynamicResponseField_resField3.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField3.setExport(Boolean.TRUE);
				
				
//				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BillingCommonDynamicResponseField();
//				balEnquiryDynamicResponseField_resField3.setName("amountPaid");
//				balEnquiryDynamicResponseField_resField3
//						.setValue(rootNode.path("responseData").path("amountPaid").asText());
//				balEnquiryDynamicResponseField_resField3.setLabel("");
//				balEnquiryDynamicResponseField_resField3.setType("text");
//				balEnquiryDynamicResponseField_resField3.setVisible(Boolean.FALSE);
//				balEnquiryDynamicResponseField_resField3.setExport(Boolean.FALSE);

				
//				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BillingCommonDynamicResponseField();
//				balEnquiryDynamicResponseField_resField2.setName("apiReturnTransactionId");
//				balEnquiryDynamicResponseField_resField2
//						.setValue(rootNode.path("responseData").path("providerTransactionId").asText());
//				balEnquiryDynamicResponseField_resField2.setLabel("");
//				balEnquiryDynamicResponseField_resField2.setType("text");
//				balEnquiryDynamicResponseField_resField2.setVisible(Boolean.FALSE);

				
				
				
				balanceEnquiryDynamicResponseField = new ArrayList<BillingCommonDynamicResponseField>();
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField1);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField2);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField3);
				billPaymentResponse.setDynamicResponseFields(balanceEnquiryDynamicResponseField);

			} else if (Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
					&& !mbmeBillPaymentService.getServiceId().isEmpty()
					&& mbmeBillPaymentService.getServiceId().equalsIgnoreCase("19")) {
							//|| mbmeBillPaymentService.getServiceId().equalsIgnoreCase("42")
							//|| mbmeBillPaymentService.getServiceId().equalsIgnoreCase("21")) {
				
			if(Objects.nonNull(mbmeBillPaymentService.getServiceCode())
						&& !mbmeBillPaymentService.getServiceCode().isEmpty()
						&& mbmeBillPaymentService.getServiceCode().equalsIgnoreCase("EtisalatBillPayment")) {
			
				// TODO : Done
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField1.setName("externaltransactionId");
				balEnquiryDynamicResponseField_resField1
						.setValue(rootNode.path("responseData").path("transactionId").asText());
				balEnquiryDynamicResponseField_resField1.setLabel("");
				balEnquiryDynamicResponseField_resField1.setType("text");
				balEnquiryDynamicResponseField_resField1.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField1.setExport(Boolean.TRUE);
				
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField2.setName("amountPaid");
				balEnquiryDynamicResponseField_resField2
						.setValue(rootNode.path("responseData").path("amountPaid").asText());
				balEnquiryDynamicResponseField_resField2.setLabel("currency");
				balEnquiryDynamicResponseField_resField2.setType("text");
				balEnquiryDynamicResponseField_resField2.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField2.setExport(Boolean.TRUE);
				
				// 3 . apiReturnTransactionId
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField3.setName("apiReturnTransactionId");
				balEnquiryDynamicResponseField_resField3
						.setValue(rootNode.path("responseData").path("providerTransactionId").asText());
				balEnquiryDynamicResponseField_resField3.setLabel("");
				balEnquiryDynamicResponseField_resField3.setType("text");
				balEnquiryDynamicResponseField_resField3.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField3.setExport(Boolean.FALSE);
				
				// 4. transactionBillerStatus
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField4 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField4.setName("transactionBillerStatus");
				balEnquiryDynamicResponseField_resField4
						.setValue(rootNode.path("responseData").path("resField1").asText());
				balEnquiryDynamicResponseField_resField4.setLabel("");
				balEnquiryDynamicResponseField_resField4.setType("text");
				balEnquiryDynamicResponseField_resField4.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField4.setExport(Boolean.FALSE);

				// 5. serviceType
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField5 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField5.setName("serviceType");
				balEnquiryDynamicResponseField_resField5
						.setValue(rootNode.path("responseData").path("resField2").asText()); // serviceType
				balEnquiryDynamicResponseField_resField5.setLabel("");
				balEnquiryDynamicResponseField_resField5.setType("text");
				balEnquiryDynamicResponseField_resField5.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField5.setExport(Boolean.FALSE);

				// 6. transactionType
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField6 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField6.setName("transactionType");
				balEnquiryDynamicResponseField_resField6
						.setValue(rootNode.path("responseData").path("resField3").asText()); // transactionType
				balEnquiryDynamicResponseField_resField6.setLabel("");
				balEnquiryDynamicResponseField_resField6.setType("text");
				balEnquiryDynamicResponseField_resField6.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField6.setExport(Boolean.FALSE);
				// 7. transactionTime

				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField7 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField7.setName("transactionTime");
				balEnquiryDynamicResponseField_resField7
						.setValue(rootNode.path("responseData").path("resField4").asText()); // transactionTime
				balEnquiryDynamicResponseField_resField7.setLabel("");
				balEnquiryDynamicResponseField_resField7.setType("text");
				balEnquiryDynamicResponseField_resField7.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField7.setExport(Boolean.FALSE);

				// 8. replyTime

				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField8 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField8.setName("replyTime");
				balEnquiryDynamicResponseField_resField8
						.setValue(rootNode.path("responseData").path("resField5").asText()); // replyTime
				balEnquiryDynamicResponseField_resField8.setLabel("");
				balEnquiryDynamicResponseField_resField8.setType("text");
				balEnquiryDynamicResponseField_resField8.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField8.setExport(Boolean.FALSE);

				// 9. middlewareTransactionId

				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField9 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField9.setName("middlewareTransactionId");
				balEnquiryDynamicResponseField_resField9
						.setValue(rootNode.path("responseData").path("resField6").asText()); // middlewareTransactionId
				balEnquiryDynamicResponseField_resField9.setLabel("");
				balEnquiryDynamicResponseField_resField9.setType("text");
				balEnquiryDynamicResponseField_resField9.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField9.setExport(Boolean.TRUE);

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
				
			}else if(Objects.nonNull(mbmeBillPaymentService.getServiceCode())
					&& !mbmeBillPaymentService.getServiceCode().isEmpty()
					&& mbmeBillPaymentService.getServiceCode().equalsIgnoreCase("EtisalatTopUp")) {
				// TODO : Done
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField1.setName("externaltransactionId");
				balEnquiryDynamicResponseField_resField1
						.setValue(rootNode.path("responseData").path("transactionId").asText());
				balEnquiryDynamicResponseField_resField1.setLabel("");
				balEnquiryDynamicResponseField_resField1.setType("text");
				balEnquiryDynamicResponseField_resField1.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField1.setExport(Boolean.TRUE);
				
				
				// 3 . apiReturnTransactionId
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField2.setName("apiReturnTransactionId");
				balEnquiryDynamicResponseField_resField2
						.setValue(rootNode.path("responseData").path("providerTransactionId").asText());
				balEnquiryDynamicResponseField_resField2.setLabel("PROVIDER_TRANSACTION_ID");
				balEnquiryDynamicResponseField_resField2.setType("text");
				balEnquiryDynamicResponseField_resField2.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField2.setExport(Boolean.FALSE);
				
				// 4. transactionBillerStatus
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField3.setName("middlewareTransactionId");
				balEnquiryDynamicResponseField_resField3
						.setValue(rootNode.path("responseData").path("resField16").asText());
				balEnquiryDynamicResponseField_resField3.setLabel("MIDDLEWARE_TRANSACTION_ID");
				balEnquiryDynamicResponseField_resField3.setType("text");
				balEnquiryDynamicResponseField_resField3.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField3.setExport(Boolean.FALSE);

				

				balanceEnquiryDynamicResponseField = new ArrayList<BillingCommonDynamicResponseField>();
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField1);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField2);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField3);
			

				billPaymentResponse.setDynamicResponseFields(balanceEnquiryDynamicResponseField);
			}
			
				

			}else if (Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
					&& !mbmeBillPaymentService.getServiceId().isEmpty()
					&& mbmeBillPaymentService.getServiceId().equalsIgnoreCase("42")) {
							//|| mbmeBillPaymentService.getServiceId().equalsIgnoreCase("42")
							//|| mbmeBillPaymentService.getServiceId().equalsIgnoreCase("21")) {

				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField1.setName("externaltransactionId");
				balEnquiryDynamicResponseField_resField1
						.setValue(rootNode.path("responseData").path("transactionId").asText());
				balEnquiryDynamicResponseField_resField1.setLabel("");
				balEnquiryDynamicResponseField_resField1.setType("text");
				balEnquiryDynamicResponseField_resField1.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField1.setExport(Boolean.TRUE);

				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField2.setName("middlewareTransactionId");
				balEnquiryDynamicResponseField_resField2
						.setValue(rootNode.path("responseData").path("resField3").asText()); // middlewareTransactionId
				balEnquiryDynamicResponseField_resField2.setLabel("");
				balEnquiryDynamicResponseField_resField2.setType("text");
				balEnquiryDynamicResponseField_resField2.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField2.setExport(Boolean.TRUE);


				// 3 . apiReturnTransactionId
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField3.setName("apiReturnTransactionId");
				balEnquiryDynamicResponseField_resField3
						.setValue(rootNode.path("responseData").path("providerTransactionId").asText());
				balEnquiryDynamicResponseField_resField3.setLabel("");
				balEnquiryDynamicResponseField_resField3.setType("text");
				balEnquiryDynamicResponseField_resField3.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField3.setExport(Boolean.TRUE);
				
				

				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField4 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField4.setName("paymentTimeStamp");
				balEnquiryDynamicResponseField_resField4
						.setValue(rootNode.path("responseData").path("resField1").asText()); // paymentTimeStamp
				balEnquiryDynamicResponseField_resField4.setLabel("");
				balEnquiryDynamicResponseField_resField4.setType("text");
				balEnquiryDynamicResponseField_resField4.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField4.setExport(Boolean.TRUE);
				
				// 8. replyTime
				// TopupReferenceNumber
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField5 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField5.setName("TopupReferenceNumber");
				balEnquiryDynamicResponseField_resField5
						.setValue(rootNode.path("responseData").path("resField2").asText()); // replyTime
				balEnquiryDynamicResponseField_resField5.setLabel("");
				balEnquiryDynamicResponseField_resField5.setType("text");
				balEnquiryDynamicResponseField_resField5.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField5.setExport(Boolean.TRUE);
				// 9. middlewareTransactionId


				
				
				
//				
//				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BillingCommonDynamicResponseField();
//				balEnquiryDynamicResponseField_resField3.setName("amountPaid");
//				balEnquiryDynamicResponseField_resField3
//						.setValue(rootNode.path("responseData").path("amountPaid").asText());
//				balEnquiryDynamicResponseField_resField3.setLabel("");
//				balEnquiryDynamicResponseField_resField3.setType("text");
//				balEnquiryDynamicResponseField_resField3.setVisible(Boolean.FALSE);
//				balEnquiryDynamicResponseField_resField3.setExport(Boolean.TRUE);

				

				// 4. transactionBillerStatus
//				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField4 = new BillingCommonDynamicResponseField();
//				balEnquiryDynamicResponseField_resField4.setName("transactionBillerStatus");
//				balEnquiryDynamicResponseField_resField4
//						.setValue(rootNode.path("responseData").path("resField1").asText());
//				balEnquiryDynamicResponseField_resField4.setLabel("");
//				balEnquiryDynamicResponseField_resField4.setType("text");
//				balEnquiryDynamicResponseField_resField4.setVisible(Boolean.FALSE);
//				balEnquiryDynamicResponseField_resField4.setExport(Boolean.TRUE);
//
//				// 5. serviceType
//				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField5 = new BillingCommonDynamicResponseField();
//				balEnquiryDynamicResponseField_resField5.setName("serviceType");
//				balEnquiryDynamicResponseField_resField5
//						.setValue(rootNode.path("responseData").path("resField2").asText()); // serviceType
//				balEnquiryDynamicResponseField_resField5.setLabel("");
//				balEnquiryDynamicResponseField_resField5.setType("text");
//				balEnquiryDynamicResponseField_resField5.setVisible(Boolean.FALSE);
//				balEnquiryDynamicResponseField_resField5.setExport(Boolean.TRUE);

				// 6. transactionType
//				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField6 = new BillingCommonDynamicResponseField();
//				balEnquiryDynamicResponseField_resField6.setName("transactionType");
//				balEnquiryDynamicResponseField_resField6
//						.setValue(rootNode.path("responseData").path("resField3").asText()); // transactionType
//				balEnquiryDynamicResponseField_resField6.setLabel("");
//				balEnquiryDynamicResponseField_resField6.setType("text");
//				balEnquiryDynamicResponseField_resField6.setVisible(Boolean.FALSE);
//				balEnquiryDynamicResponseField_resField5.setExport(Boolean.TRUE);
				// 7. paymentTimeStamp
				
//				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField9 = new BillingCommonDynamicResponseField();
//				balEnquiryDynamicResponseField_resField9.setName("middlewareTransactionId");
//				balEnquiryDynamicResponseField_resField9
//						.setValue(rootNode.path("responseData").path("resField6").asText()); // middlewareTransactionId
//				balEnquiryDynamicResponseField_resField9.setLabel("");
//				balEnquiryDynamicResponseField_resField9.setType("text");
//				balEnquiryDynamicResponseField_resField9.setVisible(Boolean.FALSE);

				balanceEnquiryDynamicResponseField = new ArrayList<BillingCommonDynamicResponseField>();
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField1);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField2);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField3);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField4);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField5);

				billPaymentResponse.setDynamicResponseFields(balanceEnquiryDynamicResponseField);

			}else if (Objects.nonNull(mbmeBillPaymentService) && Objects.nonNull(mbmeBillPaymentService.getServiceId())
					&& !mbmeBillPaymentService.getServiceId().isEmpty()
					&& mbmeBillPaymentService.getServiceId().equalsIgnoreCase("21")) {
							//|| mbmeBillPaymentService.getServiceId().equalsIgnoreCase("42")
							//|| mbmeBillPaymentService.getServiceId().equalsIgnoreCase("21")) {
// TODO : Done
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField1 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField1.setName("externaltransactionId");
				balEnquiryDynamicResponseField_resField1
						.setValue(rootNode.path("responseData").path("transactionId").asText());
				balEnquiryDynamicResponseField_resField1.setLabel("");
				balEnquiryDynamicResponseField_resField1.setType("text");
				balEnquiryDynamicResponseField_resField1.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField1.setExport(Boolean.TRUE);
				
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField2 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField2.setName("orderId");
				balEnquiryDynamicResponseField_resField2
						.setValue(rootNode.path("responseData").path("resField1").asText());
				balEnquiryDynamicResponseField_resField2.setLabel("");
				balEnquiryDynamicResponseField_resField2.setType("text");
				balEnquiryDynamicResponseField_resField2.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField2.setExport(Boolean.TRUE);
				

				// 3 . apiReturnTransactionId
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField3 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField3.setName("apiReturnTransactionId");
				balEnquiryDynamicResponseField_resField3
						.setValue(rootNode.path("responseData").path("providerTransactionId").asText());
				balEnquiryDynamicResponseField_resField3.setLabel("");
				balEnquiryDynamicResponseField_resField3.setType("text");
				balEnquiryDynamicResponseField_resField3.setVisible(Boolean.FALSE);
				balEnquiryDynamicResponseField_resField3.setExport(Boolean.TRUE);
				
				// 4. transactionBillerStatus
				BillingCommonDynamicResponseField balEnquiryDynamicResponseField_resField4 = new BillingCommonDynamicResponseField();
				balEnquiryDynamicResponseField_resField4.setName("newBalance");
				balEnquiryDynamicResponseField_resField4
						.setValue(rootNode.path("responseData").path("resField2").asText());
				balEnquiryDynamicResponseField_resField4.setLabel("NEW_BALANCE");
				balEnquiryDynamicResponseField_resField4.setType("currency");
				balEnquiryDynamicResponseField_resField4.setVisible(Boolean.TRUE);
				balEnquiryDynamicResponseField_resField4.setExport(Boolean.TRUE);
				
				

				balanceEnquiryDynamicResponseField = new ArrayList<BillingCommonDynamicResponseField>();
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField1);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField2);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField3);
				balanceEnquiryDynamicResponseField.add(balEnquiryDynamicResponseField_resField4);


				billPaymentResponse.setDynamicResponseFields(balanceEnquiryDynamicResponseField);

			}else {

			}
			return billPaymentResponse;

		} else {

			logger.error("Failed to process processMbmeBillPayment request.");
			throw new RuntimeException("Failed to process processMbmeBillPayment request");
		}

	}

	private BillingCommonDynamicResponseField createField(String name, String value) {
		BillingCommonDynamicResponseField field = new BillingCommonDynamicResponseField();
		field.setName(name);
		field.setValue(value);
		field.setLabel("");
		field.setType("text");
		field.setVisible(false);
		return field;
	}

	private BillPaymentResponse buildBillPaymentResponse(BillPaymentRequest billPaymentRequest, String responseBody,
			MbmeBillPaymentRequest mbmeBillPaymentService) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(responseBody);

		BillPaymentResponse response = new BillPaymentResponse();
		response.setDeviceTransactionId(billPaymentRequest.getDeviceTransactionId());
		response.setExternalTransactionId(billPaymentRequest.getExternalTransactionId());
		response.setResponseCode(rootNode.path("responseCode").asText());
		response.setResponseStatus(rootNode.path("status").asText());
		response.setResponseMessage(rootNode.path("responseMessage").asText());
		response.setStatus(rootNode.path("responseMessage").asText());

		List<BillingCommonDynamicResponseField> dynamicFields = new ArrayList<>();

		if (Objects.nonNull(mbmeBillPaymentService.getServiceId())) {
			String serviceId = mbmeBillPaymentService.getServiceId();
			switch (serviceId) {
			case "103", "1" -> {
				dynamicFields.add(createField("amountPaid", rootNode.path("responseData").path("amountPaid").asText()));
				dynamicFields.add(createField("apiReturnTransactionId",
						rootNode.path("responseData").path("providerTransactionId").asText()));
			}
			case "19", "42", "21" -> {
				dynamicFields.add(createField("externaltransactionId",
						rootNode.path("responseData").path("transactionId").asText()));
				dynamicFields.add(createField("amountPaid", rootNode.path("responseData").path("amountPaid").asText()));
				dynamicFields.add(createField("apiReturnTransactionId",
						rootNode.path("responseData").path("providerTransactionId").asText()));
				dynamicFields.add(createField("transactionBillerStatus",
						rootNode.path("responseData").path("resField1").asText()));
				dynamicFields.add(createField("serviceType", rootNode.path("responseData").path("resField2").asText()));
				dynamicFields
						.add(createField("transactionType", rootNode.path("responseData").path("resField3").asText()));
				dynamicFields
						.add(createField("transactionTime", rootNode.path("responseData").path("resField4").asText()));
				dynamicFields.add(createField("replyTime", rootNode.path("responseData").path("resField5").asText()));
				dynamicFields.add(createField("middlewareTransactionId",
						rootNode.path("responseData").path("resField6").asText()));
			}
			}
		}

		response.setDynamicResponseFields(dynamicFields);
		return response;
	}

	
	public String createRequestJson(BillPaymentRequest billPaymentRequest) {
	    if (Objects.nonNull(billPaymentRequest) && Objects.nonNull(billPaymentRequest.getServiceId())
	            && !billPaymentRequest.getServiceId().isEmpty()) {

	        String serviceId = billPaymentRequest.getServiceId();
	        List<DynamicRequestField> dynamicFields = billPaymentRequest.getDynamicRequestFields();

	        // Validate that dynamicFields is not null or empty
	        if (dynamicFields == null || dynamicFields.isEmpty()) {
	            throw new IllegalArgumentException("Dynamic fields are missing for service ID: " + serviceId);
	        }

	        return switch (serviceId.toLowerCase()) {
	            case "103" -> {
	                // Validation for case 103
	                if (dynamicFields.get(0).getName() == null ||
	                        !dynamicFields.get(0).getName().equalsIgnoreCase("accountNumber") &&
	                        dynamicFields.get(0).getValue() == null) {
	                    throw new IllegalArgumentException("Invalid or missing 'accountNumber' in dynamic fields for service ID 103");
	                }

	                yield buildRequestJson(
	                        billPaymentRequest,
	                        dynamicFields.get(0).getValue(),
	                        "CREDIT_ACCOUNT_PAY",
	                        null,
	                        null
	                );
	            }

	            case "1" -> {
	                // Validation for case 1
	            	 if (dynamicFields.get(0).getName() == null ||
		                        !dynamicFields.get(0).getName().equalsIgnoreCase("accountNumber") &&
		                        dynamicFields.get(0).getValue() == null) {
		                    throw new IllegalArgumentException("Invalid or missing 'accountNumber' in dynamic fields for service ID 103");
		                }
	            	 
				/*
				 * if (dynamicFields.get(0).getValue() == null) { throw new
				 * IllegalArgumentException("Missing value for dynamic field in service ID 1");
				 * }
				 */

	                yield buildRequestJson(
	                        billPaymentRequest,
	                        dynamicFields.get(0).getValue(),
	                        "time",
	                        null,
	                        null
	                );
	            }

	            case "42", "20", "21" -> {
	                // Validation for cases 42, 20, 21
	                String field1 = dynamicFields.size() > 0 ? dynamicFields.get(0).getValue() : null;
	                String field2 = dynamicFields.size() > 1 ? dynamicFields.get(1).getValue() : null;
	                String field3 = dynamicFields.size() > 2 ? dynamicFields.get(2).getValue() : null;

	                if (field1 == null) {
	                    throw new IllegalArgumentException("Missing required field1 for service ID: " + serviceId);
	                }

	                yield buildRequestJson(
	                        billPaymentRequest,
	                        field1,
	                        field2,
	                        field3,
	                        null
	                );
	            }

	            case "19" -> {
	                // Validation for case 19
	            	  String field1 = null;
	            	  String field2 = null;
	            	  String field3 = null;
	            	  String field4 = null;
	            	if (dynamicFields.get(0).getName() != null ||
	                        dynamicFields.get(0).getName().equalsIgnoreCase("accountNumber") &&
	                        dynamicFields.get(0).getValue() != null) {
	            		field1 = !StringUtils.isEmpty(dynamicFields.get(0).getValue()) ? dynamicFields.get(0).getValue() : null;
	                }
	            	
	            	if (dynamicFields.get(1).getName() != null ||
	                        dynamicFields.get(1).getName().equalsIgnoreCase("serviceType") &&
	                        dynamicFields.get(1).getValue() != null) {
	            		field2 = !StringUtils.isEmpty(dynamicFields.get(1).getValue()) ? dynamicFields.get(1).getValue() : null;
	                }
	            	
	            	if (dynamicFields.get(2).getName() != null ||
	                        dynamicFields.get(2).getName().equalsIgnoreCase("serviceType") &&
	                        dynamicFields.get(2).getValue() != null) {
	            		field3 = !StringUtils.isEmpty(dynamicFields.get(2).getValue()) ? dynamicFields.get(2).getValue() : null;
	                }
	            	
	            	if (dynamicFields.get(3).getName() != null ||
	                        dynamicFields.get(3).getName().equalsIgnoreCase("serviceType") &&
	                        dynamicFields.get(3).getValue() != null) {
	            		field4 = !StringUtils.isEmpty(dynamicFields.get(3).getValue()) ? dynamicFields.get(3).getValue() : null;
	                }
	            	
//	                String field1 = dynamicFields.size() > 1 ? dynamicFields.get(1).getValue() : null;
//	                String field2 = dynamicFields.size() > 0 ? dynamicFields.get(0).getValue() : null;
//	                String field3 = dynamicFields.size() > 2 ? dynamicFields.get(2).getValue() : null;
//	                String field4 = dynamicFields.size() > 3 ? dynamicFields.get(3).getValue() : null;

//	                if (field2 == null) {
//	                    throw new IllegalArgumentException("Missing required field2 for service ID 19");
//	                }

	                yield buildRequestJson(
	                        billPaymentRequest,
	                        field1,
	                        field2,
	                        field3,
	                        field4
	                );
	            }

	            default -> throw new IllegalArgumentException("Unsupported service ID: " + serviceId);
	        };
	    }

	    throw new IllegalArgumentException("Invalid BillPaymentRequest: null or missing service ID");
	}
	
	
//	public String createRequestJson(BillPaymentRequest billPaymentRequest) {
//		if (Objects.nonNull(billPaymentRequest) && Objects.nonNull(billPaymentRequest.getServiceId())
//				&& !billPaymentRequest.getServiceId().isEmpty()) {
//
//			String serviceId = billPaymentRequest.getServiceId();
//			List<DynamicRequestField> dynamicFields = billPaymentRequest.getDynamicRequestFields();
//
//			return switch (serviceId.toLowerCase()) {
//			case "103" ->
//				buildRequestJson(billPaymentRequest, dynamicFields.size() > 0 ? dynamicFields.get(0).getValue() : null,
//						"CREDIT_ACCOUNT_PAY", null, null);
//
//			case "1" -> buildRequestJson(billPaymentRequest,
//					dynamicFields.size() > 0 ? dynamicFields.get(0).getValue() : null, "time", null, null);
//
//			case "42", "20", "21" ->
//				buildRequestJson(billPaymentRequest, dynamicFields.size() > 0 ? dynamicFields.get(0).getValue() : null,
//						dynamicFields.size() > 1 ? dynamicFields.get(1).getValue() : null,
//						dynamicFields.size() > 2 ? dynamicFields.get(2).getValue() : null, null);
//
//			case "19" ->
//				buildRequestJson(billPaymentRequest, dynamicFields.size() > 1 ? dynamicFields.get(1).getValue() : null,
//						dynamicFields.size() > 0 ? dynamicFields.get(0).getValue() : null,
//						dynamicFields.size() > 2 ? dynamicFields.get(2).getValue() : null,
//						dynamicFields.size() > 3 ? dynamicFields.get(3).getValue() : null);
//
//			default -> throw new IllegalArgumentException("Unsupported service ID: " + serviceId);
//			};
//		}
//		return null; // Optional: you could throw IllegalArgumentException instead of returning null
//	}

	private String buildRequestJson(BillPaymentRequest billPaymentRequest, String reqField1, String reqField2,
			String reqField3, String reqField4) {
		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("{\r\n").append("    \"transactionId\": \"")
				.append(billPaymentRequest.getExternalTransactionId()).append("\",\r\n")
				.append("    \"merchantId\": \"").append(billPaymentRequest.getMerchantId()).append("\",\r\n")
				.append("    \"merchantLocation\": \"").append(billPaymentRequest.getMerchantLocation())
				.append("\",\r\n").append("    \"method\": \"pay\",\r\n").append("    \"serviceId\": \"")
				.append(billPaymentRequest.getServiceId()).append("\",\r\n")
				.append("    \"paymentMode\": \"Cash\",\r\n").append("    \"paidAmount\": \"")
				.append(billPaymentRequest.getPaidAmount()).append("\",\r\n").append("    \"lang\": \"en\",\r\n")
				.append("    \"reqField1\": \"").append(reqField1).append("\",\r\n").append("    \"reqField2\": \"")
				.append(reqField2).append("\"");

		if (reqField3 != null) {
			jsonBuilder.append(",\r\n").append("    \"reqField3\": \"").append(reqField3).append("\"");
		}
		if (reqField4 != null) {
			jsonBuilder.append(",\r\n").append("    \"reqField4\": \"").append(reqField4).append("\"");
		}

		jsonBuilder.append("\r\n}");
		return jsonBuilder.toString();
	}

	private HttpHeaders createHeaders() throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(tokenService.getValidToken().getAccessToken());
		return headers;
	}

	private BillingCommonDynamicResponseField createDynamicField(String name, String value) {
		BillingCommonDynamicResponseField field = new BillingCommonDynamicResponseField();
		field.setName(name);
		field.setValue(value);
		field.setLabel("");
		field.setType("text");
		field.setVisible(Boolean.FALSE);
		return field;
	}

	private String getSafeText(JsonNode node, String fieldName) {
		if (node != null && node.hasNonNull(fieldName)) {
			return node.path(fieldName).asText("");
		}
		return "";
	}
	
	
	private List<BillingCommonDynamicResponseField> prepareDynamicResponseFields(JsonNode rootNode, String serviceId) {
        List<BillingCommonDynamicResponseField> responseFields = new ArrayList<>();

        JsonNode responseData = rootNode.path("responseData");

        if ("103".equalsIgnoreCase(serviceId) || "1".equalsIgnoreCase(serviceId)) {
            responseFields.add(createDynamicField("amountPaid", getSafeText(responseData, "amountPaid")));
            responseFields.add(createDynamicField("apiReturnTransactionId", getSafeText(responseData, "providerTransactionId")));
        } else if ("19".equalsIgnoreCase(serviceId) || "21".equalsIgnoreCase(serviceId) || "42".equalsIgnoreCase(serviceId)) {
            responseFields.add(createDynamicField("externalTransactionId", getSafeText(responseData, "transactionId")));
            responseFields.add(createDynamicField("amountPaid", getSafeText(responseData, "amountPaid")));
            responseFields.add(createDynamicField("apiReturnTransactionId", getSafeText(responseData, "providerTransactionId")));
            responseFields.add(createDynamicField("transactionBillerStatus", getSafeText(responseData, "resField1")));
            responseFields.add(createDynamicField("serviceType", getSafeText(responseData, "resField2")));
            responseFields.add(createDynamicField("transactionType", getSafeText(responseData, "resField3")));
            responseFields.add(createDynamicField("transactionTime", getSafeText(responseData, "resField4")));
            responseFields.add(createDynamicField("replyTime", getSafeText(responseData, "resField5")));
            responseFields.add(createDynamicField("middlewareTransactionId", getSafeText(responseData, "resField6")));
        }

        return responseFields;
    }

}