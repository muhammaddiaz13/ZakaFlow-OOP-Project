package com.zakaflow.zakaflow.service;

import com.zakaflow.zakaflow.model.PaymentChannel;
import com.zakaflow.zakaflow.model.PaymentMethod;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodService {

    List<PaymentMethod> findAllActive();

    List<PaymentMethod> findActiveByChannel(PaymentChannel channel);

    Optional<PaymentMethod> findById(Long id);

    PaymentMethod save(PaymentMethod paymentMethod);
}
