package com.metenkanich.fastfoodkiosk.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItem(
    UUID id,
    UUID orderId,
    UUID menuItemId,
    Integer quantity,
    BigDecimal price
) implements Entity, Comparable<OrderItem> {
    @Override
    public int compareTo(OrderItem o) {
        return this.id.compareTo(o.id);
    }

    @Override
    public UUID id() {
        return id;
    }
}
