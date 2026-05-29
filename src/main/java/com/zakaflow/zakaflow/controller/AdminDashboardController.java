package com.zakaflow.zakaflow.controller;

import com.zakaflow.zakaflow.model.DonationTransaction;
import com.zakaflow.zakaflow.model.TransactionStatus;
import com.zakaflow.zakaflow.service.CategoryService;
import com.zakaflow.zakaflow.service.DonationProgramService;
import com.zakaflow.zakaflow.service.DonationTransactionService;
import com.zakaflow.zakaflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final DonationProgramService donationProgramService;
    private final DonationTransactionService donationTransactionService;

    @GetMapping
    public String dashboard(Model model) {
        List<DonationTransaction> transactions = donationTransactionService.findAll();

        BigDecimal totalCollected = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .map(DonationTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activePrograms = donationProgramService.findAll().stream()
                .filter(p -> !p.isCompleted())
                .count();

        List<DonationTransaction> recent = transactions.stream()
                .sorted(Comparator.comparing(DonationTransaction::getTransactionDate).reversed())
                .limit(8)
                .toList();

        model.addAttribute("userCount", userService.findAll().size());
        model.addAttribute("categoryCount", categoryService.findAll().size());
        model.addAttribute("programCount", donationProgramService.findAll().size());
        model.addAttribute("activeProgramCount", activePrograms);
        model.addAttribute("transactionCount", transactions.size());
        model.addAttribute("totalCollected", totalCollected);
        model.addAttribute("recentTransactions", recent);
        return "admin/dashboard";
    }

    @PostMapping("/reset-donations")
    public String resetDonations(RedirectAttributes redirectAttributes) {
        donationTransactionService.resetAllDonations();
        redirectAttributes.addFlashAttribute("successMessage",
                "Semua transaksi donasi telah dihapus dan total terkumpul program direset ke Rp 0.");
        return "redirect:/admin";
    }
}
