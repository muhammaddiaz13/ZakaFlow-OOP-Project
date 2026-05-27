package com.zakaflow.zakaflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/transactions")
public class DonationTransactionController {

    @GetMapping
    public String history() {
        return "redirect:/admin/transactions";
    }
}