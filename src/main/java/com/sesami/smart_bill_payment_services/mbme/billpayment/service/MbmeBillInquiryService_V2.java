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
import com.sesami.smart_bill_payment_services.exception.SmartServiceCommonException;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillingCommonDynamicResponseField;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.ListItem;
import com.sesami.smart_bill_payment_services.mbme.billpayment.entity.BillInquiryEntity;
import com.sesami.smart_bill_payment_services.mbme.billpayment.repository.BillInquiryRepository;
import com.sesami.smart_bill_payment_services.mbme.billpayment.util.DynamicBillInquiryRequestUtil;
import com.sesami.smart_bill_payment_services.mbme.token.service.TokenService;

@Service
public class MbmeBillInquiryService_V2 {

	private static final Logger logger = LoggerFactory.getLogger(MbmeBillInquiryService.class);

	@Autowired
	private BillInquiryRepository billInquiryRepository;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${mbme.api.balance-payment.url}")
	private String billPaymentUrl;
	
	private static final String TEXT_FIELD_TYPE = "text";
	private static final String CURRENCY_FIELD_TYPE = "currency";
	private static final String DUE_AMOUNT_LABEL = "DUE_AMOUNT";

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
		// billInquiryRepository.save(billInquiry);
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
// TODO : 
	private List<BillingCommonDynamicResponseField> generateDynamicResponseFields(
			BalanceEnquiryRequest balanceEnquiryRequest, JsonNode rootNode) {
		var dynamicFields = new ArrayList<BillingCommonDynamicResponseField>();
// createDynamicField(String name, String value, String label, String type,		boolean visible, boolean isExportAllowed, ArrayList<ListItem> list)
		switch (balanceEnquiryRequest.getServiceId()) {
		case "103" -> { // DU Bill payment service
			dynamicFields.add(createDynamicField("amount", rootNode.path("responseData").path("amount").asText(),
					"DUE_AMOUNT", "currency", Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields
					.add(createDynamicField("customerName", rootNode.path("responseData").path("custName").asText(),
							"CUSTOMER_NAME", "text", Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields.add(
					createDynamicField("accountNumber", rootNode.path("responseData").path("accountNumber").asText(),
							"ACCOUNT_NUMBER", "text", Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("apiReturnTransactionId",
					rootNode.path("responseData").path("resField1").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("middlewareTransactionId",
					rootNode.path("responseData").path("resField2").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
		}
		case "1" -> { // DU TopUp Recharge
			dynamicFields.add(createDynamicField("middlewareTransactionId",
					rootNode.path("responseData").path("resField2").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("apiReturnTransactionId",
					rootNode.path("responseData").path("resField1").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
			dynamicFields.add(
					createDynamicField("accountNumber", rootNode.path("responseData").path("accountNumber").asText(),
							"PHONE_NUMBER", "text", Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields
					.add(createDynamicField("minAmount", "5.00", "MIN_DEPOSIT_AMOUNT", "currency", Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields.add(
					createDynamicField("maxAmount", "1000.00", "MAX_DEPOSIT_AMOUNT", "currency", Boolean.TRUE, Boolean.FALSE, null));
		}
		case "19" -> { // EtisalatTopUp 
			if (Objects.nonNull(balanceEnquiryRequest.getServiceCode())
					&& !balanceEnquiryRequest.getServiceCode().isEmpty()
					&& balanceEnquiryRequest.getServiceCode().equalsIgnoreCase("EtisalatTopUp")) {
				
				
				dynamicFields.add(createDynamicField("accountNumber",
						rootNode.path("responseData").path("accountNumber").asText(), "ACCOUNT_NUMBER", "text", Boolean.TRUE,
						Boolean.FALSE, null));
				dynamicFields.add(createDynamicField("apiReturnTransactionId",
						rootNode.path("responseData").path("providerTransactionId").asText(), "", "text", Boolean.FALSE, Boolean.FALSE,
						null));
				dynamicFields.add(createDynamicField("currentBalance", rootNode.path("responseData").path("amount").asText(),
						"CURRENT_BALANCE", "currency", Boolean.TRUE, Boolean.FALSE, null));
				
				dynamicFields.add(
						createDynamicField("minAmount",
								rootNode.path("responseData").path("resField5").asText(), "MIN_DEPOSIT_AMOUNT", "currency", Boolean.TRUE, Boolean.FALSE, null));
				
				dynamicFields.add(createDynamicField("maxAmount", 
						rootNode.path("responseData").path("resField6").asText(), "MAX_DEPOSIT_AMOUNT", "currency", Boolean.TRUE,
						Boolean.FALSE, null));
				dynamicFields.add(createDynamicField("transactionTime",
						rootNode.path("responseData").path("resField3").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
				
				dynamicFields.add(createDynamicField("replyTime",
						rootNode.path("responseData").path("resField4").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
			
				//EtisalatBillPayment
			} else if (Objects.nonNull(balanceEnquiryRequest.getServiceCode())
					&& !balanceEnquiryRequest.getServiceCode().isEmpty()
					&& balanceEnquiryRequest.getServiceCode().equalsIgnoreCase("EtisalatBillPayment")) {

				dynamicFields.add(createDynamicField("accountNumber",
						rootNode.path("responseData").path("accountNumber").asText(), "ACCOUNT_NUMBER", "text", Boolean.TRUE,
						Boolean.FALSE, null));
				dynamicFields.add(createDynamicField("apiReturnTransactionId",
						rootNode.path("responseData").path("providerTransactionId").asText(), "", "text", Boolean.FALSE, Boolean.FALSE,
						null));
				dynamicFields.add(createDynamicField("amount", rootNode.path("responseData").path("amount").asText(),
						DUE_AMOUNT_LABEL, CURRENCY_FIELD_TYPE, Boolean.TRUE, Boolean.FALSE, null));
				
//				dynamicFields.add(
//						createDynamicField("minAmount",
//								rootNode.path("responseData").path("resField5").asText(),"MIN_DEPOSIT_AMOUNT", "currency", Boolean.TRUE, Boolean.FALSE, null));
				
				dynamicFields.add(createDynamicField(
					    "minAmount", 
					    rootNode.path("responseData").path("resField5").asText().isEmpty() ? "20.00" : rootNode.path("responseData").path("resField5").asText(),
					    "MIN_DEPOSIT_AMOUNT", 
					    "currency", 
					    Boolean.TRUE, 
					    Boolean.FALSE, 
					    null
					));
				
//				dynamicFields.add(createDynamicField("maxAmount", 
//						rootNode.path("responseData").path("resField6").asText(),"MAX_DEPOSIT_AMOUNT", "currency", Boolean.TRUE,
//						Boolean.FALSE, null));
				dynamicFields.add(createDynamicField(
					    "maxAmount", 
					    rootNode.path("responseData").path("resField6").asText().isEmpty() ? "10000.00" : rootNode.path("responseData").path("resField6").asText(),
					    "MAX_DEPOSIT_AMOUNT", 
					    "currency", 
					    Boolean.TRUE, 
					    Boolean.FALSE, 
					    null
					));
				
				dynamicFields.add(createDynamicField("serviceType",
						rootNode.path("responseData").path("resField1").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));

				dynamicFields.add(createDynamicField("transactionType",
						rootNode.path("responseData").path("resField2").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
				dynamicFields.add(createDynamicField("transactionTime",
						rootNode.path("responseData").path("resField3").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
				dynamicFields.add(createDynamicField("replyTime",
						rootNode.path("responseData").path("resField4").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
			}

		}
		case "21" -> {  // Salik Topup Service
			dynamicFields
			.add(createDynamicField("customerName", rootNode.path("responseData").path("custName").asText(),
					"CUSTOMER_NAME", "text", Boolean.TRUE, Boolean.FALSE, null));

			dynamicFields.add(createDynamicField("amount", rootNode.path("responseData").path("amount").asText(),
					"BALANCE_AMOUNT", "currency", Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("apiReturnTransactionId",
					rootNode.path("responseData").path("providerTransactionId").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
			
		}
		case "42" -> { // NOL Card TopUp -- Recharge
			dynamicFields.add(createDynamicField("timeStamp",
					rootNode.path("responseData").path("resField1").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
			
			dynamicFields.add(createDynamicField("apiReturnTransactionId",
					rootNode.path("responseData").path("resField2").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
			
		}
		case "20150" -> { // Dubai DED Payment
			dynamicFields.add(createDynamicField("commissionValue", "20.00", "CUSTOMER_COMMISSION", "currency", Boolean.TRUE,
					Boolean.FALSE, null));
			dynamicFields
					.add(createDynamicField("voucherNumber", rootNode.path("responseData").path("resField1").asText(),
							"VOUCHER_NUMBER", "text", Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("dueAmount", rootNode.path("responseData").path("amount").asText(),
					"DUE_AMOUNT", "currency", Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields
					.add(createDynamicField("voucherDate", rootNode.path("responseData").path("resField2").asText(),
							"VOUCHER_DATE", "text", Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields.add(
					createDynamicField("voucherExpireDate", rootNode.path("responseData").path("resField3").asText(),
							"VOUCHER_EXPIRE_DATE", "text", Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("cashFlag", rootNode.path("responseData").path("resField4").asText(),
					"", "text", Boolean.FALSE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("datedChequeFlag",
					rootNode.path("responseData").path("resField5").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("creditCardFlag",
					rootNode.path("responseData").path("resField6").asText(), "", "text", Boolean.FALSE, Boolean.FALSE, null));

		}
		case "20147" -> { // Hello / Five recharge

			var productsList = new ArrayList<ListItem>();
			var arrayResponse = rootNode.path("responseData").path("arrayResponse");
		    for (var i = 0; i < arrayResponse.size(); i++) {
		        var productNode = arrayResponse.get(i);
		        var productAttributes = new ArrayList<ListItem>();

		        productAttributes.add(createListItem("id", productNode.path("id2").asText(), "", "text", Boolean.TRUE));
		        productAttributes.add(createListItem("serviceId", productNode.path("serviceId").asText(), "", "text", Boolean.TRUE));
		        productAttributes.add(createListItem("description", productNode.path("desc").asText(), "PRODUCT_DESCRIPTION", "text", Boolean.FALSE));
		        productAttributes.add(createListItem("amount", productNode.path("Sum").asText(), "", "currency", Boolean.TRUE));
		        productAttributes.add(createListItem("image", productNode.path("image").asText(), "", "image", Boolean.FALSE));
		        productAttributes.add(createListItem("voucherBrand", productNode.path("brand").asText(), "", "text", Boolean.TRUE));

		        var product = new ListItem();
		        product.setRowNumber(i + 1);
		        product.setList(productAttributes);
		        productsList.add(product);
		    }

		    dynamicFields.add(createDynamicField("productsList", "", "SELECT_VOUCHER", "buttons", Boolean.TRUE, Boolean.FALSE, productsList));

		}
		case "112" -> { // International TopUp

		    // Check for error response
		    String responseCode = rootNode.path("responseCode").asText();
		    if ("302".equals(responseCode)) {
		        String billerErrorCode = rootNode.path("billerErrorCode").asText();
		        String billerMessage = rootNode.path("billerMessage").asText();
		        String responseMessage = rootNode.path("responseMessage").asText();

		        throw new SmartServiceCommonException(
		            String.format("Error Code: %s, Biller Error Code: %s, Message: %s", responseCode, billerErrorCode, responseMessage),
		            HttpStatus.TOO_MANY_REQUESTS
		        );
		    } else if ("301".equals(responseCode)) {
		        String responseMessage = rootNode.path("responseMessage").asText();

		        throw new SmartServiceCommonException(
		            String.format("Duplicate Transaction Error: %s", responseMessage),
		            HttpStatus.CONFLICT
		        );
		    }

		    // Parse providerName
		    dynamicFields.add(createDynamicField(
		        "providerName",
		       "",
		        // rootNode.path("responseData").path("dynamicResponseFields").findValue("resField2").path("value").asText(), 
		        "SERVICE_PROVIDER",
		        "text",
		        Boolean.TRUE,
		        Boolean.FALSE,
		        null
		    ));

		    // Parse phoneNumber
		    dynamicFields.add(createDynamicField(
		        "phoneNumber",
		       // rootNode.path("responseData").path("dynamicResponseFields").findValue("phoneNumber").path("value").asText(),
		        "",
		        "PHONE_NUMBER",
		        "text",
		        Boolean.TRUE,
		        Boolean.FALSE,
		        null
		    ));

		    // Parse amounts (list of products)
		    var productsList = new ArrayList<ListItem>();
		    var amountsField = rootNode.path("responseData").path("dynamicResponseFields").findValue("amounts");
		    var listArray = amountsField.path("list");

		    for (var i = 0; i < listArray.size(); i++) {
		        var productNode = listArray.get(i);
		        var productAttributes = new ArrayList<ListItem>();

		        productAttributes.add(createListItem("id", productNode.findValue("id").path("value").asText(), "SKU_CODE", "text", Boolean.TRUE));
		        productAttributes.add(createListItem("description", productNode.findValue("description").path("value").asText(), "PRODUCT_DESCRIPTION", "text", Boolean.FALSE));
		        productAttributes.add(createListItem("value", productNode.findValue("value").path("value").asText(), "PRODUCT_VALUE", "currency", Boolean.TRUE));
		        productAttributes.add(createListItem("foreignValue", productNode.findValue("foreignValue").path("value").asText(), "PRODUCT_FOREIGN_VALUE", "number", Boolean.TRUE));
		        productAttributes.add(createListItem("foreignCurrencyIso", productNode.findValue("foreignCurrencyIso").path("value").asText(), "FOREIGN_CURRENCY_ISO", "text", Boolean.FALSE));

		        var product = new ListItem();
		        product.setRowNumber(i + 1);
		        product.setList(productAttributes);
		        productsList.add(product);
		    }

		    dynamicFields.add(createDynamicField(
		        "amounts",
		        "",
		        "SELECT_TOPUP_ITEM",
		        "buttons",
		        Boolean.TRUE,
		        Boolean.FALSE,
		        productsList
		    ));
		}
		case "18" -> { // Dubai Police Traffic Fines 

			
			
		    // Add trafficFileNo field
		    dynamicFields.add(createDynamicField("trafficFileNo", rootNode.path("responseData").path("resField2").asText(),
		            "TRAFFIC_FILE_NUMBER", "text", Boolean.TRUE, Boolean.FALSE, null));

		    // Add pedestrianFine field
		    dynamicFields.add(createDynamicField("pedestrianFine", rootNode.path("responseData").path("resField3").asText(),
		            "", "text", Boolean.FALSE, Boolean.FALSE, null));

		    // Add ticketsList field
		    ArrayList<ListItem> ticketsList = new ArrayList<>();
		    JsonNode arrayResponse = rootNode.path("responseData").path("arrayResponse");
		    for (int i = 0; i < arrayResponse.size(); i++) {
		        JsonNode ticketNode = arrayResponse.get(i);
		        JsonNode ticketsArray = ticketNode.path("Tickets");

		        for (int j = 0; j < ticketsArray.size(); j++) {
		            JsonNode ticket = ticketsArray.get(j);
		            List<ListItem> ticketAttributes = new ArrayList<>();

		            ticketAttributes.add(createListItem("ticketNo", ticket.path("TicketNo").asText(), "TICKET_NUMBER", "text", Boolean.TRUE));
		            ticketAttributes.add(createListItem("flagPayable", ticket.path("isPayable").asText(), "PAYABLE_FLAG", "text", Boolean.FALSE));
		            ticketAttributes.add(createListItem("ticketId", ticket.path("TicketId").asText(), "", "text", Boolean.TRUE));
		            ticketAttributes.add(createListItem("fineSource", ticketNode.path("FineSource").asText(), "FINE_SOURCE", "text", Boolean.FALSE));
		            ticketAttributes.add(createListItem("ticketDate", ticket.path("ticketDateField").asText(), "TICKET_DATE", "text", Boolean.FALSE));
		            ticketAttributes.add(createListItem("ticketTime", ticket.path("ticketTimeField").asText(), "TICKET_TIME", "text", Boolean.FALSE));
		            ticketAttributes.add(createListItem("ticketDescription", ticket.path("TicketDescription").asText(), "TICKET_DESCRIPTION", "text", Boolean.FALSE));
		            ticketAttributes.add(createListItem("ticketAmount", ticket.path("CalculatedFineAmount").asText(), "PRODUCT_DESCRIPTION", "currency", Boolean.FALSE));
		            ticketAttributes.add(createListItem("locationDescription", ticket.path("locationDescription").asText(), "LOCATION_DESCRIPTION", "text", Boolean.FALSE));
		            ticketAttributes.add(createListItem("penaltyFine", ticket.path("PenaltyFine").asText(), "PENALTY_FINE", "text", Boolean.FALSE));
		            ticketAttributes.add(createListItem("knowledgeFee", ticket.path("KnowledgeFee").asText(), "KNOWLEDGE_FEE", "text", Boolean.FALSE));
		            ticketAttributes.add(createListItem("licenceNo", ticket.path("LicenseNo").asText(), "LICENCE_NUMBER", "text", Boolean.FALSE));
		            ticketAttributes.add(createListItem("plateFrom", ticket.path("PlateFrom").asText(), "PLATE_FROM", "text", Boolean.FALSE));
		            ticketAttributes.add(createListItem("plateCode", ticket.path("PlateCode").asText(), "PLATE_CODE", "text", Boolean.FALSE));
		            ticketAttributes.add(createListItem("plateNo", ticket.path("PlateNo").asText(), "PLATE_NUMBER", "text", Boolean.FALSE));

		            ListItem ticketItem = new ListItem();
		            ticketItem.setRowNumber(j + 1);
		            ticketItem.setIsPayable(ticket.path("isPayable").asText()	); // Assuming "1" means payable
		            ticketItem.setList(ticketAttributes);

		            ticketsList.add(ticketItem);
		        }
		    }

		    dynamicFields.add(createDynamicField("ticketsList", "", "", "table", Boolean.TRUE, Boolean.FALSE, ticketsList));
		   
		    

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
		field.setExport(isExportAllowed); // Assuming export is always Boolean.TRUE for these fields
		field.setList(list); // Initialize list if needed
		return field;
	}

	private void setAdditionalResponseDetails(BalanceEnquiryResponse response,
			BalanceEnquiryRequest balanceEnquiryRequest) {
		switch (balanceEnquiryRequest.getServiceId()) {
		case "103" -> {
			response.setMinAmount("50.00");
			response.setMaxAmount("10000.00");
			response.setCustomerCommission(Boolean.TRUE);
			response.setCommissionPercentage("0");
			response.setPartialPayment(Boolean.TRUE);
			response.setChangeHandling("credit");
		}case  "1" -> {
			response.setMinAmount("5.00");
			response.setMaxAmount("1000.00");
			response.setCustomerCommission(Boolean.TRUE);
			response.setCommissionPercentage("0");
			response.setPartialPayment(Boolean.TRUE);
			response.setChangeHandling("credit");
		}
		case "18" -> {
			response.setMinAmount("");
			response.setMaxAmount("");
			response.setCustomerCommission(Boolean.FALSE);
			response.setPartialPayment(Boolean.FALSE);
			response.setChangeHandling("credit");
		}
		case "19" -> {
			if (Objects.nonNull(balanceEnquiryRequest.getServiceCode())
					&& !balanceEnquiryRequest.getServiceCode().isEmpty()
					&& balanceEnquiryRequest.getServiceCode().equalsIgnoreCase("EtisalatTopUp")) {
				
				response.setMinAmount("10.00");
				response.setMaxAmount("10000.00");
				response.setCustomerCommission(Boolean.TRUE);
				response.setPartialPayment(Boolean.TRUE);
				response.setBanknoteCut("10.00");;
				response.setChangeHandling("credit");
			}else if (Objects.nonNull(balanceEnquiryRequest.getServiceCode())
					&& !balanceEnquiryRequest.getServiceCode().isEmpty()
					&& balanceEnquiryRequest.getServiceCode().equalsIgnoreCase("EtisalatBillPayment")) {
				response.setMinAmount("10.00");
				response.setMaxAmount("10000.00");
				response.setCustomerCommission(Boolean.TRUE);
				response.setPartialPayment(Boolean.TRUE);
				response.setChangeHandling("credit");
			} else {
				logger.warn("Unhandled Etisalat service code: {}", balanceEnquiryRequest.getServiceCode());
			}
			
		}
		case "21" -> {
			response.setMinAmount("50.00");
			response.setMaxAmount("1000.00");
			response.setCustomerCommission(Boolean.TRUE);
			response.setPartialPayment(Boolean.TRUE);
			response.setBanknoteCut("50.00");;
			response.setChangeHandling("credit");
		}
		case "42" -> {
			response.setMinAmount("10.00");
			response.setMaxAmount("500.00");
			response.setCustomerCommission(Boolean.TRUE);
			response.setPartialPayment(Boolean.TRUE);
			response.setChangeHandling("credit");
		}
		default -> logger.warn("Unhandled serviceId for additional response details: {}",
				balanceEnquiryRequest.getServiceId());
		}
	}
}
