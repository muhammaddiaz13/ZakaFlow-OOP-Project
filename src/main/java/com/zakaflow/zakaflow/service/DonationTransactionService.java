package com.zakaflow.zakaflow.service;

import com.zakaflow.zakaflow.model.DonationTransaction;
import com.zakaflow.zakaflow.model.PaymentMethod;
import com.zakaflow.zakaflow.model.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DonationTransactionService {

    List<DonationTransaction> findAll();

    Optional<DonationTransaction> findById(Long id);

    List<DonationTransaction> findByUserId(Long userId);

    DonationTransaction create(Long userId, Long programId, BigDecimal amount, PaymentMethod paymentMethod);

    DonationTransaction confirmPayment(Long transactionId, Long userId, String paymentReference);

    DonationTransaction approvePayment(Long transactionId);

    DonationTransaction updateStatus(Long id, TransactionStatus status);

    void deleteById(Long id);
}
