package com.metenkanich.fastfoodkiosk.persistence.entity;

import com.metenkanich.fastfoodkiosk.persistence.entity.enums.Role;
import java.time.LocalDateTime;
import java.util.UUID;

public record User(
    UUID userId,
    String username,
    String password,
    Role role,
    String email,
    LocalDateTime createdAt
) implements Entity, Comparable<User> {
  @Override
  public int compareTo(User o) {
    return this.username.compareTo(o.username);
  }

  @Override
  public UUID id() {
    return userId;
  }
}
