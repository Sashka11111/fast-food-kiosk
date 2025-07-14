package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Cart;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CartRepositoryImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CartItemCard {

    @FXML
    private ImageView menuImage;

    @FXML
    private Label menuItemName;

    @FXML
    private Label menuItemPrice;

    @FXML
    private Label menuItemCalories;

    @FXML
    private Label quantityLabel;

    @FXML
    private Button deleteFromCartButton;

    private Cart cartItem;
    private MenuItem menuItem;
    private CartController parentController; // Посилання на CartController для оновлення кошика
    private CartRepositoryImpl cartRepository;

    public CartItemCard() {
        this.cartRepository = new CartRepositoryImpl(DatabaseConnection.getStaticDataSource());
    }

    public void setCartItem(Cart cartItem, MenuItem menuItem) {
        this.cartItem = cartItem;
        this.menuItem = menuItem;

        if (menuItemName != null) {
            menuItemName.setText(menuItem.name());
        }
        if (menuItemPrice != null) {
            menuItemPrice.setText(String.format("%.2f грн", cartItem.subtotal()));
        }
//        if (menuItemCalories != null) {
//            menuItemCalories.setText(menuItem.calories() != null ? menuItem.calories() + " ккал" : "0 ккал");
//        }
//        if (quantityLabel != null) {
//            quantityLabel.setText("Кількість: " + cartItem.quantity());
//        }
//        if (menuImage != null) {
//            if (menuItem.imagePath() != null && menuItem.imagePath().length > 0) {
//                menuImage.setImage(new Image(new ByteArrayInputStream(menuItem.image())));
//            } else {
//                menuImage.setImage(new Image(getClass().getResourceAsStream("/data/ingredients.png")));
//            }
//        }

        // Налаштування кнопки видалення
        if (deleteFromCartButton != null) {
            deleteFromCartButton.setOnAction(event -> deleteFromCart());
        }
    }

    public void setParentController(CartController controller) {
        this.parentController = controller;
    }

    @FXML
    private void showDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menuItemDetails.fxml"));
            Scene scene = new Scene(loader.load(), 300, 450);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/data/icon.png")));
            stage.setTitle("Деталі страви");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            MenuItemDetailsController controller = loader.getController();
            controller.setMenuItem(menuItem);

            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("Помилка завантаження MenuItemDetails.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteFromCart() {
        try {
            cartRepository.deleteById(cartItem.cartId());
            if (parentController != null) {
                parentController.loadCartItems();
            }
        } catch (EntityNotFoundException e) {
            System.err.println("Помилка видалення з кошика: " + e.getMessage());
            e.printStackTrace();
        }
    }
}