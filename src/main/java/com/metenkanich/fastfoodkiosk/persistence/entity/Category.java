package com.metenkanich.fastfoodkiosk.persistence.entity;

import java.util.UUID;

public record Category(
    UUID categoryId,
    String categoryName
) implements Entity,Comparable<Category> {
  @Override
  public int compareTo(Category o) {
    return this.categoryName.compareTo(o.categoryName);
  }

  @Override
  public UUID id() {
    return categoryId;
  }
}
