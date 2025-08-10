package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.Role;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainMenuController {

    @FXML
    private StackPane contentArea;

    @FXML
    private StackPane stackPane;

    @FXML
    private Button closeButton;

    @FXML
    private Button minimazeButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button paymentButton;

    @FXML
    private Button categoryButton;

    @FXML
    private Button menuItemsButton;

    @FXML
    private Button ordersButton;

    @FXML
    private Button cartButton;

    @FXML
    private Button menuButton;
    private Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    void initialize() {
        closeButton.setOnAction(event -> System.exit(0));
        minimazeButton.setOnAction(event -> minimizeWindow());
        cartButton.setOnAction(event -> showCartPage());
        menuButton.setOnAction(event -> showMenuPage());
        usersButton.setOnAction(event -> showUsersPage());
        paymentButton.setOnAction(event -> showPaymentPage());
        categoryButton.setOnAction(event -> showCategoryPage());
        menuItemsButton.setOnAction(event -> showMenuItemsPage());
        ordersButton.setOnAction(event -> showOrdersPage());
        showMenu();
        Platform.runLater(() -> {
            Stage primaryStage = (Stage) contentArea.getScene().getWindow();
            addDragListeners(primaryStage.getScene().getRoot());
        });
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();

        if (currentUser.role() != Role.ADMIN) {
            usersButton.setVisible(false);
            categoryButton.setVisible(false);
            menuItemsButton.setVisible(false);
            ordersButton.setVisible(false);
            paymentButton.setVisible(false);
        }
    }

    private void moveStackPane(Button button) {
        double buttonX = button.localToParent(button.getBoundsInLocal()).getMinX();
        double buttonY = button.localToParent(button.getBoundsInLocal()).getMinY();
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.3), stackPane);
        transition.setToX(buttonX);
        stackPane.setLayoutY(buttonY);
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

    private void showMenuPage() {
        moveStackPane(menuButton);
        loadFXML("/view/menu.fxml");
    }

    private void showCartPage() {
        moveStackPane(cartButton);
        loadFXML("/view/cart.fxml");
    }

    private void showUsersPage() {
        moveStackPane(usersButton);
        loadFXML("/view/user_management.fxml");
    }

    private void showPaymentPage() {
        moveStackPane(paymentButton);
        loadFXML("/view/payment.fxml");
    }

    private void showCategoryPage() {
        moveStackPane(categoryButton);
        loadFXML("/view/category.fxml");
    }

    private void showMenuItemsPage() {
        moveStackPane(menuItemsButton);
        loadFXML("/view/menu_item.fxml");
    }
    private void showOrdersPage() {
        moveStackPane(ordersButton);
        loadFXML("/view/orders.fxml");
    }

    private void showMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menu.fxml"));
            Parent menuPane = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(menuPane);
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
}