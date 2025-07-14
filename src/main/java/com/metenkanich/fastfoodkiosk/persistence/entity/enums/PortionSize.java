package com.metenkanich.fastfoodkiosk.persistence.entity.enums;

/**
 * Enum для розмірів порцій страв
 */
public enum PortionSize {
    SMALL("Маленька", 0.8),
    MEDIUM("Середня", 1.0),
    LARGE("Велика", 1.3),
    EXTRA_LARGE("Екстра велика", 1.6);

    private final String displayName;
    private final double priceMultiplier;

    PortionSize(String displayName, double priceMultiplier) {
        this.displayName = displayName;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
