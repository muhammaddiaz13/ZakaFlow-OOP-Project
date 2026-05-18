package com.zakaflow.zakaflow.service.impl;

import com.zakaflow.zakaflow.model.DonationProgram;
import com.zakaflow.zakaflow.model.DonationTransaction;
import com.zakaflow.zakaflow.model.TransactionStatus;
import com.zakaflow.zakaflow.model.User;
import com.zakaflow.zakaflow.repository.DonationProgramRepository;
import com.zakaflow.zakaflow.repository.DonationTransactionRepository;
import com.zakaflow.zakaflow.repository.UserRepository;
import com.zakaflow.zakaflow.service.DonationTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonationTransactionServiceImpl implements DonationTransactionService {

    private final DonationTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final DonationProgramRepository programRepository;

    @Override
    public List<DonationTransaction> findAll() {
        return transactionRepository.findAll();
    }

    @Override
    public Optional<DonationTransaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    @Override
    public List<DonationTransaction> findByUserId(Long userId) {
        return transactionRepository.findByUser_Id(userId);
    }

    @Override
    @Transactional
    public DonationTransaction create(Long userId, Long programId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        DonationProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program tidak ditemukan"));

        if (program.isCompleted()) {
            throw new IllegalStateException("Program donasi sudah selesai");
        }

        DonationTransaction transaction = new DonationTransaction();
        transaction.setUser(user);
        transaction.setProgram(program);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);

        program.setCurrentAmount(program.getCurrentAmount().add(amount));
        if (program.getCurrentAmount().compareTo(program.getTargetAmount()) >= 0) {
            program.setCompleted(true);
        }

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public DonationTransaction updateStatus(Long id, TransactionStatus status) {
        DonationTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan"));
        transaction.setStatus(status);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        transactionRepository.deleteById(id);
    }
}
