package com.zakaflow.zakaflow.service.dto;

import java.math.BigDecimal;

/**
 * Hasil perhitungan zakat penghasilan.
 *
 * @param status      "wajib zakat" atau "tidak wajib"
 * @param jumlahZakat jumlah zakat yang perlu dibayar (dalam Rupiah)
 */
public record ZakatCalculationResult(String status, BigDecimal jumlahZakat) {
}

