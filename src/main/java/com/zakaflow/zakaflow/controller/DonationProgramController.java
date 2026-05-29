package com.zakaflow.zakaflow.controller;

import com.zakaflow.zakaflow.config.ZakatProperties;
import com.zakaflow.zakaflow.model.PaymentChannel;
import com.zakaflow.zakaflow.service.DonationProgramService;
import com.zakaflow.zakaflow.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/programs")
@RequiredArgsConstructor
public class DonationProgramController {

    private final DonationProgramService donationProgramService;
    private final PaymentMethodService paymentMethodService;
    private final ZakatProperties zakatProperties;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("programs", donationProgramService.findAllActive());
        return "programs/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var program = donationProgramService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program tidak ditemukan"));
        model.addAttribute("program", program);

        // transaction count
        int txCount = program.getTransactions() != null ? program.getTransactions().size() : 0;
        model.addAttribute("transactionCount", txCount);

        // prepare chart data for last 6 months
        java.time.YearMonth now = java.time.YearMonth.now();
        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<Long> amounts = new java.util.ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth ym = now.minusMonths(i);
            labels.add(ym.getMonth().name().substring(0, 3) + ' ' + ym.getYear());
            final java.time.YearMonth target = ym;
            java.math.BigDecimal sumBd = program.getTransactions().stream()
                .filter(t -> t.getTransactionDate() != null && java.time.YearMonth.from(t.getTransactionDate().toLocalDate()).equals(target))
                .map(t -> t.getAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            long sum = sumBd.longValue();
            amounts.add(sum);
        }
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartAmounts", amounts);
        long maxAmount = amounts.stream().mapToLong(Long::longValue).max().orElse(0L);
        long chartTarget = program.getTargetAmount() != null ? program.getTargetAmount().longValue() : 0L;
        long chartMax = Math.max(maxAmount, chartTarget);
        long headroom = (long) Math.ceil(chartMax * 0.1);
        chartMax = chartMax + headroom;
        model.addAttribute("chartTarget", chartTarget);
        model.addAttribute("chartMax", chartMax);

        model.addAttribute("paymentChannels", PaymentChannel.values());
        model.addAttribute("bankAccounts", paymentMethodService.findActiveByChannel(PaymentChannel.TRANSFER_BANK));
        model.addAttribute("paymentMethods", paymentMethodService.findAllActive());
        model.addAttribute("zakatNisabBulanan", zakatProperties.getNisabBulanan());
        model.addAttribute("zakatTarif", zakatProperties.getTarif());

        return "programs/detail";
    }
}