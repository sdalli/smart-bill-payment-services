package com.sesami.smart_bill_payment_services.mbme.billpayment.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sesami.smart_bill_payment_services.common.util.SmartServiceCommonException;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillPaymentRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillPaymentResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.service.MbmeBillInquiryService;
import com.sesami.smart_bill_payment_services.mbme.billpayment.service.MbmeBillPaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/mbme-bill-service/api/")
@Tag(name = "MBME Billing Service", description = "API for Bill Inquiry Service")
public class MbmeBillServiceController {

    @Autowired
    private MbmeBillInquiryService billInquiryService;

    @Autowired
    private MbmeBillPaymentService billPaymentService;
    
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello";
    }
    
  @PostMapping("/bill-inquiry")
  @Operation(summary = "Process Bill Inquiry", description = "Processes a bill inquiry request and returns the response.")
    public ResponseEntity<BalanceEnquiryResponse> processBillInquiry(@RequestBody BalanceEnquiryRequest balanceEnquiryRequest) {
        try {
            BalanceEnquiryResponse balanceEnquiryResponse = billInquiryService.processBillInquiry(balanceEnquiryRequest);
            return ResponseEntity.ok(balanceEnquiryResponse);
        } catch (SmartServiceCommonException e) {
            // Log the custom exception here
            return ResponseEntity.status(e.getHttpStatus()).body(null); // Return the custom error response
        } catch (Exception e) {
            // Log other exceptions here
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Generic error response
        }
    }
    
  
  @PostMapping("/bill-payment")
  @Operation(summary = "Process Bill Payment", description = "Processes a bill payment request and returns the response.")
  public ResponseEntity<BillPaymentResponse> processBillPayment(@RequestBody BillPaymentRequest billPaymentRequest) throws IOException {
      try {
          BillPaymentResponse billPaymentResponse = billPaymentService.processBillPayment(billPaymentRequest);
          return ResponseEntity.ok(billPaymentResponse);
      } catch (SmartServiceCommonException e) {
          // Log the custom exception here
          return ResponseEntity.status(e.getHttpStatus()).body(null); // Return the custom error response
      } catch (Exception e) {
          // Log other exceptions here
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Generic error response
      }
  }
    
}
