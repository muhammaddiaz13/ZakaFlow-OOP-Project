package com.zakaflow.zakaflow.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {

    private String bankName;
    private String accountNumber;
    private String accountHolder;
}
