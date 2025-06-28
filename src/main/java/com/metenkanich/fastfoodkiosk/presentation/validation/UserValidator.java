package com.metenkanich.fastfoodkiosk.presentation.validation;

import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.UserRepositoryImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class UserValidator {
  // Constants for validation
  private static final int MIN_USERNAME_LENGTH = 3;
  private static final int MAX_USERNAME_LENGTH = 50;
  private static final int MIN_PASSWORD_LENGTH = 8;
  private static final int MAX_PASSWORD_LENGTH = 100;
  private static final int MAX_EMAIL_LENGTH = 100;

  // Patterns for validation
  private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";
  private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

  // Validate user ID
  public static ValidationResult isUserIdValid(UUID userId, boolean isExisting) {
    if (isExisting && userId == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Ідентифікатор користувача не може бути відсутнім для існуючого користувача");
      return new ValidationResult(false, errors);
    }
    return new ValidationResult(true);
  }

  // Validate username
  public static ValidationResult isUsernameValid(String username) {
    List<String> errors = new ArrayList<>();
    if (username == null) {
      errors.add("Ім'я користувача не може бути відсутнім");
      return new ValidationResult(false, errors);
    }
    if (username.length() < MIN_USERNAME_LENGTH) {
      errors.add("Ім'я користувача \"" + username + "\" повинно містити щонайменше " + MIN_USERNAME_LENGTH + " символи");
    }
    if (username.length() > MAX_USERNAME_LENGTH) {
      errors.add("Ім'я користувача \"" + username + "\" не може перевищувати " + MAX_USERNAME_LENGTH + " символів");
    }
    if (!Pattern.matches(USERNAME_PATTERN, username)) {
      errors.add("Ім'я користувача \"" + username + "\" може містити лише літери, цифри та підкреслення");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  // Validate password
  public static ValidationResult isPasswordValid(String password) {
    List<String> errors = new ArrayList<>();
    if (password == null) {
      errors.add("Пароль не може бути відсутнім");
      return new ValidationResult(false, errors);
    }
    if (password.length() < MIN_PASSWORD_LENGTH) {
      errors.add("Пароль повинен містити щонайменше " + MIN_PASSWORD_LENGTH + " символів");
    }
    if (password.length() > MAX_PASSWORD_LENGTH) {
      errors.add("Пароль не може перевищувати " + MAX_PASSWORD_LENGTH + " символів");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  // Validate email
  public static ValidationResult isEmailValid(String email) {
    List<String> errors = new ArrayList<>();
    if (email == null) {
      errors.add("Електронна пошта не може бути відсутньою");
      return new ValidationResult(false, errors);
    }
    if (email.length() > MAX_EMAIL_LENGTH) {
      errors.add("Електронна пошта \"" + email + "\" не може перевищувати " + MAX_EMAIL_LENGTH + " символів");
    }
    if (!Pattern.matches(EMAIL_PATTERN, email)) {
      errors.add("Електронна пошта \"" + email + "\" має некоректний формат");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  // Validate username uniqueness
  public static ValidationResult isUsernameUnique(String username, UUID userId, UserRepositoryImpl repository) {
    ValidationResult usernameValidation = isUsernameValid(username);
    if (!usernameValidation.isValid()) {
      return usernameValidation;
    }

    List<String> errors = new ArrayList<>();
    try {
      User existingUser = null;
      try {
        existingUser = repository.findByUsername(username);
      } catch (Exception e) {
        if (!e.getMessage().contains("не знайдено")) {
          throw e;
        }
      }

      if (existingUser != null) {
        if (userId == null || !existingUser.userId().equals(userId)) {
          errors.add("Ім'я користувача \"" + username + "\" вже використовується іншим користувачем");
          return new ValidationResult(false, errors);
        }
      }
    } catch (Exception e) {
      errors.add("Помилка перевірки унікальності імені користувача: " + e.getMessage());
      return new ValidationResult(false, errors);
    }

    return new ValidationResult(true);
  }

  // Full validation of User object
  public static ValidationResult isUserValid(User user, boolean isExisting, UserRepositoryImpl repository) {
    if (user == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Користувач не може бути відсутнім");
      return new ValidationResult(false, errors);
    }

    List<String> errors = new ArrayList<>();

    // Validate user ID
    ValidationResult userIdResult = isUserIdValid(user.userId(), isExisting);
    if (!userIdResult.isValid()) {
      errors.addAll(userIdResult.getErrors());
    }

    // Validate username
    ValidationResult usernameResult = isUsernameValid(user.username());
    if (!usernameResult.isValid()) {
      errors.addAll(usernameResult.getErrors());
    }

    // Validate username uniqueness
    ValidationResult usernameUniqueResult = isUsernameUnique(user.username(), user.userId(), repository);
    if (!usernameUniqueResult.isValid()) {
      errors.addAll(usernameUniqueResult.getErrors());
    }

    // Validate password
    ValidationResult passwordResult = isPasswordValid(user.password());
    if (!passwordResult.isValid()) {
      errors.addAll(passwordResult.getErrors());
    }

    // Validate email
    ValidationResult emailResult = isEmailValid(user.email());
    if (!emailResult.isValid()) {
      errors.addAll(emailResult.getErrors());
    }

    // Validate role
    if (user.role() == null) {
      errors.add("Роль користувача не може бути відсутньою");
    }

    // Validate createdAt
    if (user.createdAt() == null) {
      errors.add("Дата створення користувача не може бути відсутньою");
    }

    return new ValidationResult(errors.isEmpty(), errors);
  }
}