package com.sesami.smart_bill_payment_services.exception;

import org.springframework.http.HttpStatus;

public class SmartServiceCommonException extends RuntimeException {

	   
	private static final long serialVersionUID = -868509570817456965L;
	private HttpStatus httpStatus;

    public SmartServiceCommonException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
