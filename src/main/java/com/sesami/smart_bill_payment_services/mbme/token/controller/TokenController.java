package com.sesami.smart_bill_payment_services.mbme.token.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sesami.smart_bill_payment_services.mbme.token.entity.TokenDetails;
import com.sesami.smart_bill_payment_services.mbme.token.service.TokenService;

@RestController
@RequestMapping("/mbme/api/")
public record TokenController(TokenService tokenService) {

    /**
     * Requests a token string from the TokenService and returns the valid token.
     *
     * @return the valid token string
     */
    @GetMapping("/token")
    public String requestTokenString() {
        try {
            TokenDetails tokenDetails = tokenService.getValidToken();
            return tokenDetails.getAccessToken();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}