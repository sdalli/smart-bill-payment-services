package com.sesami.smart_bill_payment_services.mbme.billpayment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sesami.smart_bill_payment_services.mbme.token.entity.BillPayment;
@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {
}
