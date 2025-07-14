package com.metenkanich.fastfoodkiosk.persistence.entity;

import com.metenkanich.fastfoodkiosk.persistence.entity.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Order(
    UUID orderId,
    UUID userId,
    BigDecimal totalPrice,
    OrderStatus status,
    LocalDateTime createdAt
) implements Entity, Comparable<Order> {
  @Override
  public int compareTo(Order o) {
    return this.createdAt.compareTo(o.createdAt);
  }

  @Override
  public UUID id() {
    return orderId;
  }
}