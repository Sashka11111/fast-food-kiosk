package com.metenkanich.fastfoodkiosk.persistence.entity;

import java.util.UUID;

public record Cart(
    UUID cartId,
    UUID userId,
    UUID itemId,
    int quantity,
    double subtotal,
    boolean isOrdered
) implements Entity,Comparable<Cart> {

    @Override
    public int compareTo(Cart o) {
        int orderComparison = this.userId.compareTo(o.userId);
        if (orderComparison != 0) return orderComparison;
        return this.itemId.compareTo(o.itemId);
    }

    @Override
    public UUID id() {
        return cartId;
    }
}