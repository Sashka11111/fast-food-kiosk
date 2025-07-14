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
        showMenu();

        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();


        if (currentUser.role() != Role.ADMIN) {
//            manageOrdersButton.setVisible(false);
//            manageMenuButton.setVisible(false);
//            categoryButton.setVisible(false);
//            usersButton.setVisible(false);
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