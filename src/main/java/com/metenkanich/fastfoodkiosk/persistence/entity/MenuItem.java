package com.metenkanich.fastfoodkiosk.persistence.entity;

import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PortionSize;
import java.math.BigDecimal;
import java.util.UUID;

public record MenuItem(
    UUID itemId,
    String name,
    String description,
    BigDecimal price,
    UUID categoryId,
    Boolean isAvailable,
    String imagePath,
    PortionSize defaultPortionSize
) implements Entity, Comparable<MenuItem> {
  @Override
  public int compareTo(MenuItem o) {
    return this.name.compareTo(o.name);
  }

  @Override
  public UUID id() {
    return itemId;
  }

  public BigDecimal getPriceForSize(PortionSize size) {
    if (size == null) {
      size = defaultPortionSize != null ? defaultPortionSize : PortionSize.MEDIUM;
    }
    return price.multiply(BigDecimal.valueOf(size.getPriceMultiplier()));
  }
}