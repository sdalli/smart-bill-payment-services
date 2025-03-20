package com.sesami.smart_bill_payment_services.mbme.token.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sesami.smart_bill_payment_services.mbme.token.entity.TokenActivity;

public interface TokenActivityRepository extends JpaRepository<TokenActivity, Long> {
}