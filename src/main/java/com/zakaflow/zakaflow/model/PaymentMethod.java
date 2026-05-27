package com.zakaflow.zakaflow.model;

public enum PaymentMethod {
    QRIS("QRIS"),
    TRANSFER_BANK("Transfer Bank (Semua Bank)"),
    E_WALLET("E-Wallet"),
    CASH("Tunai");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
