package com.zakaflow.zakaflow.repository;

import com.zakaflow.zakaflow.model.PaymentChannel;
import com.zakaflow.zakaflow.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByActiveTrueOrderByNameAsc();

    List<PaymentMethod> findByChannelAndActiveTrueOrderByNameAsc(PaymentChannel channel);
}
