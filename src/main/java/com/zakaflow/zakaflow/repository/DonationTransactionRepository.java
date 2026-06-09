package com.zakaflow.zakaflow.repository;

import com.zakaflow.zakaflow.model.DonationTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonationTransactionRepository extends JpaRepository<DonationTransaction, Long> {

    @Override
    @EntityGraph(attributePaths = {"program", "user", "paymentMethod"})
    Optional<DonationTransaction> findById(Long id);

    @EntityGraph(attributePaths = {"program", "user", "paymentMethod"})
    List<DonationTransaction> findByUser_Id(Long userId);

    @Override
    @EntityGraph(attributePaths = {"program", "user", "paymentMethod"})
    List<DonationTransaction> findAll();
}
