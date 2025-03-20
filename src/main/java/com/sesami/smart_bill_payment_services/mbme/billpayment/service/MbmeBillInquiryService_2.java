package com.sesami.smart_bill_payment_services.mbme.billpayment.service;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.MbmeBillInquiryRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.MbmeBillInquiryResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.entity.BillInquiryEntity;
import com.sesami.smart_bill_payment_services.mbme.billpayment.repository.BillInquiryRepository;
import com.sesami.smart_bill_payment_services.mbme.token.service.TokenService;

@Service
public class MbmeBillInquiryService_2 {

    private static final Logger logger = LoggerFactory.getLogger(MbmeBillInquiryService.class);

    private final RestTemplate restTemplate;
    private final TokenService tokenService;
    private final BillInquiryRepository billInquiryRepository;
    private final ObjectMapper objectMapper;
    

    @Value("${mbme.api.balance-payment.url}")
    private String billPaymentUrl;

    public MbmeBillInquiryService_2(RestTemplate restTemplate, TokenService tokenService,
                                   BillInquiryRepository billInquiryRepository,
                                   ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
        this.billInquiryRepository = billInquiryRepository;
        this.objectMapper = objectMapper;
       // this.billPaymentUrl = billPaymentUrl;
    }

    public BalanceEnquiryResponse processBillInquiry(BalanceEnquiryRequest balanceEnquiryRequest) {
        logger.info("Processing MBME BillInquiry request for transactionId: {}", balanceEnquiryRequest.getTransactionId());

        MbmeBillInquiryRequest req = createMbmeBillInquiryRequest(balanceEnquiryRequest);
        setDynamicRequestFields(req, balanceEnquiryRequest);

        try {
            String jsonRequest = objectMapper.writeValueAsString(req);
            HttpEntity<String> httpRequest = createHttpRequest(jsonRequest);
            LocalDateTime requestTimestamp = LocalDateTime.now();
            ResponseEntity<String> response = restTemplate.exchange(billPaymentUrl, HttpMethod.POST, httpRequest, String.class);

            saveBillInquiry(balanceEnquiryRequest, jsonRequest, response, requestTimestamp);

            return handleResponse(balanceEnquiryRequest, response);
        } catch (Exception e) {
            logger.error("Failed to process MBME BillInquiry request.", e);
            throw new RuntimeException("Failed to process MBME BillInquiry request");
        }
    }

    private MbmeBillInquiryRequest createMbmeBillInquiryRequest(BalanceEnquiryRequest balanceEnquiryRequest) {
        MbmeBillInquiryRequest req = new MbmeBillInquiryRequest();
        req.setTransactionId(balanceEnquiryRequest.getTransactionId());
        req.setMerchantId(balanceEnquiryRequest.getMerchantId());
        req.setMerchantLocation(balanceEnquiryRequest.getMerchantLocation());
        req.setServiceId(balanceEnquiryRequest.getServiceCode());
        req.setMethod(balanceEnquiryRequest.getServiceType());
        req.setLang("en");
        return req;
    }

    private void setDynamicRequestFields(MbmeBillInquiryRequest req, BalanceEnquiryRequest balanceEnquiryRequest) {
        if (Objects.nonNull(req.getServiceId()) && req.getServiceId().equalsIgnoreCase("103")) {
            if (balanceEnquiryRequest.getDynamicRequestFields() != null && !balanceEnquiryRequest.getDynamicRequestFields().isEmpty()) {
                req.setReqField1(balanceEnquiryRequest.getDynamicRequestFields().get(0).getValue());
                if (balanceEnquiryRequest.getDynamicRequestFields().size() > 1) {
                   // req.setReqField2(balanceEnquiryRequest.getDynamicRequestFields().get(1).getValue());
                }
            }
        }
    }

    private HttpEntity<String> createHttpRequest(String jsonRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
			headers.set("Authorization", "Bearer " + tokenService.getValidToken().getAccessToken());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return new HttpEntity<>(jsonRequest, headers);
    }

    private void saveBillInquiry(BalanceEnquiryRequest balanceEnquiryRequest, String jsonRequest, ResponseEntity<String> response, LocalDateTime requestTimestamp) {
        BillInquiryEntity billInquiry = new BillInquiryEntity();
        billInquiry.setTransactionId(balanceEnquiryRequest.getTransactionId());
        billInquiry.setMerchantId(balanceEnquiryRequest.getMerchantId());
        billInquiry.setMerchantLocation(balanceEnquiryRequest.getMerchantLocation());
        billInquiry.setRequestJson(jsonRequest);
        billInquiry.setResponseJson(response.getBody());
        billInquiry.setTimestamp(requestTimestamp);
        billInquiryRepository.save(billInquiry);
    }

    private BalanceEnquiryResponse handleResponse(BalanceEnquiryRequest balanceEnquiryRequest, ResponseEntity<String> response) throws com.fasterxml.jackson.core.JsonProcessingException {
        BalanceEnquiryResponse balanceEnquiryResponse = new BalanceEnquiryResponse();
        balanceEnquiryResponse.setTransactionId(balanceEnquiryRequest.getTransactionId());
        balanceEnquiryResponse.setCurrencyCode("AED");

        if (response.getStatusCode() == HttpStatus.OK) {
            MbmeBillInquiryResponse mbmeBillInquiryResponse = objectMapper.readValue(response.getBody(), MbmeBillInquiryResponse.class);
            balanceEnquiryResponse.setResponseCode(mbmeBillInquiryResponse.getResponseCode());
            balanceEnquiryResponse.setResponseStatus(mbmeBillInquiryResponse.getStatus());
            balanceEnquiryResponse.setResponseMessage(mbmeBillInquiryResponse.getResponseMessage());
            balanceEnquiryResponse.setInternalWebServiceCode("000");
            balanceEnquiryResponse.setInternalWebServiceDesc("SUCCESS");
            balanceEnquiryResponse.setDueAmount(mbmeBillInquiryResponse.getResponseData().getAmount());
            return balanceEnquiryResponse;
        } else {
            balanceEnquiryResponse.setResponseCode(response.getStatusCode().toString());
            balanceEnquiryResponse.setResponseStatus("FAILED");
            balanceEnquiryResponse.setResponseMessage("FAILED");
            balanceEnquiryResponse.setInternalWebServiceCode("444");
            balanceEnquiryResponse.setInternalWebServiceDesc("FAILED");
            balanceEnquiryResponse.setDueAmount("0.00");
            return balanceEnquiryResponse;
        }
    }
}