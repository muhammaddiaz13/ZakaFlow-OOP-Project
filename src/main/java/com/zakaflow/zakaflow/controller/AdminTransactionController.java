package com.zakaflow.zakaflow.controller;

import com.zakaflow.zakaflow.service.DonationTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;

@Controller
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final DonationTransactionService donationTransactionService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("transactions", donationTransactionService.findAll().stream()
                .sorted(Comparator.comparing(t -> t.getTransactionDate(), Comparator.reverseOrder()))
                .toList());
        model.addAttribute("activePage", "transactions");
        return "admin/transactions/list";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            donationTransactionService.approvePayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pembayaran disetujui.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/transactions";
    }
}
