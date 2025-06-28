package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MenuItemCard {

    @FXML
    private ImageView menuImage;

    @FXML
    private Label menuItemName;

    @FXML
    private Label menuItemPrice;

    @FXML
    private Label menuItemCalories;

    @FXML
    private Spinner<Integer> quantity;

    @FXML
    private Button addToCartButton;

    private MenuController parentController;
    private MenuItem menuItem;
    private MenuItemRepositoryImpl cartRepository;

    public MenuItemCard() {
        this.cartRepository = new MenuItemRepositoryImpl(new DatabaseConnection().getDataSource());
    }

    @FXML
    private void initialize() {
        if (quantity != null) {
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
            quantity.setValueFactory(valueFactory);
        } else {
            System.err.println("quantity Spinner is null");
        }

        if (addToCartButton != null) {
            addToCartButton.setOnAction(event -> addToCart());
        } else {
            System.err.println("addToCartButton is null");
        }
    }

    public void setMenuItem(MenuItem item) {
        this.menuItem = item;
        if (menuItemName != null) {
            menuItemName.setText(item.name());
        } else {
            System.err.println("menuItemName is null");
        }
        if (menuItemPrice != null) {
            menuItemPrice.setText(String.format("%.2f грн", item.price()));
        } else {
            System.err.println("menuItemPrice is null");
        }
        if (menuItemCalories != null) {
            menuItemCalories.setText(item.calories() != null ? item.calories() + " ккал" : "0 ккал");
        } else {
            System.err.println("menuItemCalories is null");
        }
        if (menuImage != null) {
            if (item.image() != null && item.image().length > 0) {
                menuImage.setImage(new Image(new ByteArrayInputStream(item.image())));
            } else {
                menuImage.setImage(new Image(getClass().getResourceAsStream("/data/ingredients.png")));
            }
        } else {
            System.err.println("menuImage is null");
        }
    }

    public void setParentController(MenuController parentController) {
        this.parentController = parentController;
    }

    private int getQuantity() {
        if (quantity == null) {
            System.err.println("quantity Spinner is null");
            return 0;
        }
        try {
            return quantity.getValue();
        } catch (Exception e) {
            System.err.println("Помилка отримання значення зі Spinner: " + e.getMessage());
            return 0;
        }
    }

    private void addToCart() {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertController.showAlert("Будь ласка, увійдіть у систему, щоб додати елемент до кошика.");
            return;
        }

        int qty = getQuantity();
        if (qty <= 0) {
            AlertController.showAlert("Виберіть коректну кількість (більше 0).");
            return;
        }

        // Перевіряємо, чи елемент уже є в кошику користувача
        List<Cart> userCartItems = cartRepository.findByUserId(currentUser.id());
        boolean itemExistsInCart = userCartItems.stream()
            .filter(cartItem -> !cartItem.isOrdered()) // Перевіряємо тільки незамовлені елементи
            .anyMatch(cartItem -> cartItem.itemId().equals(menuItem.itemId()));

        if (itemExistsInCart) {
            AlertController.showAlert("Цей елемент уже є у Вашому кошику!");
            return;
        }

        // Додаємо елемент до кошика
        parentController.addToCart(menuItem, qty);
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
            stage.initModality(Modality.APPLICATION_MODAL); // Модальне вікно
            stage.setResizable(false);

            MenuItemDetailsController controller = loader.getController();
            controller.setMenuItem(menuItem);

            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("Помилка завантаження MenuItemDetails.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}