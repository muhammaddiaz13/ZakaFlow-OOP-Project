package com.zakaflow.zakaflow.repository;

import com.zakaflow.zakaflow.model.DonationProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationProgramRepository extends JpaRepository<DonationProgram, Long> {

    List<DonationProgram> findByIsCompletedFalse();
}
