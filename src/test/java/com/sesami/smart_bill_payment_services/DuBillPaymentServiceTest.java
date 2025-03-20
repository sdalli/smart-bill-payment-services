package com.sesami.smart_bill_payment_services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillPaymentRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.repository.BillPaymentActivityRepository;
import com.sesami.smart_bill_payment_services.mbme.billpayment.repository.BillPaymentRepository;
import com.sesami.smart_bill_payment_services.mbme.billpayment.service.MbmeBillPaymentService;
import com.sesami.smart_bill_payment_services.mbme.token.entity.BillPayment;
import com.sesami.smart_bill_payment_services.mbme.token.entity.BillPaymentActivity;

public class DuBillPaymentServiceTest {

    @InjectMocks
    private MbmeBillPaymentService duBillPaymentService;

    @Mock
    private BillPaymentRepository duBillPaymentRepository;

    @Mock
    private BillPaymentActivityRepository billPaymentActivityRepository;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessDuBillPayment() throws IOException {
        BillPaymentRequest request = new BillPaymentRequest();
        request.setTransactionId("20010****0012340066");
        request.setMerchantId("66");
        request.setMerchantLocation("DU POSTPAID HQ");
        request.setServiceCode("103");
        request.setMethod("balance");
        request.setLanguage("en");
        //request.setReqField1("0551234567");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpRequest = new HttpEntity<>(new ObjectMapper().writeValueAsString(request), headers);

        String responseBody = "{\"responseCode\":\"000\",\"status\":\"SUCCESS\",\"responseMessage\":\"Success\",\"responseData\":{\"accountNumber\":\"5233996576\",\"amount\":\"408.35\",\"custName\":\"\",\"resField1\":\"200101120012340066\",\"resField2\":\"ck48qka5ggygyhlfcujwxs1y4sj\"}}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), eq(httpRequest), eq(String.class))).thenReturn(responseEntity);

        duBillPaymentService.processMbmeBillPayment(request);

        verify(duBillPaymentRepository, times(1)).save(any(BillPayment.class));
        verify(billPaymentActivityRepository, times(1)).save(any(BillPaymentActivity.class));
    }
}