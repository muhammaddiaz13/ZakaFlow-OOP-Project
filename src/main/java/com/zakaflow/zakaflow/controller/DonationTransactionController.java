package com.zakaflow.zakaflow.controller;

import com.zakaflow.zakaflow.service.DonationTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class DonationTransactionController {

    private final DonationTransactionService donationTransactionService;

    @GetMapping
    public String history(Model model) {
        model.addAttribute("transactions", donationTransactionService.findAll());
        return "transactions/history";
    }
}
