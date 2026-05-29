package com.zakaflow.zakaflow.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "account_holder", nullable = false, length = 150)
    private String accountHolder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentChannel channel = PaymentChannel.TRANSFER_BANK;

    @Column(nullable = false)
    private boolean active = true;
}
