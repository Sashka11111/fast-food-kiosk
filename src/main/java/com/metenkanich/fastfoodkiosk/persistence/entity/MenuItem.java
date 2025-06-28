package com.metenkanich.fastfoodkiosk.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItem(
    UUID itemId,
    String name,
    String description,
    BigDecimal price,
    UUID categoryId,
    Boolean isAvailable,
    byte[] image
) implements Entity, Comparable<MenuItem> {
  @Override
  public int compareTo(MenuItem o) {
    return this.name.compareTo(o.name);
  }

  @Override
  public UUID id() {
    return itemId;
  }
}