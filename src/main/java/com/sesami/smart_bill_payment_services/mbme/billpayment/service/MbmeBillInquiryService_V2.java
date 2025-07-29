package com.sesami.smart_bill_payment_services.mbme.billpayment.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryDynamicRequestField;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillingCommonDynamicResponseField;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.ListItem;
import com.sesami.smart_bill_payment_services.mbme.billpayment.entity.BillInquiryEntity;
import com.sesami.smart_bill_payment_services.mbme.billpayment.repository.BillInquiryRepository;
import com.sesami.smart_bill_payment_services.mbme.billpayment.util.DynamicBillInquiryRequestUtil;
import com.sesami.smart_bill_payment_services.mbme.token.service.TokenService;

@Service
public class MbmeBillPaymentService_new {

	private static final Logger logger = LoggerFactory.getLogger(MbmeBillInquiryService.class);

	@Autowired
	private BillInquiryRepository billInquiryRepository;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${mbme.api.balance-payment.url}")
	private String billPaymentUrl;

	public BalanceEnquiryResponse processBillInquiry(BalanceEnquiryRequest balanceEnquiryRequest) throws IOException {
		Objects.requireNonNull(balanceEnquiryRequest, "BalanceEnquiryRequest must not be null");

		logger.info("Processing MBME BillInquiry request for transactionId: {}",
				balanceEnquiryRequest.getDeviceTransactionId());

		var headers = createHeaders();
		var jsonRequest = DynamicBillInquiryRequestUtil.generateDynamicBillInquiryRequest(balanceEnquiryRequest);
		var httpRequest = new HttpEntity<>(jsonRequest, headers);

		logger.info("Request JSON: {}", jsonRequest);

		var response = restTemplate.exchange(billPaymentUrl, HttpMethod.POST, httpRequest, String.class);
		saveBillInquiryEntity(balanceEnquiryRequest, jsonRequest, response);

		return processResponse(balanceEnquiryRequest, response);
	}

