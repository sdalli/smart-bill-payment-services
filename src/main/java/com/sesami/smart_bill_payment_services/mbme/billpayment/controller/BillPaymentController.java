package com.sesami.smart_bill_payment_services.mbme.billpayment.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sesami.smart_bill_payment_services.common.util.SmartServiceCommonException;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillPaymentRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillPaymentResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.service.MbmeBillPaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/bill-payment/api/")
@Tag(name = "Bill Payment Service", description = "API for Bill Payment Service")
public class BillPaymentController {

	
	

    @Autowired
    private MbmeBillPaymentService billPaymentService;
	
	
//	@PostMapping("/mbme-bill-payment")
//    @Operation(summary = "Process Bill Payment", description = "Processes a bill payment request and returns the response.")
//    public BillPaymentResponse processBillPayment(@RequestBody BillPaymentRequest billPaymentRequest) throws IOException {
//		BillPaymentResponse billPaymentResponse =   billPaymentService.processMbmeBillPayment(billPaymentRequest);
//         
//         return billPaymentResponse;
//    }
    
    
    
    @PostMapping("/mbme-bill-payment")
    @Operation(summary = "Process Bill Payment", description = "Processes a bill payment request and returns the response.")
    public ResponseEntity<BillPaymentResponse> processBillPayment(@RequestBody BillPaymentRequest billPaymentRequest) throws IOException {
        try {
            BillPaymentResponse billPaymentResponse = billPaymentService.processMbmeBillPayment(billPaymentRequest);
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
