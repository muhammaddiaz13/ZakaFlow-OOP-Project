package com.zakaflow.zakaflow.service.impl;

import com.zakaflow.zakaflow.model.PaymentChannel;
import com.zakaflow.zakaflow.model.PaymentMethod;
import com.zakaflow.zakaflow.repository.PaymentMethodRepository;
import com.zakaflow.zakaflow.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public List<PaymentMethod> findAllActive() {
        return paymentMethodRepository.findByActiveTrueOrderByNameAsc();
    }

    @Override
    public List<PaymentMethod> findActiveByChannel(PaymentChannel channel) {
        return paymentMethodRepository.findByChannelAndActiveTrueOrderByNameAsc(channel);
    }

    @Override
    public Optional<PaymentMethod> findById(Long id) {
        return paymentMethodRepository.findById(id);
    }

    @Override
    @Transactional
    public PaymentMethod save(PaymentMethod paymentMethod) {
        return paymentMethodRepository.save(paymentMethod);
    }
}
