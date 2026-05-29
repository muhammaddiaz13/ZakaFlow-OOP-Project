package com.zakaflow.zakaflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "zakaflow.zakat")
public class ZakatProperties {

    /**
     * Nisab zakat penghasilan untuk periode bulanan (dalam Rupiah).
     * Default: Rp 6.850.000 (bisa diubah dari application.properties).
     */
    private BigDecimal nisabBulanan = new BigDecimal("6850000");

    /**
     * Tarif zakat penghasilan.
     * Default: 2.5% (0.025).
     */
    private BigDecimal tarif = new BigDecimal("0.025");
}

