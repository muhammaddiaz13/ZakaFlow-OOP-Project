package com.zakaflow.zakaflow.model;

public enum PaymentChannel {
    QRIS("QRIS"),
    TRANSFER_BANK("Transfer Bank (Semua Bank)"),
    E_WALLET("E-Wallet"),
    CASH("Tunai");

    private final String label;

    PaymentChannel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
