package com.metenkanich.fastfoodkiosk.persistence.entity.enums;

public enum PaymentStatus {
    PENDING("Очікується"),
    COMPLETED("Завершено"),
    FAILED("Неуспішно");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

