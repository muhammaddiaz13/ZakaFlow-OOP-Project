package com.zakaflow.zakaflow.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "zakaflow.payment")
public class PaymentProperties {

    private String qrisMerchant = "ZakaFlow Donasi";
    private String ewalletInfo = "GoPay / OVO / DANA — 081234567890 (a.n. ZakaFlow)";
    private List<BankAccount> bankAccounts = new ArrayList<>();

    @PostConstruct
    void initDefaults() {
        if (bankAccounts.isEmpty()) {
            String holder = "Yayasan ZakaFlow";
            bankAccounts.add(new BankAccount("BCA", "1234567890", holder));
            bankAccounts.add(new BankAccount("BRI", "9876543210", holder));
            bankAccounts.add(new BankAccount("Bank Mandiri", "1122334455", holder));
            bankAccounts.add(new BankAccount("BNI", "5566778899", holder));
            bankAccounts.add(new BankAccount("BTN", "6677889900", holder));
            bankAccounts.add(new BankAccount("BSI (Bank Syariah)", "7788990011", holder));
        }
    }

    public String getAccountHolder() {
        return bankAccounts.isEmpty() ? "Yayasan ZakaFlow" : bankAccounts.getFirst().getAccountHolder();
    }
}
