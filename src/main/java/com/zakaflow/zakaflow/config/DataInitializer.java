package com.zakaflow.zakaflow.config;

import com.zakaflow.zakaflow.config.BankAccount;
import com.zakaflow.zakaflow.model.Category;
import com.zakaflow.zakaflow.model.PaymentChannel;
import com.zakaflow.zakaflow.model.PaymentMethod;
import com.zakaflow.zakaflow.model.Role;
import com.zakaflow.zakaflow.repository.CategoryRepository;
import com.zakaflow.zakaflow.repository.PaymentMethodRepository;
import com.zakaflow.zakaflow.service.RoleService;
import com.zakaflow.zakaflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentProperties paymentProperties;
    private final RoleService roleService;
    private final UserService userService;
    // private final RoleService roleService;

    @Override
    public void run(String... args) {
        seedCategory("Zakat", "Kewajiban zakat bagi yang memenuhi syarat");
        seedCategory("Infak", "Sedekah sunnah di luar zakat");
        seedCategory("Sedekah", "Bantuan sukarela untuk yang membutuhkan");

        seedRole("ADMIN");
        seedRole("DONATUR");
        seedPaymentMethods();

        if (userService.findByUsername("admin").isEmpty()) {
            userService.register("admin", "admin@zakaflow.local", "admin123", "ADMIN");
        }
        if (userService.findByUsername("donatur").isEmpty()) {
            userService.register("donatur", "donatur@zakaflow.local", "donatur123", "DONATUR");
        }
    }

    private void seedCategory(String name, String description) {
        if (categoryRepository.findByName(name).isEmpty()) {
            categoryRepository.save(new Category(null, name, description, null));
        }
    }

    private void seedRole(String roleName) {
        String normalized = roleName.trim().toUpperCase();
        if (roleService.findByName(normalized).isEmpty()) {
            roleService.save(new Role(null, normalized));
        }
    }

    private void seedPaymentMethods() {
        if (paymentMethodRepository.count() > 0) {
            return;
        }
        for (BankAccount bank : paymentProperties.getBankAccounts()) {
            PaymentMethod method = new PaymentMethod();
            method.setName(bank.getBankName());
            method.setAccountNumber(bank.getAccountNumber());
            method.setAccountHolder(bank.getAccountHolder());
            method.setChannel(PaymentChannel.TRANSFER_BANK);
            method.setActive(true);
            paymentMethodRepository.save(method);
        }
    }
}
