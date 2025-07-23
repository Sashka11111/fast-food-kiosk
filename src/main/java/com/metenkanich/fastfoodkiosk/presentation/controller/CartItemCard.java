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
import java.io.IOException;
import javax.sql.DataSource;

public class CartItemCard {

    @FXML
    private ImageView menuImage;

    @FXML
    private Label menuItemName;

    @FXML
    private Label menuItemPrice;

    @FXML
    private Label quantityLabel;

    @FXML
    private Button deleteFromCartButton;

    private Cart cartItem;
    private MenuItem menuItem;
    private CartController parentController; // Посилання на CartController для оновлення кошика
    private CartRepositoryImpl cartRepository;

    public CartItemCard() {
        DataSource dataSource = DatabaseConnection.getInstance().getDataSource();
        this.cartRepository = new CartRepositoryImpl(dataSource);
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
        if (quantityLabel != null) {
            quantityLabel.setText("Кількість: " + cartItem.quantity());
        }

        // Встановлення зображення
        setItemImage(menuItem.imagePath(), menuItem.name());

        // Налаштування кнопки видалення
        if (deleteFromCartButton != null) {
            deleteFromCartButton.setOnAction(event -> deleteFromCart());
        }
    }

    private void setItemImage(String imagePath, String itemName) {
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                if (!image.isError()) {
                    menuImage.setImage(image);
                } else {
                    System.err.println("Помилка завантаження зображення для страви: " + itemName + " з шляху: " + imagePath);
                    setDefaultItemImage(itemName);
                }
            } catch (Exception e) {
                System.err.println("Помилка завантаження зображення для страви: " + itemName + " з шляху: " + imagePath + ": " + e.getMessage());
                setDefaultItemImage(itemName);
            }
        } else {
            setDefaultItemImage(itemName);
        }
    }

    private void setDefaultItemImage(String itemName) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/fast-food.jpg"));
            if (!defaultImage.isError()) {
                menuImage.setImage(defaultImage);
            } else {
                System.err.println("Помилка завантаження дефолтного зображення для страви: " + itemName);
                menuImage.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Помилка завантаження дефолтного зображення для страви: " + itemName + ": " + e.getMessage());
            menuImage.setImage(null);
        }
    }

    public void setParentController(CartController controller) {
        this.parentController = controller;
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