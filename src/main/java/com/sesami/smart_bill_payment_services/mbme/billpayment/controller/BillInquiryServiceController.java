package com.sesami.smart_bill_payment_services.mbme.billpayment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sesami.smart_bill_payment_services.common.util.SmartServiceCommonException;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryResponse;
import com.sesami.smart_bill_payment_services.mbme.billpayment.service.MbmeBillInquiryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/bill-inquiry/api/")
@Tag(name = "Bill Inquiry Service", description = "API for Bill Inquiry Service")
public class BillInquiryServiceController {

    @Autowired
    private MbmeBillInquiryService billInquiryService;

    

//    @PostMapping("/mbme-bill-inquiry")
//    @Operation(summary = "Process Bill Inquiry", description = "Processes a bill inquiry request and returns the response.")
//    public BalanceEnquiryResponse processBillInquiryService(@RequestBody BalanceEnquiryRequest balanceEnquiryRequest) throws IOException {
//    	BalanceEnquiryResponse balanceEnquiryResponse =billInquiryService.processMbmeBillInquiryService(balanceEnquiryRequest);
//    	
//    	return balanceEnquiryResponse;
//    }
    
  @PostMapping("/mbme-bill-inquiry")
  @Operation(summary = "Process Bill Inquiry", description = "Processes a bill inquiry request and returns the response.")
    public ResponseEntity<BalanceEnquiryResponse> processBillInquiryService(@RequestBody BalanceEnquiryRequest balanceEnquiryRequest) {
        try {
            BalanceEnquiryResponse balanceEnquiryResponse = billInquiryService.processMbmeBillInquiryService(balanceEnquiryRequest);
            return ResponseEntity.ok(balanceEnquiryResponse);
        } catch (SmartServiceCommonException e) {
            // Log the custom exception here
            return ResponseEntity.status(e.getHttpStatus()).body(null); // Return the custom error response
        } catch (Exception e) {
            // Log other exceptions here
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Generic error response
        }
    }
    
    
}
