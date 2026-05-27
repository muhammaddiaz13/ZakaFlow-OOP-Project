package com.zakaflow.zakaflow.service;

import com.zakaflow.zakaflow.model.DonationProgram;

import java.util.List;
import java.util.Optional;

public interface DonationProgramService {

    List<DonationProgram> findAll();

    List<DonationProgram> findAllActive();

    Optional<DonationProgram> findById(Long id);

    DonationProgram save(DonationProgram program);

    void deleteById(Long id);
}
