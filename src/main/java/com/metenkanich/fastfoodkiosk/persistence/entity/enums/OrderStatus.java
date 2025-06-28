package com.metenkanich.fastfoodkiosk.persistence.entity.enums;

public enum OrderStatus {
    PENDING("Очікує підтвердження"),
    CONFIRMED("Підтверджено"),
    PREPARING("Готується"),
    READY("Готове"),
    DELIVERED("Доставлено"),
    CANCELLED("Скасовано");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
