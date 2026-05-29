package com.zakaflow.zakaflow.controller;

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

    @GetMapping
    public String list(Model model) {
        model.addAttribute("programs", donationProgramService.findAllActive());
        return "programs/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("program", donationProgramService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program tidak ditemukan")));
        model.addAttribute("paymentChannels", PaymentChannel.values());
        model.addAttribute("bankAccounts", paymentMethodService.findActiveByChannel(PaymentChannel.TRANSFER_BANK));
        return "programs/detail";
    }
}
