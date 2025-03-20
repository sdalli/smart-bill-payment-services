package com.sesami.smart_bill_payment_services.mbme.token.service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sesami.smart_bill_payment_services.mbme.token.entity.TokenDetails;
@Repository
public interface TokenDetailsRepository extends JpaRepository<TokenDetails, Long> {
    TokenDetails findTopByOrderByIdDesc();
}
