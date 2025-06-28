package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.Role;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainMenuController {

  @FXML
  private Button menuButton;

  @FXML
  private Button changeAccountButton;

  @FXML
  private Button ordersButton;

  @FXML
  private Button closeButton;

  @FXML
  private StackPane contentArea;

  @FXML
  private Button manageOrdersButton;

  @FXML
  private Button manageMenuButton;
  @FXML
  private Button usersButton;

  @FXML
  private Button themesButton;

  @FXML
  private Button categoryButton;

  @FXML
  private Button minimazeButton;
  @FXML
  private Button cartButton;

  @FXML
  private StackPane stackPane;

  @FXML
  private Label userName;

  private Stage stage;
  private double xOffset = 0;
  private double yOffset = 0;
  @FXML
  void initialize() {
    closeButton.setOnAction(event -> System.exit(0));
    minimazeButton.setOnAction(event -> minimizeWindow());
    showReservation();
    menuButton.setOnAction(event->showMenuPage());
    cartButton.setOnAction(event->showCartPage());
    ordersButton.setOnAction(event->showOrdersPage());
    manageOrdersButton.setOnAction(event->showManageOrdersPage());
    manageMenuButton.setOnAction(event->showManageMenuPage());
    themesButton.setOnAction(event->showThemePage());
    categoryButton.setOnAction(event->showCategoriesPage());
    usersButton.setOnAction(event->showUsersPage());
    changeAccountButton.setOnAction(event -> handleChangeAccountAction());

    User currentUser = AuthenticatedUser.getInstance().getCurrentUser();

    userName.setText(currentUser.username());

    if (currentUser.role() != Role.ADMIN) {
      manageOrdersButton.setVisible(false);
      manageMenuButton.setVisible(false);
      categoryButton.setVisible(false);
      usersButton.setVisible(false);
    }

    Platform.runLater(() -> {
      Stage primaryStage = (Stage) contentArea.getScene().getWindow();
      addDragListeners(primaryStage.getScene().getRoot());
    });
  }

  private void moveStackPane(Button button) {
    double buttonX = button.localToParent(button.getBoundsInLocal()).getMinX();
    double buttonY = button.localToParent(button.getBoundsInLocal()).getMinY();
    TranslateTransition transition = new TranslateTransition(Duration.seconds(0.3), stackPane);
    transition.setToX(buttonX);
    stackPane.setLayoutY(buttonY);
  }

  private void showMenuPage() {
    moveStackPane(menuButton);
    loadFXML("/view/menu.fxml");
  }
  private void showCartPage() {
    moveStackPane(cartButton);
    loadFXML("/view/cart.fxml");
  }
  private void showOrdersPage() {
    moveStackPane(ordersButton);
    loadFXML("/view/orders.fxml");
  }
  private void showManageOrdersPage() {
    moveStackPane(manageOrdersButton);
    loadFXML("/view/ordersManagement.fxml");
  }
  private void showManageMenuPage() {
    moveStackPane(manageMenuButton);
    loadFXML("/view/menuManagement.fxml");
  }
  private void showThemePage() {
    moveStackPane(themesButton);
    loadFXML("/view/themes.fxml");
  }
  private void showCategoriesPage() {
    moveStackPane(categoryButton);
    loadFXML("/view/categoryManager.fxml");
  }
  private void showUsersPage() {
    moveStackPane(usersButton);
    loadFXML("/view/userManagement.fxml");
  }
  private void loadFXML(String fxmlFileName) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
      Parent fxml = loader.load();
      contentArea.getChildren().clear();
      contentArea.getChildren().add(fxml);
    } catch (IOException ex) {
      Logger.getLogger(MainMenuController.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void showReservation() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menu.fxml"));
      AnchorPane bookingsAnchorPane = loader.load();
      contentArea.getChildren().clear();
      contentArea.getChildren().add(bookingsAnchorPane);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void minimizeWindow() {
    if (stage == null) {
      stage = (Stage) minimazeButton.getScene().getWindow();
    }
    stage.setIconified(true);
  }

  private void addDragListeners(Parent root) {
    root.setOnMousePressed(event -> {
      xOffset = event.getSceneX();
      yOffset = event.getSceneY();
    });

    root.setOnMouseDragged(event -> {
      Stage stage = (Stage) ((Parent) event.getSource()).getScene().getWindow();
      stage.setX(event.getScreenX() - xOffset);
      stage.setY(event.getScreenY() - yOffset);
    });
  }
  private void handleChangeAccountAction() {
    try {
      // Анімація для поточного вікна
      Stage currentStage = (Stage) changeAccountButton.getScene().getWindow();
      FadeTransition fadeOut = new FadeTransition(Duration.millis(500), currentStage.getScene().getRoot());
      fadeOut.setFromValue(1.0);
      fadeOut.setToValue(0.0);
      fadeOut.setOnFinished(event -> {
        try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/authorization.fxml"));
          Parent root = loader.load();

          // Анімація для нового вікна
          Stage loginStage = new Stage();
          loginStage.getIcons().add(new Image(getClass().getResourceAsStream("/data/icon.png")));
          loginStage.initStyle(StageStyle.UNDECORATED);
          Scene scene = new Scene(root);
          scene.getRoot().setOpacity(0.0);
          loginStage.setScene(scene);
          loginStage.show();

          FadeTransition fadeIn = new FadeTransition(Duration.millis(500), scene.getRoot());
          fadeIn.setFromValue(0.0);
          fadeIn.setToValue(1.0);
          fadeIn.play();

          // Закриття поточного вікна
          currentStage.close();
        } catch (IOException ex) {
          Logger.getLogger(MainMenuController.class.getName()).log(Level.SEVERE, null, ex);
        }
      });
      fadeOut.play();
    } catch (Exception ex) {
      Logger.getLogger(MainMenuController.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}