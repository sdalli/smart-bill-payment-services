package com.sesami.smart_bill_payment_services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication

//@EnableJpaRepositories(basePackages = "com.sesami.smart_bill_payment_services.mbme.billpayment.repository")
//@EnableJpaRepositories(basePackages = "com.sesami.smart_bill_payment_services.mbme.token.service.repository")
@EnableJpaRepositories(basePackages = {"com.sesami.smart_bill_payment_services.mbme.token.service.repository", 
		"com.sesami.smart_bill_payment_services.mbme.billpayment.repository"})
//@EntityScan(basePackages = "com.sesami.smart_bill_payment_services.mbme.billpayment.entity")
@EntityScan(basePackages = {"com.sesami.smart_bill_payment_services.mbme.token.entity",
"com.sesami.smart_bill_payment_services.mbme.billpayment.entity"})
public class SmartBillPaymentServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartBillPaymentServicesApplication.class, args);
	}

}
