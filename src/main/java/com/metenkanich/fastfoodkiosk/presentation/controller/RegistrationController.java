package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.domain.security.PasswordHashing;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.Role;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.UserRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.UserRepositoryImpl;
import com.metenkanich.fastfoodkiosk.presentation.validation.UserValidator;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController {

  @FXML
  private Button signInButton;

  @FXML
  private Button signUpButton;

  @FXML
  private Button btnClose;

  @FXML
  private TextField loginField;

  @FXML
  private PasswordField passwordField;

  @FXML
  private TextField emailField;

  private UserRepository userRepository;

  public RegistrationController() {
    this.userRepository = new UserRepositoryImpl(new DatabaseConnection().getDataSource());
  }

  @FXML
  void initialize() {
    btnClose.setOnAction(event -> System.exit(0));

    signInButton.setOnAction(event -> switchScene("/view/authorization.fxml"));

    signUpButton.setOnAction(event -> handleSignIn());
  }

  private void switchScene(String fxmlPath) {
    Scene currentScene = signUpButton.getScene();
    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
    try {
      Parent root = loader.load();
      currentScene.setRoot(root);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void handleSignIn() {
    UUID userId = UUID.randomUUID();
    String username = loginField.getText();
    String password = passwordField.getText();
    String email = emailField.getText();

    if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
      displayError("Логін, пароль та email не повинні бути пустими");
      return;
    }

    if (!UserValidator.isUsernameValid(username)) {
      displayError("Логін не може бути порожнім та повинен містити символи тільки латинського алфавіту");
      return;
    }

    if (!UserValidator.isPasswordValid(password)) {
      displayError("Пароль має містити велику, маленьку букву та цифру (від 6 до 20 символів)");
      return;
    }

    if (!UserValidator.isEmailValid(email)) {
      displayError("Невірний формат email");
      return;
    }
    try {
      userRepository.findByUsername(username);
      displayError("Користувач з таким ім'ям вже існує");
      return;
    } catch (EntityNotFoundException e) {
      // Якщо користувача не знайдено, це добре, ми можемо його створити
    }
    String hashedPassword = PasswordHashing.getInstance().hashedPassword(password);

      User user = new User(userId, username, hashedPassword, Role.USER, email, LocalDateTime.now());
      userRepository.addUser(user);
     switchScene("/view/authorization.fxml");
  }

  private void displayError(String message) {
    AlertController.showAlert(message);
  }
}