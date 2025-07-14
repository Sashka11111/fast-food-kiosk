package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;

public class MenuItemDetailsController {

    @FXML
    private ImageView menuImage;

    @FXML
    private Label menuItemName;

    @FXML
    private Label menuItemPrice;

    @FXML
    private Label menuItemCalories;

    @FXML
    private Label menuItemDescription;

    @FXML
    private Label menuItemIngredients;

    @FXML
    private Button closeButton;

    private MenuItem menuItem;

    public void setMenuItem(MenuItem item) {
        this.menuItem = item;
        if (menuItemName != null) {
            menuItemName.setText(item.name());
        }
        if (menuItemPrice != null) {
            menuItemPrice.setText(String.format("%.2f грн", item.price()));
        }
//        if (menuItemCalories != null) {
//            menuItemCalories.setText(item.calories() != null ? item.calories() + " ккал" : "0 ккал");
//        }
        if (menuItemDescription != null) {
            menuItemDescription.setText(item.description() != null ? item.description() : "Опис відсутній");
        }
//        if (menuItemIngredients != null) {
//            menuItemIngredients.setText(item.ingredients() != null ? "Інгредієнти: " + item.ingredients() : "Інгредієнти відсутні");
//        }
//        if (menuImage != null) {
//            if (item.image() != null && item.image().length > 0) {
//                menuImage.setImage(new Image(new ByteArrayInputStream(item.image())));
//            } else {
//                menuImage.setImage(new Image(getClass().getResourceAsStream("/data/ingredients.png")));
//            }
//        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}