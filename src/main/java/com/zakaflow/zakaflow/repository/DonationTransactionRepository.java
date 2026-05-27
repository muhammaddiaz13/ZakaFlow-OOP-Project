package com.zakaflow.zakaflow.repository;

import com.zakaflow.zakaflow.model.DonationTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationTransactionRepository extends JpaRepository<DonationTransaction, Long> {

    List<DonationTransaction> findByUser_Id(Long userId);
}
