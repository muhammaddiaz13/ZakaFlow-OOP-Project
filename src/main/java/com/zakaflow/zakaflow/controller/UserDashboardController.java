package com.zakaflow.zakaflow.controller;

import com.zakaflow.zakaflow.config.PaymentProperties;
import com.zakaflow.zakaflow.model.DonationTransaction;
import com.zakaflow.zakaflow.model.PaymentMethod;
import com.zakaflow.zakaflow.model.TransactionStatus;
import com.zakaflow.zakaflow.model.User;
import com.zakaflow.zakaflow.service.DonationProgramService;
import com.zakaflow.zakaflow.service.DonationTransactionService;
import com.zakaflow.zakaflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserDashboardController {

    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("id", "ID"));

    private final UserService userService;
    private final DonationTransactionService donationTransactionService;
    private final DonationProgramService donationProgramService;
    private final PaymentProperties paymentProperties;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = resolveUser(principal);
        List<DonationTransaction> transactions = donationTransactionService.findByUserId(user.getId());

        BigDecimal totalDonated = sumSuccessful(transactions);
        List<DonationTransaction> recent = sortByDateDesc(transactions).stream().limit(5).toList();

        addChartAttributes(transactions, model);
        model.addAttribute("user", user);
        model.addAttribute("totalDonated", totalDonated);
        model.addAttribute("donationCount", transactions.size());
        model.addAttribute("recentTransactions", recent);
        model.addAttribute("activePrograms", donationProgramService.findAllActive());
        return "user/dashboard";
    }

    @GetMapping("/programs")
    public String programs(Model model) {
        model.addAttribute("programs", donationProgramService.findAllActive());
        return "user/programs";
    }

    @GetMapping("/statistics")
    public String statistics(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = resolveUser(principal);
        List<DonationTransaction> transactions = donationTransactionService.findByUserId(user.getId());

        addChartAttributes(transactions, model);
        model.addAttribute("user", user);
        model.addAttribute("totalDonated", sumSuccessful(transactions));
        model.addAttribute("donationCount", transactions.size());
        model.addAttribute("successCount", transactions.stream().filter(t -> t.getStatus() == TransactionStatus.SUCCESS).count());
        model.addAttribute("pendingCount", transactions.stream().filter(t -> t.getStatus() == TransactionStatus.PENDING).count());
        return "user/statistics";
    }

    @GetMapping("/donations")
    public String myDonations(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {
        User user = resolveUser(principal);
        List<DonationTransaction> all = donationTransactionService.findByUserId(user.getId());
        List<DonationTransaction> filtered = filterTransactions(all, status, programId, dateFrom, dateTo);

        Set<Long> donatedProgramIds = all.stream()
                .map(t -> t.getProgram().getId())
                .collect(Collectors.toSet());
        List<com.zakaflow.zakaflow.model.DonationProgram> userPrograms = donationProgramService.findAll().stream()
                .filter(p -> donatedProgramIds.contains(p.getId()))
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("transactions", sortByDateDesc(filtered));
        model.addAttribute("userPrograms", userPrograms);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterProgramId", programId);
        model.addAttribute("filterDateFrom", dateFrom);
        model.addAttribute("filterDateTo", dateTo);
        model.addAttribute("statuses", TransactionStatus.values());
        return "user/donations";
    }

    @GetMapping("/donations/{id}")
    public String donationDetail(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            Model model) {
        User user = resolveUser(principal);
        DonationTransaction transaction = requireOwnTransaction(user, id);
        model.addAttribute("user", user);
        model.addAttribute("transaction", transaction);
        return "user/donation-detail";
    }

    @GetMapping("/donations/{id}/print")
    public String donationPrint(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            Model model) {
        User user = resolveUser(principal);
        DonationTransaction transaction = requireOwnTransaction(user, id);
        model.addAttribute("user", user);
        model.addAttribute("transaction", transaction);
        return "user/donation-print";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("user", resolveUser(principal));
        return "user/profile";
    }

    @GetMapping("/profile/edit")
    public String profileEdit(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("user", resolveUser(principal));
        return "user/profile-edit";
    }

    @PostMapping("/profile/edit")
    public String profileUpdate(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        try {
            userService.updateEmail(user.getId(), email);
            redirectAttributes.addFlashAttribute("successMessage", "Email berhasil diperbarui.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/user/profile/edit";
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/profile/password")
    public String passwordForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("user", resolveUser(principal));
        return "user/profile-password";
    }

    @PostMapping("/profile/password")
    public String passwordUpdate(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Konfirmasi password tidak cocok.");
            return "redirect:/user/profile/password";
        }
        try {
            userService.changePassword(user.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password berhasil diubah.");
            return "redirect:/user/profile";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/user/profile/password";
        }
    }

    @PostMapping("/donate")
    public String donate(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Long programId,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod,
            RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);

        if (amount == null || amount.signum() <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nominal donasi harus lebih dari 0.");
            return "redirect:/programs/" + programId;
        }

        try {
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod);
            DonationTransaction transaction = donationTransactionService.create(
                    user.getId(), programId, amount, method);
            return "redirect:/user/payment/" + transaction.getId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/programs/" + programId;
        }
    }

    @GetMapping("/payment/{id}")
    public String paymentPage(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            Model model) {
        User user = resolveUser(principal);
        DonationTransaction transaction = requireOwnTransaction(user, id);

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            return "redirect:/user/donations/" + id;
        }

        model.addAttribute("user", user);
        model.addAttribute("transaction", transaction);
        model.addAttribute("payment", paymentProperties);
        return "user/payment";
    }

    @PostMapping("/payment/{id}/confirm")
    public String confirmPayment(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @RequestParam(required = false) String paymentReference,
            RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        try {
            donationTransactionService.confirmPayment(id, user.getId(), paymentReference);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Pembayaran berhasil dikonfirmasi. Terima kasih atas donasi Anda!");
            return "redirect:/user/donations/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/user/payment/" + id;
        }
    }

    private DonationTransaction requireOwnTransaction(User user, Long id) {
        DonationTransaction transaction = donationTransactionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Akses ditolak");
        }
        return transaction;
    }

    private List<DonationTransaction> filterTransactions(
            List<DonationTransaction> transactions,
            String status,
            Long programId,
            LocalDate dateFrom,
            LocalDate dateTo) {
        return transactions.stream()
                .filter(t -> status == null || status.isBlank() || t.getStatus().name().equalsIgnoreCase(status))
                .filter(t -> programId == null || t.getProgram().getId().equals(programId))
                .filter(t -> dateFrom == null || !t.getTransactionDate().toLocalDate().isBefore(dateFrom))
                .filter(t -> dateTo == null || !t.getTransactionDate().toLocalDate().isAfter(dateTo))
                .toList();
    }

    private List<DonationTransaction> sortByDateDesc(List<DonationTransaction> transactions) {
        return transactions.stream()
                .sorted(Comparator.comparing(DonationTransaction::getTransactionDate).reversed())
                .toList();
    }

    private BigDecimal sumSuccessful(List<DonationTransaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .map(DonationTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void addChartAttributes(List<DonationTransaction> transactions, Model model) {
        Map<YearMonth, BigDecimal> monthly = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getTransactionDate()),
                        Collectors.reducing(BigDecimal.ZERO, DonationTransaction::getAmount, BigDecimal::add)));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            labels.add(month.format(MONTH_LABEL));
            amounts.add(monthly.getOrDefault(month, BigDecimal.ZERO));
        }

        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartAmounts", amounts);
    }

    private User resolveUser(UserDetails principal) {
        return userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User tidak ditemukan"));
    }
}
