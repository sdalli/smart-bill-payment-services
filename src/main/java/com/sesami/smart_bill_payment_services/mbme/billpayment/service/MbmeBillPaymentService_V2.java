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
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.DynamicRequestField;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.ListItem;
import com.sesami.smart_bill_payment_services.mbme.billpayment.util.DynamicBillPaymentServiceRequestUtil;
import com.sesami.smart_bill_payment_services.mbme.token.service.TokenService;

@Service
public class MbmeBillPaymentService_V2 {

    private static final Logger logger = LoggerFactory.getLogger(MbmeBillPaymentService_V2.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${mbme.api.balance-payment.url}")
    private String billPaymentUrl;
    
    
    private static final String TEXT_FIELD_TYPE = "text";
    private static final String CURRENCY_FIELD_TYPE = "currency";
    private static final String EXTERNAL_TRANSACTION_ID_NAME = "externalTransactionId";;
    private static final String MIDDLEWARE_TRANSACTION_ID_NAME = "middlewareTransactionId";
    private static final String AMOUNT_PAID_NAME = "amountPaid";
    private static final String CUSTOMER_COMMISSION = "20.00";
    private static final String VOUCHER_NUMBER_LABEL = "VOUCHER_NUMBER";
    private static final String DUE_AMOUNT_LABEL = "DUE_AMOUNT";
    private static final String VOUCHER_DATE_LABEL = "VOUCHER_DATE";
    private static final String VOUCHER_EXPIRE_DATE_LABEL = "VOUCHER_EXPIRE_DATE";

    public BillPaymentResponse processBillPayment(BillPaymentRequest billPaymentRequest) throws IOException {
        Objects.requireNonNull(billPaymentRequest, "BillPaymentRequest must not be null");

        logger.info("Processing MBME BillPayment request for transactionId: {}",
                billPaymentRequest.getDeviceTransactionId());

        var headers = createHeaders();
        var jsonRequest = DynamicBillPaymentServiceRequestUtil.generateDynamicBillPaymentRequest(billPaymentRequest);
        var httpRequest = new HttpEntity<>(jsonRequest, headers);

        logger.info("Request JSON: {}", jsonRequest);

        var response = restTemplate.exchange(billPaymentUrl, HttpMethod.POST, httpRequest, String.class);

		if (response == null) {
			logger.error("Received null response from MBME BillPayment service.");
			throw new RuntimeException("Received null response from MBME BillPayment service");
		}

		logger.info("Received response with status code: {}", response.getStatusCode());

		// Process the response
		return processResponse(billPaymentRequest, response);
    }
    
    private BillPaymentResponse processResponse(BillPaymentRequest request, ResponseEntity<String> response)
			throws IOException {
		if (response.getStatusCode() == HttpStatus.OK) {
			logger.info("Response JSON: {}", response.getBody());
			return parseSuccessResponse(request, response.getBody());
		} else {
			logger.error("Failed to process MBME BillInquiry request.");
			throw new RuntimeException("Failed to process MBME BillInquiry request");
		}
	}

    public HttpHeaders createHeaders() throws IOException {
        var headers = new HttpHeaders();
        headers.setBearerAuth(tokenService.getValidToken().getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    


    private BillPaymentResponse parseSuccessResponse(BillPaymentRequest balanceEnquiryRequest,
			String responseBody) throws IOException {
		var objectMapper = new ObjectMapper();
		var rootNode = objectMapper.readTree(responseBody);

		var response = new BillPaymentResponse();
		response.setDeviceTransactionId(balanceEnquiryRequest.getDeviceTransactionId());
		response.setExternalTransactionId(balanceEnquiryRequest.getExternalTransactionId());
		response.setResponseCode(rootNode.path("responseCode").asText());
		response.setResponseStatus(rootNode.path("status").asText());
		response.setResponseMessage(rootNode.path("responseMessage").asText());
		
		response.setCustomerMessage("");
		response.setInternalWebServiceCode("000");
		response.setInternalWebServiceDesc("SUCCESS");

		response.setDynamicResponseFields(generateDynamicResponseFields(balanceEnquiryRequest, rootNode));

		// setAdditionalResponseDetails(response, balanceEnquiryRequest);
		return response;
	}
    public List<BillingCommonDynamicResponseField> generateDynamicResponseFields(
    		BillPaymentRequest billPaymentRequest, JsonNode rootNode) {
		var dynamicFields = new ArrayList<BillingCommonDynamicResponseField>();

		switch (billPaymentRequest.getServiceId()) {
		case "103" -> { // DU Bill payment service
			dynamicFields.add(createDynamicField(EXTERNAL_TRANSACTION_ID_NAME, billPaymentRequest.getExternalTransactionId(),
					"", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			dynamicFields.add(createDynamicField(AMOUNT_PAID_NAME, rootNode.path("responseData").path("amountPaid").asText(),
					"", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("apiReturnTransactionId",
					rootNode.path("responseData").path("providerTransactionId").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			
		}
		case "1" -> { // DU TopUp Recharge
			dynamicFields.add(createDynamicField(EXTERNAL_TRANSACTION_ID_NAME, billPaymentRequest.getExternalTransactionId(),
					"", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			dynamicFields.add(createDynamicField("MIDDLEWARE_TRANSACTION_ID_NAME",
					rootNode.path("responseData").path("resField1").asText(), "MIDDLEWARE_TRANSACTION_ID", TEXT_FIELD_TYPE, Boolean.TRUE, Boolean.TRUE, null));
			dynamicFields.add(createDynamicField("apiReturnTransactionId",
					rootNode.path("responseData").path("providerTransactionId").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			
		}
		case "19" -> { // EtisalatTopUp /EtisalatBillPayment
			if (Objects.nonNull(billPaymentRequest.getServiceCode())
					&& !billPaymentRequest.getServiceCode().isEmpty()
					&& billPaymentRequest.getServiceCode().equalsIgnoreCase("EtisalatTopUp")) {
				
				
				dynamicFields.add(createDynamicField(EXTERNAL_TRANSACTION_ID_NAME,
						rootNode.path("responseData").path("transactionId").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
				dynamicFields.add(createDynamicField("apiReturnTransactionId",
						rootNode.path("responseData").path("providerTransactionId").asText(), "PROVIDER_TRANSACTION_ID",
						TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE, null));
				dynamicFields.add(createDynamicField(MIDDLEWARE_TRANSACTION_ID_NAME,
						rootNode.path("responseData").path("resField16").asText(), "MIDDLEWARE_TRANSACTION_ID", TEXT_FIELD_TYPE,
						Boolean.FALSE, Boolean.FALSE, null));
			
			} else if (Objects.nonNull(billPaymentRequest.getServiceCode())
					&& !billPaymentRequest.getServiceCode().isEmpty()
					&& billPaymentRequest.getServiceCode().equalsIgnoreCase("EtisalatBillPayment")) {
				dynamicFields.add(createDynamicField(EXTERNAL_TRANSACTION_ID_NAME,
						rootNode.path("responseData").path("transactionId").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
				dynamicFields
						.add(createDynamicField(AMOUNT_PAID_NAME, rootNode.path("responseData").path("amountPaid").asText(),
								"currency", CURRENCY_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
				dynamicFields.add(createDynamicField("apiReturnTransactionId",
						rootNode.path("responseData").path("providerTransactionId").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE,
						null));
				dynamicFields.add(createDynamicField("transactionBillerStatus",
						rootNode.path("responseData").path("resField1").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE, null));
				dynamicFields.add(createDynamicField("serviceType",
						rootNode.path("responseData").path("resField2").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE, null));
				dynamicFields.add(createDynamicField("transactionType",
						rootNode.path("responseData").path("resField3").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE, null));
				dynamicFields.add(createDynamicField("transactionTime",
						rootNode.path("responseData").path("resField4").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE, null));
				dynamicFields.add(createDynamicField("replyTime",
						rootNode.path("responseData").path("resField5").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE, null));
				dynamicFields.add(createDynamicField(MIDDLEWARE_TRANSACTION_ID_NAME,
						rootNode.path("responseData").path("resField6").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			}

		}
		case "21" -> { // Salik 
			dynamicFields.add(createDynamicField(EXTERNAL_TRANSACTION_ID_NAME,
					billPaymentRequest.getExternalTransactionId(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			dynamicFields.add(createDynamicField("orderId",
					rootNode.path("responseData").path("resField1").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			dynamicFields.add(createDynamicField("apiReturnTransactionId",
					rootNode.path("responseData").path("providerTransactionId").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			dynamicFields.add(createDynamicField("newBalance",
					rootNode.path("responseData").path("amountPaid").asText(), "NEW_BALANCE", CURRENCY_FIELD_TYPE, Boolean.TRUE, Boolean.FALSE, null));
			
		}
		case "42" -> { // NOL Card TopUp / Recharge
			dynamicFields.add(createDynamicField(EXTERNAL_TRANSACTION_ID_NAME,
					rootNode.path("responseData").path("transactionId").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			dynamicFields.add(createDynamicField(MIDDLEWARE_TRANSACTION_ID_NAME,
					rootNode.path("responseData").path("resField3").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			dynamicFields.add(createDynamicField("paymentTimeStamp",
					rootNode.path("responseData").path("resField1").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			dynamicFields.add(createDynamicField("TopupReferenceNumber",
					rootNode.path("responseData").path("resField2").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
		}
		case "20150" -> { // Dubai DED Payment
			dynamicFields.add(createDynamicField("commissionValue", "20.00", "CUSTOMER_COMMISSION", "currency", Boolean.TRUE,
					Boolean.FALSE, null));
			dynamicFields
					.add(createDynamicField("voucherNumber", rootNode.path("responseData").path("resField1").asText(),
							"VOUCHER_NUMBER", TEXT_FIELD_TYPE, Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("dueAmount", rootNode.path("responseData").path("amount").asText(),
					"DUE_AMOUNT", "currency", Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields
					.add(createDynamicField("voucherDate", rootNode.path("responseData").path("resField2").asText(),
							"VOUCHER_DATE", TEXT_FIELD_TYPE, Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields.add(
					createDynamicField("voucherExpireDate", rootNode.path("responseData").path("resField3").asText(),
							"VOUCHER_EXPIRE_DATE", TEXT_FIELD_TYPE, Boolean.TRUE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("cashFlag", rootNode.path("responseData").path("resField4").asText(),
					"", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("datedChequeFlag",
					rootNode.path("responseData").path("resField5").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE, null));
			dynamicFields.add(createDynamicField("creditCardFlag",
					rootNode.path("responseData").path("resField6").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE, null));

		}
		case "20147" -> { // Hello / Five recharge

			var productsList = new ArrayList<ListItem>();
			var arrayResponse = rootNode.path("responseData").path("arrayResponse");
		    for (var i = 0; i < arrayResponse.size(); i++) {
		        var productNode = arrayResponse.get(i);
		        var productAttributes = new ArrayList<ListItem>();

		        productAttributes.add(createListItem("id", productNode.path("id2").asText(), "", TEXT_FIELD_TYPE, Boolean.TRUE));
		        productAttributes.add(createListItem("serviceId", productNode.path("serviceId").asText(), "", TEXT_FIELD_TYPE, Boolean.TRUE));
		        productAttributes.add(createListItem("description", productNode.path("desc").asText(), "PRODUCT_DESCRIPTION", TEXT_FIELD_TYPE, Boolean.FALSE));
		        productAttributes.add(createListItem("amount", productNode.path("Sum").asText(), "", "currency", Boolean.TRUE));
		        productAttributes.add(createListItem("image", productNode.path("image").asText(), "", "image", Boolean.FALSE));
		        productAttributes.add(createListItem("voucherBrand", productNode.path("brand").asText(), "", TEXT_FIELD_TYPE, Boolean.TRUE));

		        var product = new ListItem();
		        product.setRowNumber(i + 1);
		        product.setList(productAttributes);
		        productsList.add(product);
		    }

		    dynamicFields.add(createDynamicField("productsList", "", "SELECT_VOUCHER", "buttons", Boolean.TRUE, Boolean.FALSE, productsList));

		}
		case "112" -> { // International TopUp
			dynamicFields.add(createDynamicField(EXTERNAL_TRANSACTION_ID_NAME, billPaymentRequest.getExternalTransactionId(),
					"", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			dynamicFields.add(createDynamicField(MIDDLEWARE_TRANSACTION_ID_NAME, rootNode.path("responseData").path("transactionId").asText(),
					"", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));

			dynamicFields.add(createDynamicField("providerTransactionId",
					rootNode.path("responseData").path("providerTransactionId").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			
			
			dynamicFields.add(createDynamicField("foreignAmount",
					rootNode.path("responseData").path("amountPaid").asText(), "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.TRUE, null));
			
			
		   
		}
		case "18" -> { // Dubai Police Traffic Fines 

			
			
		    // Add trafficFileNo field
		    dynamicFields.add(createDynamicField("trafficFileNo", rootNode.path("responseData").path("resField2").asText(),
		            "TRAFFIC_FILE_NUMBER", TEXT_FIELD_TYPE, Boolean.TRUE, Boolean.FALSE, null));

		    // Add pedestrianFine field
		    dynamicFields.add(createDynamicField("pedestrianFine", rootNode.path("responseData").path("resField3").asText(),
		            "", TEXT_FIELD_TYPE, Boolean.FALSE, Boolean.FALSE, null));

		    // Add ticketsList field
		    ArrayList<ListItem> ticketsList = new ArrayList<>();
		    JsonNode arrayResponse = rootNode.path("responseData").path("arrayResponse");
		    for (int i = 0; i < arrayResponse.size(); i++) {
		        JsonNode ticketNode = arrayResponse.get(i);
		        JsonNode ticketsArray = ticketNode.path("Tickets");

		        for (int j = 0; j < ticketsArray.size(); j++) {
		            JsonNode ticket = ticketsArray.get(j);
		            List<ListItem> ticketAttributes = new ArrayList<>();

		            ticketAttributes.add(createListItem("ticketNo", ticket.path("TicketNo").asText(), "TICKET_NUMBER", TEXT_FIELD_TYPE, Boolean.TRUE));
		            ticketAttributes.add(createListItem("flagPayable", ticket.path("isPayable").asText(), "PAYABLE_FLAG", TEXT_FIELD_TYPE, Boolean.FALSE));
		            ticketAttributes.add(createListItem("ticketId", ticket.path("TicketId").asText(), "", TEXT_FIELD_TYPE, Boolean.TRUE));
		            ticketAttributes.add(createListItem("fineSource", ticketNode.path("FineSource").asText(), "FINE_SOURCE", TEXT_FIELD_TYPE, Boolean.FALSE));
		            ticketAttributes.add(createListItem("ticketDate", ticket.path("ticketDateField").asText(), "TICKET_DATE", TEXT_FIELD_TYPE, Boolean.FALSE));
		            ticketAttributes.add(createListItem("ticketTime", ticket.path("ticketTimeField").asText(), "TICKET_TIME", TEXT_FIELD_TYPE, Boolean.FALSE));
		            ticketAttributes.add(createListItem("ticketDescription", ticket.path("TicketDescription").asText(), "TICKET_DESCRIPTION", TEXT_FIELD_TYPE, Boolean.FALSE));
		            ticketAttributes.add(createListItem("ticketAmount", ticket.path("CalculatedFineAmount").asText(), "PRODUCT_DESCRIPTION", "currency", Boolean.FALSE));
		            ticketAttributes.add(createListItem("locationDescription", ticket.path("locationDescription").asText(), "LOCATION_DESCRIPTION", TEXT_FIELD_TYPE, Boolean.FALSE));
		            ticketAttributes.add(createListItem("penaltyFine", ticket.path("PenaltyFine").asText(), "PENALTY_FINE", TEXT_FIELD_TYPE, Boolean.FALSE));
		            ticketAttributes.add(createListItem("knowledgeFee", ticket.path("KnowledgeFee").asText(), "KNOWLEDGE_FEE", TEXT_FIELD_TYPE, Boolean.FALSE));
		            ticketAttributes.add(createListItem("licenceNo", ticket.path("LicenseNo").asText(), "LICENCE_NUMBER", TEXT_FIELD_TYPE, Boolean.FALSE));
		            ticketAttributes.add(createListItem("plateFrom", ticket.path("PlateFrom").asText(), "PLATE_FROM", TEXT_FIELD_TYPE, Boolean.FALSE));
		            ticketAttributes.add(createListItem("plateCode", ticket.path("PlateCode").asText(), "PLATE_CODE", TEXT_FIELD_TYPE, Boolean.FALSE));
		            ticketAttributes.add(createListItem("plateNo", ticket.path("PlateNo").asText(), "PLATE_NUMBER", TEXT_FIELD_TYPE, Boolean.FALSE));

		            ListItem ticketItem = new ListItem();
		            ticketItem.setRowNumber(j + 1);
		            ticketItem.setIsPayable(ticket.path("isPayable").asText()	); // Assuming "1" means payable
		            ticketItem.setList(ticketAttributes);

		            ticketsList.add(ticketItem);
		        }
		    }

		    dynamicFields.add(createDynamicField("ticketsList", "", "", "table", Boolean.TRUE, Boolean.FALSE, ticketsList));
		   
		    

		}
		default -> logger.warn("Unhandled serviceId: {}", billPaymentRequest.getServiceId());
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
    
}
