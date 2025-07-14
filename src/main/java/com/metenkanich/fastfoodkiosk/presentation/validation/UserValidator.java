package com.metenkanich.fastfoodkiosk.presentation.validation;

import java.util.regex.Pattern;

public class UserValidator {
  // Мінімальна довжина паролю
  private static final int MIN_PASSWORD_LENGTH = 6;
  // Максимальна довжина паролю
  private static final int MAX_PASSWORD_LENGTH = 20;
  // Максимальна довжина email
  private static final int MAX_EMAIL_LENGTH = 100;

  // Паттерн для перевірки складності паролю
  private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$";

  // Паттерн для перевірки формату email
  private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

  // Перевірка унікальності імені користувача
  public static boolean isUsernameValid(String username) {
    return username != null && !username.isEmpty();
  }

  // Перевірка паролю
  public static boolean isPasswordValid(String password) {
    return password != null &&
        password.length() >= MIN_PASSWORD_LENGTH &&
        password.length() <= MAX_PASSWORD_LENGTH &&
        Pattern.matches(PASSWORD_PATTERN, password);
  }

  // Перевірка email
  public static boolean isEmailValid(String email) {
    return email != null &&
        !email.isEmpty() &&
        email.length() <= MAX_EMAIL_LENGTH &&
        Pattern.matches(EMAIL_PATTERN, email);
  }
}