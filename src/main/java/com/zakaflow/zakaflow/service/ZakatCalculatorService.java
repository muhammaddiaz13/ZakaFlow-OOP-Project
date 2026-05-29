package com.zakaflow.zakaflow.service;

import com.zakaflow.zakaflow.config.ZakatProperties;
import com.zakaflow.zakaflow.service.dto.ZakatCalculationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ZakatCalculatorService {

    private static final String STATUS_WAJIB = "wajib zakat";
    private static final String STATUS_TIDAK_WAJIB = "tidak wajib";

    private final ZakatProperties zakatProperties;

    /**
     * Menghitung zakat penghasilan berdasarkan gaji bulanan dan bonus (opsional).
     *
     * Ketentuan:
     * - Jika (gajiBulanan + bonus) >= nisabBulanan => zakat = tarif * total
     * - Jika < nisabBulanan => zakat = 0
     *
     * Catatan:
     * - Nilai negatif dianggap tidak valid dan akan ditolak.
     * - Pembulatan Rupiah dilakukan ke bilangan bulat (0 desimal) dengan HALF_UP.
     */
    public ZakatCalculationResult hitungZakatPenghasilan(BigDecimal gajiBulanan, BigDecimal bonus) {
        BigDecimal gaji = requireNonNegative(gajiBulanan, "gajiBulanan");
        BigDecimal tambahan = bonus == null ? BigDecimal.ZERO : requireNonNegative(bonus, "bonus");

        BigDecimal total = gaji.add(tambahan);
        BigDecimal nisab = safePositive(zakatProperties.getNisabBulanan(), new BigDecimal("6850000"));
        BigDecimal tarif = safePositive(zakatProperties.getTarif(), new BigDecimal("0.025"));

        if (total.compareTo(nisab) < 0) {
            return new ZakatCalculationResult(STATUS_TIDAK_WAJIB, BigDecimal.ZERO);
        }

        BigDecimal zakat = total.multiply(tarif).setScale(0, RoundingMode.HALF_UP);
        return new ZakatCalculationResult(STATUS_WAJIB, zakat);
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " tidak boleh null");
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " tidak boleh negatif");
        }
        return value;
    }

    private static BigDecimal safePositive(BigDecimal value, BigDecimal fallback) {
        if (value == null || value.signum() <= 0) {
            return fallback;
        }
        return value;
    }
}

