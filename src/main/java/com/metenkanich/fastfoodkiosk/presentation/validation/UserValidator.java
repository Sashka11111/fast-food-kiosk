package com.metenkanich.fastfoodkiosk.presentation.validation;

import java.util.regex.Pattern;

public class UserValidator {
  private static final int MIN_PASSWORD_LENGTH = 6;
  private static final int MAX_PASSWORD_LENGTH = 20;
  private static final int MAX_EMAIL_LENGTH = 100;

  private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$";

  private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

  public static boolean isUsernameValid(String username) {
    return username != null && !username.isEmpty();
  }

  public static boolean isPasswordValid(String password) {
    return password != null &&
        password.length() >= MIN_PASSWORD_LENGTH &&
        password.length() <= MAX_PASSWORD_LENGTH &&
        Pattern.matches(PASSWORD_PATTERN, password);
  }

  public static boolean isEmailValid(String email) {
    return email != null &&
        !email.isEmpty() &&
        email.length() <= MAX_EMAIL_LENGTH &&
        Pattern.matches(EMAIL_PATTERN, email);
  }
}