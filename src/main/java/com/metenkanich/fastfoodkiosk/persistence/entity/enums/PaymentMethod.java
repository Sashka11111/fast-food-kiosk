package com.metenkanich.fastfoodkiosk.persistence.entity.enums;

public enum PaymentMethod {
    CASH("Готівка"),
    CARD("Банківська картка");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
