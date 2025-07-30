package com.sesami.smart_bill_payment_services;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillPaymentRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillPaymentResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.service.MbmeBillPaymentService_V2;
import com.sesami.smart_bill_payment_services.mbme.token.service.TokenService;

class MbmeBillPaymentService_V2Test {

    @InjectMocks
    private MbmeBillPaymentService_V2 mbmeBillPaymentService;

    @Mock
    private TokenService tokenService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessBillPayment_Success() throws IOException {
        // Arrange
        BillPaymentRequest request = new BillPaymentRequest();
        request.setDeviceTransactionId("12345");
        request.setServiceId("103");
        request.setExternalTransactionId("ext-123");

        String mockToken = "mock-token";
        when(tokenService.getValidToken().getAccessToken()).thenReturn(mockToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mockToken);

        String mockJsonRequest = """
        		{
  "category": "SmartServices",
  "deviceTransactionId": "2001011200123407425",
  "externalTransactionId": "2001011200123407425",
  "deviceId": "12345",
  "serviceCode": "DuBillPayment",
  "serviceId":"103",
  "serviceType": "balance",
  "merchantId": "3668",
  "merchantLocation": "Transguard",
  "language": "en",
  "paymentMode":"CH",
  "dynamicRequestFields": [
    {
      "name": "accountNumber",
      "value": "0551234567"
    }
  ]
}""";

		when(objectMapper.writeValueAsString(request)).thenReturn(mockJsonRequest);
        String mockResponseBody = "{\"responseCode\":\"200\",\"status\":\"SUCCESS\",\"responseMessage\":\"Payment successful\",\"responseData\":{\"amountPaid\":\"100.00\"}}";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockResponseBody, HttpStatus.OK));

        // Act
        BillPaymentResponse response = mbmeBillPaymentService.processBillPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals("12345", response.getDeviceTransactionId());
        assertEquals("ext-123", response.getExternalTransactionId());
        assertEquals("200", response.getResponseCode());
        assertEquals("SUCCESS", response.getResponseStatus());
        assertEquals("Payment successful", response.getResponseMessage());
    }

    @Test
    void testProcessBillPayment_Failure() throws IOException {
        // Arrange
        BillPaymentRequest request = new BillPaymentRequest();
        request.setDeviceTransactionId("12345");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> mbmeBillPaymentService.processBillPayment(request));
    }

    @Test
    void testCreateHeaders() throws IOException {
        // Arrange
        String mockToken = "mock-token";
        when(tokenService.getValidToken().getAccessToken()).thenReturn(mockToken);

        // Act
        HttpHeaders headers = mbmeBillPaymentService.createHeaders();

        // Assert
        assertNotNull(headers);
        assertEquals("Bearer mock-token", headers.getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals("application/json", headers.getContentType().toString());
    }

    @Test
    void testGenerateDynamicResponseFields() throws IOException {
        // Arrange
        BillPaymentRequest request = new BillPaymentRequest();
        request.setServiceId("103");
        request.setExternalTransactionId("ext-123");

        String mockResponseBody = "{\"responseData\":{\"amountPaid\":\"100.00\"}}";
        var rootNode = new ObjectMapper().readTree(mockResponseBody);

        // Act
        List<?> dynamicFields = mbmeBillPaymentService.generateDynamicResponseFields(request, rootNode);

        // Assert
        assertNotNull(dynamicFields);
        assertFalse(dynamicFields.isEmpty());
    }
}