	private HttpHeaders createHeaders() throws IOException {
		var headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + tokenService.getValidToken().getAccessToken());
		return headers;
	}

	private void saveBillInquiryEntity(BalanceEnquiryRequest request, String jsonRequest,
			ResponseEntity<String> response) {
		var billInquiry = new BillInquiryEntity();
		billInquiry.setTransactionId(request.getDeviceTransactionId());
		billInquiry.setMerchantId(request.getMerchantId());
		billInquiry.setMerchantLocation(request.getMerchantLocation());
		billInquiry.setRequestJson(jsonRequest);
		billInquiry.setResponseJson(response.getBody());
		billInquiry.setTimestamp(LocalDateTime.now());
		billInquiryRepository.save(billInquiry);
	}

	private BalanceEnquiryResponse processResponse(BalanceEnquiryRequest request, ResponseEntity<String> response)
			throws IOException {
		if (response.getStatusCode() == HttpStatus.OK) {
			logger.info("Response JSON: {}", response.getBody());
			return parseSuccessResponse(request, response.getBody());
		} else {
			logger.error("Failed to process MBME BillInquiry request.");
			throw new RuntimeException("Failed to process MBME BillInquiry request");
		}
	}

	private BalanceEnquiryResponse parseSuccessResponse(BalanceEnquiryRequest balanceEnquiryRequest,
			String responseBody) throws IOException {
		var objectMapper = new ObjectMapper();
		var rootNode = objectMapper.readTree(responseBody);

		var response = new BalanceEnquiryResponse();
		response.setDeviceTransactionId(balanceEnquiryRequest.getDeviceTransactionId());
		response.setExternalTransactionId(balanceEnquiryRequest.getExternalTransactionId());
		response.setCurrencyCode("AED");
		response.setResponseCode(rootNode.path("responseCode").asText());
		response.setResponseStatus(rootNode.path("status").asText());
		response.setResponseMessage(rootNode.path("responseMessage").asText());
		response.setInternalWebServiceCode("000");
		response.setInternalWebServiceDesc("SUCCESS");

		response.setDynamicResponseFields(generateDynamicResponseFields(balanceEnquiryRequest, rootNode));
		response.setDueAmount(rootNode.path("responseData").path("amount").asText());

		setAdditionalResponseDetails(response, balanceEnquiryRequest);
		return response;
	}

	private List<BillingCommonDynamicResponseField> generateDynamicResponseFields(
			BalanceEnquiryRequest balanceEnquiryRequest, JsonNode rootNode) {
		var dynamicFields = new ArrayList<BillingCommonDynamicResponseField>();

		switch (balanceEnquiryRequest.getServiceId()) {
		case "103" -> { // DU Bill payment service
			dynamicFields.add(createDynamicField("amount", rootNode.path("responseData").path("amount").asText(),
					"DUE_AMOUNT", "currency", true, false, null));
			dynamicFields
					.add(createDynamicField("customerName", rootNode.path("responseData").path("custName").asText(),
							"CUSTOMER_NAME", "text", true, false, null));
			dynamicFields.add(
					createDynamicField("accountNumber", rootNode.path("responseData").path("accountNumber").asText(),
							"ACCOUNT_NUMBER", "text", true, false, null));
			dynamicFields.add(createDynamicField("apiReturnTransactionId",
					rootNode.path("responseData").path("resField1").asText(), "", "text", true, false, null));
			dynamicFields.add(createDynamicField("middlewareTransactionId",
					rootNode.path("responseData").path("resField2").asText(), "", "text", true, false, null));
		}
		case "1" -> { // DU TopUp Recharge
			dynamicFields.add(createDynamicField("middlewareTransactionId",
					rootNode.path("responseData").path("resField2").asText(), "", "text", true, false, null));
			dynamicFields.add(createDynamicField("apiReturnTransactionId",
					rootNode.path("responseData").path("resField1").asText(), "", "text", true, false, null));
			dynamicFields.add(
					createDynamicField("accountNumber", rootNode.path("responseData").path("accountNumber").asText(),
							"PHONE_NUMBER", "text", true, false, null));
			dynamicFields
					.add(createDynamicField("minAmount", "5.00", "MIN_DEPOSIT_AMOUNT", "currency", true, false, null));
			dynamicFields.add(
					createDynamicField("maxAmount", "1000.00", "MAX_DEPOSIT_AMOUNT", "currency", true, false, null));
		}
		case "19" -> { // EtisalatTopUp /EtisalatBillPayment
			if (Objects.nonNull(balanceEnquiryRequest.getServiceCode())
					&& !balanceEnquiryRequest.getServiceCode().isEmpty()
					&& balanceEnquiryRequest.getServiceCode().equalsIgnoreCase("EtisalatTopUp")) {
				dynamicFields.add(createDynamicField("externaltransactionId",
						rootNode.path("responseData").path("transactionId").asText(), "", "text", false, true, null));
				dynamicFields.add(createDynamicField("apiReturnTransactionId",
						rootNode.path("responseData").path("providerTransactionId").asText(), "PROVIDER_TRANSACTION_ID",
						"text", false, false, null));
				dynamicFields.add(createDynamicField("middlewareTransactionId",
						rootNode.path("responseData").path("resField16").asText(), "MIDDLEWARE_TRANSACTION_ID", "text",
						false, false, null));
			} else if (Objects.nonNull(balanceEnquiryRequest.getServiceCode())
					&& !balanceEnquiryRequest.getServiceCode().isEmpty()
					&& balanceEnquiryRequest.getServiceCode().equalsIgnoreCase("EtisalatBillPayment")) {
				dynamicFields.add(createDynamicField("externaltransactionId",
						rootNode.path("responseData").path("transactionId").asText(), "", "text", false, true, null));
				dynamicFields
						.add(createDynamicField("amountPaid", rootNode.path("responseData").path("amountPaid").asText(),
								"currency", "text", false, true, null));
				dynamicFields.add(createDynamicField("apiReturnTransactionId",
						rootNode.path("responseData").path("providerTransactionId").asText(), "", "text", false, false,
						null));
				dynamicFields.add(createDynamicField("transactionBillerStatus",
						rootNode.path("responseData").path("resField1").asText(), "", "text", false, false, null));
				dynamicFields.add(createDynamicField("serviceType",
						rootNode.path("responseData").path("resField2").asText(), "", "text", false, false, null));
				dynamicFields.add(createDynamicField("transactionType",
						rootNode.path("responseData").path("resField3").asText(), "", "text", false, false, null));
				dynamicFields.add(createDynamicField("transactionTime",
						rootNode.path("responseData").path("resField4").asText(), "", "currency", true, false, null));
				dynamicFields.add(createDynamicField("replyTime",
						rootNode.path("responseData").path("resField5").asText(), "", "text", false, false, null));
				dynamicFields.add(createDynamicField("middlewareTransactionId",
						rootNode.path("responseData").path("resField6").asText(), "", "text", false, true, null));
			}

		}
		case "42" -> { // NOL Card TopUp / Recharge
			dynamicFields.add(createDynamicField("externaltransactionId",
					rootNode.path("responseData").path("transactionId").asText(), "", "text", false, true, null));
			dynamicFields.add(createDynamicField("middlewareTransactionId",
					rootNode.path("responseData").path("resField3").asText(), "", "text", false, true, null));
			dynamicFields.add(createDynamicField("apiReturnTransactionId",
					rootNode.path("responseData").path("resField1").asText(), "", "text", false, true, null));
			dynamicFields.add(createDynamicField("TopupReferenceNumber",
					rootNode.path("responseData").path("resField2").asText(), "", "text", false, true, null));
		}
		case "20150" -> { // Dubai DED Payment
			dynamicFields.add(createDynamicField("commissionValue", "20.00", "CUSTOMER_COMMISSION", "currency", true,
					false, null));
			dynamicFields
					.add(createDynamicField("voucherNumber", rootNode.path("responseData").path("resField1").asText(),
							"VOUCHER_NUMBER", "text", true, false, null));
			dynamicFields.add(createDynamicField("dueAmount", rootNode.path("responseData").path("amount").asText(),
					"DUE_AMOUNT", "currency", true, false, null));
			dynamicFields
					.add(createDynamicField("voucherDate", rootNode.path("responseData").path("resField2").asText(),
							"VOUCHER_DATE", "text", true, false, null));
			dynamicFields.add(
					createDynamicField("voucherExpireDate", rootNode.path("responseData").path("resField3").asText(),
							"VOUCHER_EXPIRE_DATE", "text", true, false, null));
			dynamicFields.add(createDynamicField("cashFlag", rootNode.path("responseData").path("resField4").asText(),
					"", "text", false, false, null));
			dynamicFields.add(createDynamicField("datedChequeFlag",
					rootNode.path("responseData").path("resField5").asText(), "", "text", false, false, null));
			dynamicFields.add(createDynamicField("creditCardFlag",
					rootNode.path("responseData").path("resField6").asText(), "", "text", false, false, null));

		}
		case "20147" -> { // Hello / Five recharge

			var productsList = new ArrayList<ListItem>();
			var arrayResponse = rootNode.path("responseData").path("arrayResponse");
		    for (var i = 0; i < arrayResponse.size(); i++) {
		        var productNode = arrayResponse.get(i);
		        var productAttributes = new ArrayList<ListItem>();

		        productAttributes.add(createListItem("id", productNode.path("id2").asText(), "", "text", true));
		        productAttributes.add(createListItem("serviceId", productNode.path("serviceId").asText(), "", "text", true));
		        productAttributes.add(createListItem("description", productNode.path("desc").asText(), "PRODUCT_DESCRIPTION", "text", false));
		        productAttributes.add(createListItem("amount", productNode.path("Sum").asText(), "", "currency", true));
		        productAttributes.add(createListItem("image", productNode.path("image").asText(), "", "image", false));
		        productAttributes.add(createListItem("voucherBrand", productNode.path("brand").asText(), "", "text", true));

		        var product = new ListItem();
		        product.setRowNumber(i + 1);
		        product.setList(productAttributes);
		        productsList.add(product);
		    }

		    dynamicFields.add(createDynamicField("productsList", "", "SELECT_VOUCHER", "buttons", true, false, productsList));

		}
		case "112" -> { // International TopUp

		    dynamicFields.add(createDynamicField(
		        "providerName",
		        rootNode.path("responseData").path("resField2").asText(),
		        "SERVICE_PROVIDER",
		        "text",
		        true,
		        false,
		        null
		    ));

		    dynamicFields.add(createDynamicField(
		        "phoneNumber",
		        balanceEnquiryRequest.getDynamicRequestFields() == null ? null :
		            balanceEnquiryRequest.getDynamicRequestFields().stream()
		                .filter(field -> "phoneNumber".equals(field.getName()))
		                .map(BalanceEnquiryDynamicRequestField::getValue)
		                .findFirst()
		                .orElse(null),
		        "PHONE_NUMBER",
		        "text",
		        true,
		        false,
		        null
		    ));

		    // Get ReceiveCurrencyIso from first item of resField3 array in response
		    String receiveCurrencyIso = "";
		    var resField3 = rootNode.path("responseData").path("resField3");
		    if (resField3.isArray() && resField3.size() > 0) {
		        receiveCurrencyIso = resField3.get(0).path("Maximum").path("ReceiveCurrencyIso").asText();
		    }

		    dynamicFields.add(createDynamicField(
		        "amounts",
		        receiveCurrencyIso,
		        "DUE_AMOUNT",
		        "currency",
		        true,
		        false,
		        null
		    ));

		    // parse products list from resField3
		    var productsList = new ArrayList<ListItem>();
		    var arrayResponse = rootNode.path("responseData").path("resField3");
		    for (var i = 0; i < arrayResponse.size(); i++) {
		        var productNode = arrayResponse.get(i);
		        var productAttributes = new ArrayList<ListItem>();

		        productAttributes.add(createListItem("SkuCode", productNode.path("SkuCode").asText(), "PRODUCT_ID", "text", true));
		        productAttributes.add(createListItem("ProviderCode", productNode.path("ProviderCode").asText(), "SERVICE_ID", "text", true));
		        productAttributes.add(createListItem("description", productNode.path("DefaultDisplayText").asText(), "PRODUCT_DESCRIPTION", "text", false));
		        productAttributes.add(createListItem("amount", productNode.path("Maximum").path("ReceiveValue").asText(), "AMOUNT", "currency", true));
		        productAttributes.add(createListItem("currency", productNode.path("Maximum").path("ReceiveCurrencyIso").asText(), "CURRENCY", "text", true));
		        productAttributes.add(createListItem("benefits", productNode.path("Benefits").toString(), "BENEFITS", "text", false));
		        productAttributes.add(createListItem("validity", productNode.path("ValidityPeriodIso").asText(), "VALIDITY", "text", false));
		        productAttributes.add(createListItem("region", productNode.path("RegionCode").asText(), "REGION", "text", true));
		        productAttributes.add(createListItem("paymentType", productNode.path("PaymentTypes").toString(), "PAYMENT_TYPE", "text", true));
		        // Add more fields if needed from productNode...

		        var product = new ListItem();
		        product.setRowNumber(i + 1);
		        product.setList(productAttributes);
		        productsList.add(product);
		    }

		    dynamicFields.add(createDynamicField(
		        "productsList",
		        "",
		        "SELECT_VOUCHER",
		        "buttons",
		        true,
		        false,
		        productsList
		    ));
		}
		case "18" -> { // Dubai Police Traffic Fines 

			
			
		    // Add trafficFileNo field
		    dynamicFields.add(createDynamicField("trafficFileNo", rootNode.path("responseData").path("resField2").asText(),
		            "TRAFFIC_FILE_NUMBER", "text", true, false, null));

		    // Add pedestrianFine field
		    dynamicFields.add(createDynamicField("pedestrianFine", rootNode.path("responseData").path("resField3").asText(),
		            "", "text", false, false, null));

		    // Add ticketsList field
		    ArrayList<ListItem> ticketsList = new ArrayList<>();
		    JsonNode arrayResponse = rootNode.path("responseData").path("arrayResponse");
		    for (int i = 0; i < arrayResponse.size(); i++) {
		        JsonNode ticketNode = arrayResponse.get(i);
		        JsonNode ticketsArray = ticketNode.path("Tickets");

		        for (int j = 0; j < ticketsArray.size(); j++) {
		            JsonNode ticket = ticketsArray.get(j);
		            List<ListItem> ticketAttributes = new ArrayList<>();

		            ticketAttributes.add(createListItem("ticketNo", ticket.path("TicketNo").asText(), "TICKET_NUMBER", "text", true));
		            ticketAttributes.add(createListItem("flagPayable", ticket.path("isPayable").asText(), "PAYABLE_FLAG", "text", false));
		            ticketAttributes.add(createListItem("ticketId", ticket.path("TicketId").asText(), "", "text", true));
		            ticketAttributes.add(createListItem("fineSource", ticketNode.path("FineSource").asText(), "FINE_SOURCE", "text", false));
		            ticketAttributes.add(createListItem("ticketDate", ticket.path("ticketDateField").asText(), "TICKET_DATE", "text", false));
		            ticketAttributes.add(createListItem("ticketTime", ticket.path("ticketTimeField").asText(), "TICKET_TIME", "text", false));
		            ticketAttributes.add(createListItem("ticketDescription", ticket.path("TicketDescription").asText(), "TICKET_DESCRIPTION", "text", false));
		            ticketAttributes.add(createListItem("ticketAmount", ticket.path("CalculatedFineAmount").asText(), "PRODUCT_DESCRIPTION", "currency", false));
		            ticketAttributes.add(createListItem("locationDescription", ticket.path("locationDescription").asText(), "LOCATION_DESCRIPTION", "text", false));
		            ticketAttributes.add(createListItem("penaltyFine", ticket.path("PenaltyFine").asText(), "PENALTY_FINE", "text", false));
		            ticketAttributes.add(createListItem("knowledgeFee", ticket.path("KnowledgeFee").asText(), "KNOWLEDGE_FEE", "text", false));
		            ticketAttributes.add(createListItem("licenceNo", ticket.path("LicenseNo").asText(), "LICENCE_NUMBER", "text", false));
		            ticketAttributes.add(createListItem("plateFrom", ticket.path("PlateFrom").asText(), "PLATE_FROM", "text", false));
		            ticketAttributes.add(createListItem("plateCode", ticket.path("PlateCode").asText(), "PLATE_CODE", "text", false));
		            ticketAttributes.add(createListItem("plateNo", ticket.path("PlateNo").asText(), "PLATE_NUMBER", "text", false));

		            ListItem ticketItem = new ListItem();
		            ticketItem.setRowNumber(j + 1);
		            ticketItem.setIsPayable(ticket.path("isPayable").asText()	); // Assuming "1" means payable
		            ticketItem.setList(ticketAttributes);

		            ticketsList.add(ticketItem);
		        }
		    }

		    dynamicFields.add(createDynamicField("ticketsList", "", "", "table", true, false, ticketsList));
		   // response.setDynamicResponseFields(dynamicFields);
			
			
			
			
//			var productsList = new ArrayList<ListItem>();
//			var arrayResponse = rootNode.path("responseData").path("arrayResponse");
//		    for (var i = 0; i < arrayResponse.size(); i++) {
//		        var productNode = arrayResponse.get(i);
//		        var productAttributes = new ArrayList<ListItem>();
//
//		        
//		        productAttributes.add(createListItem("id", productNode.path("id2").asText(), "TRAFFIC_FILE_NUMBER", "text", true));
//		        productAttributes.add(createListItem("serviceId", productNode.path("serviceId").asText(), "", "text", true));
//		        productAttributes.add(createListItem("description", productNode.path("desc").asText(), "PRODUCT_DESCRIPTION", "text", false));
//		        productAttributes.add(createListItem("amount", productNode.path("Sum").asText(), "", "currency", true));
//		        productAttributes.add(createListItem("image", productNode.path("image").asText(), "", "image", false));
//		        productAttributes.add(createListItem("voucherBrand", productNode.path("brand").asText(), "", "text", true));
//
//		        var product = new ListItem();
//		        product.setRowNumber(i + 1);
//		        product.setList(productAttributes);
//		        productsList.add(product);
//		    }
//
//		    dynamicFields.add(createDynamicField("ticketsList", "", "", "table", true, false, productsList));
//		    
//		   if(balanceEnquiryRequest.getServiceCode() != null && balanceEnquiryRequest.getServiceCode().equalsIgnoreCase("byTrfNo")) {
//			   dynamicFields.add(createDynamicField("blackPoints", rootNode.path("responseData").path("resField1").asText(),
//						"", "text", true, false, null));
//			    
//			    dynamicFields.add(createDynamicField("trafficFileNo", rootNode.path("responseData").path("resField2").asText(),
//						"TRAFFIC_FILE_NUMBER", "text", true, false, null));
//			    dynamicFields.add(createDynamicField("pedestrianFine", rootNode.path("responseData").path("resField3").asText(),
//						"", "text", false, false, null));
//		    } else  if(balanceEnquiryRequest.getServiceCode() != null && balanceEnquiryRequest.getServiceCode().equalsIgnoreCase("byPlateNo")) {
//		    	dynamicFields.add(createDynamicField("trafficFileNo", rootNode.path("responseData").path("resField2").asText(),
//						"TRAFFIC_FILE_NUMBER", "text", true, false, null));
//			   
//		   }else  if(balanceEnquiryRequest.getServiceCode() != null && balanceEnquiryRequest.getServiceCode().equalsIgnoreCase("byTicketNo")) {
//		    	dynamicFields.add(createDynamicField("trafficFileNo", rootNode.path("responseData").path("resField2").asText(),
//						"TRAFFIC_FILE_NUMBER", "text", true, false, null));
//			   
//		   }else  if(balanceEnquiryRequest.getServiceCode() != null && balanceEnquiryRequest.getServiceCode().equalsIgnoreCase("byLicenceNo")) {
//		    	dynamicFields.add(createDynamicField("trafficFileNo", rootNode.path("responseData").path("resField2").asText(),
//						"TRAFFIC_FILE_NUMBER", "text", true, false, null));
//			   
//		   }
		    
		   
		    

		}
		default -> logger.warn("Unhandled serviceId: {}", balanceEnquiryRequest.getServiceId());
		}

		return dynamicFields;
	}

	private ListItem createListItem(String name, String value, String label, String type, boolean usedForPayment) {
	    var listItem = new ListItem();
	    listItem.setName(name);
	    listItem.setValue(value);
	    listItem.setLabel(label);
	    listItem.setType(type);
	    listItem.setUsedForPayment(usedForPayment);
	    return listItem;
	}

	
	private BillingCommonDynamicResponseField createDynamicField(String name, String value, String label, String type,
			boolean visible, boolean isExportAllowed, ArrayList<ListItem> list) {
		var field = new BillingCommonDynamicResponseField();
		field.setName(name);
		field.setValue(value);
		field.setLabel(label);
		field.setType(type);
		field.setVisible(visible);
		field.setExport(isExportAllowed); // Assuming export is always true for these fields
		field.setList(list); // Initialize list if needed
		return field;
	}

	private void setAdditionalResponseDetails(BalanceEnquiryResponse response,
			BalanceEnquiryRequest balanceEnquiryRequest) {
		switch (balanceEnquiryRequest.getServiceId()) {
		case "103", "1" -> {
			response.setMinAmount("50.00");
			response.setMaxAmount("10000.00");
			response.setCustomerCommission(true);
			response.setCommissionPercentage("0");
			response.setPartialPayment(true);
			response.setChangeHandling("credit");
		}
		case "18" -> {
			response.setMinAmount("");
			response.setMaxAmount("");
			response.setCustomerCommission(false);
			response.setPartialPayment(false);
			response.setChangeHandling("credit");
		}
		case "19" -> {
			response.setMinAmount("20.00");
			response.setMaxAmount("10000.00");
			response.setCustomerCommission(true);
			response.setPartialPayment(true);
			response.setChangeHandling("credit");
		}
		case "42" -> {
			response.setMinAmount("10.00");
			response.setMaxAmount("500.00");
			response.setCustomerCommission(true);
			response.setPartialPayment(true);
			response.setChangeHandling("credit");
		}
		default -> logger.warn("Unhandled serviceId for additional response details: {}",
				balanceEnquiryRequest.getServiceId());
		}
	}
}
