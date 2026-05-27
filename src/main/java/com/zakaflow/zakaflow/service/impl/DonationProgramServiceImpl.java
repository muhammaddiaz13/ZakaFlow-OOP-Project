package com.zakaflow.zakaflow.service.impl;

import com.zakaflow.zakaflow.model.DonationProgram;
import com.zakaflow.zakaflow.repository.DonationProgramRepository;
import com.zakaflow.zakaflow.service.DonationProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonationProgramServiceImpl implements DonationProgramService {

    private final DonationProgramRepository donationProgramRepository;

    @Override
    public List<DonationProgram> findAll() {
        return donationProgramRepository.findAll();
    }

    @Override
    public List<DonationProgram> findAllActive() {
        return donationProgramRepository.findByIsCompletedFalse();
    }

    @Override
    public Optional<DonationProgram> findById(Long id) {
        return donationProgramRepository.findById(id);
    }

    @Override
    @Transactional
    public DonationProgram save(DonationProgram program) {
        return donationProgramRepository.save(program);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        donationProgramRepository.deleteById(id);
    }
}
