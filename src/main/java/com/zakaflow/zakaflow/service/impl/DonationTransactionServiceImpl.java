package com.zakaflow.zakaflow.service.impl;

import com.zakaflow.zakaflow.model.DonationProgram;
import com.zakaflow.zakaflow.model.DonationTransaction;
import com.zakaflow.zakaflow.model.PaymentMethod;
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
    public DonationTransaction create(Long userId, Long programId, BigDecimal amount, PaymentMethod paymentMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        DonationProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program tidak ditemukan"));

        if (program.isCompleted()) {
            throw new IllegalStateException("Program donasi sudah selesai");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Metode pembayaran wajib dipilih");
        }

        DonationTransaction transaction = new DonationTransaction();
        transaction.setUser(user);
        transaction.setProgram(program);
        transaction.setAmount(amount);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setStatus(TransactionStatus.PENDING);

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public DonationTransaction confirmPayment(Long transactionId, Long userId, String paymentReference) {
        DonationTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Akses ditolak");
        }
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaksi sudah diproses");
        }

        if (paymentReference != null && !paymentReference.isBlank()) {
            transaction.setPaymentReference(paymentReference.trim());
        }

        return completeTransaction(transaction);
    }

    @Override
    @Transactional
    public DonationTransaction approvePayment(Long transactionId) {
        DonationTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Hanya transaksi pending yang dapat disetujui");
        }

        return completeTransaction(transaction);
    }

    private DonationTransaction completeTransaction(DonationTransaction transaction) {
        DonationProgram program = transaction.getProgram();
        if (program.isCompleted()) {
            throw new IllegalStateException("Program donasi sudah selesai");
        }

        transaction.setStatus(TransactionStatus.SUCCESS);
        program.setCurrentAmount(program.getCurrentAmount().add(transaction.getAmount()));
        if (program.getCurrentAmount().compareTo(program.getTargetAmount()) >= 0) {
            program.setCompleted(true);
        }
        programRepository.save(program);

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
