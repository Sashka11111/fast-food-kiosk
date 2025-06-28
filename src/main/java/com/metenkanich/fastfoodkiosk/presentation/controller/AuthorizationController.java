package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
import com.metenkanich.fastfoodkiosk.domain.security.PasswordHashing;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.UserRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.UserRepositoryImpl;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AuthorizationController {

  @FXML
  private Button authSignInButton;

  @FXML
  private Button authSingUpButton;

  @FXML
  private TextField loginTextField;

  @FXML
  private PasswordField passwordField;

  @FXML
  private Button btnClose;

  private UserRepository userRepository;

  public AuthorizationController() {
    this.userRepository = new UserRepositoryImpl(new DatabaseConnection().getDataSource());
  }

  @FXML
  void initialize() {
    btnClose.setOnAction(event -> {
      System.exit(0);
    });
    authSignInButton.setOnAction(event -> {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/registration.fxml"));
        Parent root = loader.load();
        Scene newScene = new Scene(root);
        // Встановлюємо нову сцену на поточному вікні
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.setScene(newScene);
        primaryStage.show();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });


    authSingUpButton.setOnAction(event -> {
      String loginText = loginTextField.getText().trim();
      String loginPassword = passwordField.getText().trim();

      if (!loginText.isEmpty() && !loginPassword.isEmpty()) {
        try {
          // Перевірка логіну та пароля користувача
          User user = userRepository.findByUsername(loginText);
          if (user != null) {
            // Хешування введеного пароля
            String hashedPassword = PasswordHashing.getInstance().hashedPassword(loginPassword);
            if (user.password().equals(hashedPassword)) {
              AuthenticatedUser.getInstance().setCurrentUser(user);
              authSingUpButton.getScene().getWindow().hide();
              FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mainMenu.fxml"));
              Parent root = loader.load();
              Stage stage = new Stage();
              stage.getIcons().add(new Image(getClass().getResourceAsStream("/data/icon.png")));
              stage.setScene(new Scene(root));
              stage.initStyle(StageStyle.UNDECORATED);
              stage.showAndWait();
            } else {
              AlertController.showAlert("Неправильний логін або пароль");
            }
          }
        } catch (EntityNotFoundException | IOException e) {
          AlertController.showAlert("Неправильний логін або пароль");
        }
      } else {
        AlertController.showAlert("Будь ласка, введіть логін та пароль");
      }
    });
  }
}