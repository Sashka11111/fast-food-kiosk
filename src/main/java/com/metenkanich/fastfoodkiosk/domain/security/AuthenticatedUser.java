package com.metenkanich.fastfoodkiosk.domain.security;


import com.metenkanich.fastfoodkiosk.persistence.entity.User;

public class AuthenticatedUser {
  private static AuthenticatedUser instance; // Єдиний екземпляр класу
  private User currentUser; // Поточний аутентифікований користувач

  private AuthenticatedUser() {} // Приватний конструктор для заборони створення зовнішніми класами

  public static AuthenticatedUser getInstance() {
    if (instance == null) {
      instance = new AuthenticatedUser();
    }
    return instance;
  }

  public User getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(User user) {
    this.currentUser = user;
  }
}