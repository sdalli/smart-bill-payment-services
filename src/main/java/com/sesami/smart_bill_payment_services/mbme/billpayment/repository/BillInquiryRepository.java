package com.sesami.smart_bill_payment_services.mbme.billpayment.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sesami.smart_bill_payment_services.mbme.billpayment.entity.BillInquiryEntity;
@Repository
public interface BillInquiryRepository extends JpaRepository<BillInquiryEntity, Long> {
}
