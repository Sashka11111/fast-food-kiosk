package com.metenkanich.fastfoodkiosk.persistence.entity;

import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Payment(
    UUID id,
    UUID orderId,
    BigDecimal amount,
    String paymentMethod,
    PaymentStatus paymentStatus,
    LocalDateTime createdAt
) implements Entity, Comparable<Payment> {
    @Override
    public int compareTo(Payment o) {
        return this.createdAt.compareTo(o.createdAt);
    }

    @Override
    public UUID id() {
        return id;
    }
}
