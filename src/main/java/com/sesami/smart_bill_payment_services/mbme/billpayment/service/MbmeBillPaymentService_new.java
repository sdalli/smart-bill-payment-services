package com.sesami.smart_bill_payment_services.mbme.billpayment.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class MbmeBillPaymentService_new {


    private static final Logger logger = LoggerFactory.getLogger(MbmeBillPaymentService.class);

//	@Autowired
//  private BillPaymentRepository billPaymentRepository;

	@Autowired
	private TokenService tokenService;
	   
	   
	@Autowired
	private RestTemplate restTemplate;

	@Value("${mbme.api.balance-payment.url}")
	private String billPaymentUrl;

    private ObjectMapper objectMapper =null;

	
	public MbmeBillPaymentService_new(ObjectMapper objectMapper) {
        this.objectMapper = new ObjectMapper();
    }

    public BillPaymentResponse processBillPayment(BillPaymentRequest billPaymentRequest) throws IOException {
        logger.info("Processing processMbmeBillPayment request for transactionId: {}", billPaymentRequest.getDeviceTransactionId());

        BillPaymentResponse billPaymentResponse = new BillPaymentResponse();
        MbmeBillPaymentRequest mbmeBillPaymentService = prepareMbmeBillPaymentRequest(billPaymentRequest);

        String requestJson = createRequestJson(billPaymentRequest);
        HttpHeaders headers = createHeaders();

        HttpEntity<String> httpRequest = new HttpEntity<>(requestJson, headers);

        logger.info("Sending MBME Bill payment service request: {}", requestJson);

        ResponseEntity<String> response = restTemplate.exchange(billPaymentUrl, HttpMethod.POST, httpRequest, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            logger.info("Received MBME Bill payment service response: {}", response.getBody());
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            billPaymentResponse.setDeviceTransactionId(billPaymentRequest.getDeviceTransactionId());
            billPaymentResponse.setExternalTransactionId(billPaymentRequest.getExternalTransactionId());
            billPaymentResponse.setResponseCode(getSafeText(rootNode, "responseCode"));
            billPaymentResponse.setResponseStatus(getSafeText(rootNode, "status"));
            billPaymentResponse.setResponseMessage(getSafeText(rootNode, "responseMessage"));
            billPaymentResponse.setStatus(billPaymentResponse.getResponseMessage());

            String serviceId = mbmeBillPaymentService.getServiceId();
            if (serviceId != null) {
                billPaymentResponse.setDynamicResponseFields(prepareDynamicResponseFields(rootNode, serviceId));
            }
        } else {
            logger.error("Failed to process MBME Bill Payment request.");
            throw new RuntimeException("Failed to process MBME Bill Payment request");
        }

        return billPaymentResponse;
    }

    private MbmeBillPaymentRequest prepareMbmeBillPaymentRequest(BillPaymentRequest billPaymentRequest) {
        MbmeBillPaymentRequest request = new MbmeBillPaymentRequest();
        request.setTransactionId(billPaymentRequest.getExternalTransactionId());
        request.setMerchantId(billPaymentRequest.getMerchantId());
        request.setMerchantLocation(billPaymentRequest.getMerchantLocation());
        request.setMethod(billPaymentRequest.getMethod());
        request.setServiceId(billPaymentRequest.getServiceId());
        request.setPaymentMode(billPaymentRequest.getPaymentMode());
        request.setPaidAmount(billPaymentRequest.getPaidAmount());
        request.setLang("en");

        List<DynamicRequestField> fields = billPaymentRequest.getDynamicRequestFields();
        if (fields != null && !fields.isEmpty()) {
            request.setReqField1(fields.get(0).getValue());
            request.setReqField2(fields.get(0).getName());

            if ("19".equalsIgnoreCase(request.getServiceId()) && fields.size() >= 4) {
                request.setReqField1(fields.get(0).getValue());
                request.setReqField2(fields.get(1).getValue());
                request.setReqField3(fields.get(2).getValue());
                request.setReqField4(fields.get(3).getValue());
            }
        }

        return request;
    }

    private HttpHeaders createHeaders() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenService.getValidToken().getAccessToken());
        return headers;
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

    private String createRequestJson(BillPaymentRequest billPaymentRequest) throws IOException {
        return objectMapper.writeValueAsString(billPaymentRequest);
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
