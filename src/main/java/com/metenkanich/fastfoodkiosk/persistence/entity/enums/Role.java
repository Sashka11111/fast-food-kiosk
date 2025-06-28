package com.metenkanich.fastfoodkiosk.persistence.entity.enums;

public enum Role {
  USER("Користувач"),
  ADMIN("Адміністратор");

  private final String label;

  Role(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
