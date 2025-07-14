package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Cart;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PortionSize;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CartRepositoryImpl;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.math.BigDecimal;
import java.util.UUID;

public class MenuItemCardController {

    @FXML
    private ImageView itemImage;

    @FXML
    private Text itemName;

    @FXML
    private Text itemDescription;

    @FXML
    private ComboBox<PortionSize> portionSizeComboBox;

    @FXML
    private Text itemPrice;

    @FXML
    private Label availabilityLabel;

    @FXML
    private Button addToCartButton;
    @FXML
    private Spinner<Integer> quantity;

    private MenuItem currentMenuItem;
    private UUID userId;
    private CartRepositoryImpl cartRepository;
    private Label errorLabel; // Для відображення повідомлень
    private MenuController parentController;
    private MenuItem menuItem;

    public MenuItemCardController() {
        this.cartRepository = new CartRepositoryImpl(DatabaseConnection.getStaticDataSource());
    }

    @FXML
    private void initialize() {
        if (quantity != null) {
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
            quantity.setValueFactory(valueFactory);
        } else {
            System.err.println("кількість Spinner дорівнює нулю");
        }

        if (addToCartButton != null) {
            addToCartButton.setOnAction(event -> addToCart());
        }
    }

    public void setMenuItem(MenuItem menuItem) {
        this.currentMenuItem = menuItem;

        // Ініціалізація репозиторію
        if (cartRepository == null) {
            cartRepository = new CartRepositoryImpl(DatabaseConnection.getStaticDataSource());
        }
        itemName.setText(menuItem.name());
        if (menuItem.description() != null && !menuItem.description().trim().isEmpty()) {
            itemDescription.setText(menuItem.description());
            itemDescription.setVisible(true);
        } else {
            itemDescription.setVisible(false);
        }

        portionSizeComboBox.getItems().clear();
        portionSizeComboBox.getItems().addAll(PortionSize.values());

        PortionSize defaultSize = menuItem.defaultPortionSize() != null ? menuItem.defaultPortionSize() : PortionSize.MEDIUM;
        portionSizeComboBox.setValue(defaultSize);

        // Налаштування відображення розмірів у ComboBox
        portionSizeComboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<PortionSize>() {
            @Override
            protected void updateItem(PortionSize item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        portionSizeComboBox.setButtonCell(new javafx.scene.control.ListCell<PortionSize>() {
            @Override
            protected void updateItem(PortionSize item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        // Оновлення ціни при зміні розміру порції
        portionSizeComboBox.setOnAction(event -> updatePrice());

        // Початкове встановлення ціни
        updatePrice();

        // Встановлення зображення
        if (menuItem.imagePath() != null && !menuItem.imagePath().trim().isEmpty()) {
            try {
                Image image = new Image(getClass().getResourceAsStream(menuItem.imagePath()));
                if (!image.isError()) {
                    itemImage.setImage(image);
                } else {
                    itemImage.setImage(null);
                }
            } catch (Exception e) {
                // Якщо зображення не вдалося завантажити, залишаємо порожнім
                itemImage.setImage(null);
            }
        } else {
            itemImage.setImage(null);
        }

        // Встановлення статусу доступності
        if (menuItem.isAvailable() != null && menuItem.isAvailable()) {
            availabilityLabel.setText("Доступно");
            availabilityLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 2 8 2 8; -fx-font-size: 10;");
            addToCartButton.setDisable(false);
            portionSizeComboBox.setDisable(false);
        } else {
            availabilityLabel.setText("Недоступно");
            availabilityLabel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 2 8 2 8; -fx-font-size: 10;");
            addToCartButton.setDisable(true);
            portionSizeComboBox.setDisable(true);
        }

        // Логіка додавання до кошика
        addToCartButton.setOnAction(event -> {
            addToCart();
        });
    }

    private void updatePrice() {
        if (currentMenuItem != null && portionSizeComboBox.getValue() != null) {
            PortionSize selectedSize = portionSizeComboBox.getValue();
            BigDecimal finalPrice = currentMenuItem.getPriceForSize(selectedSize);
            itemPrice.setText(String.format("%.2f грн", finalPrice));
        }
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
            System.err.println("Будь ласка, увійдіть у систему, щоб додати елемент до кошика.");
            return;
        }

        if (currentMenuItem == null) {
            System.err.println("Товар не вибрано");
            return;
        }

        if (portionSizeComboBox.getValue() == null) {
            System.err.println("Оберіть розмір порції");
            return;
        }

        int qty = getQuantity();
        if (qty <= 0) {
            System.err.println("Виберіть коректну кількість (більше 0).");
            return;
        }

        try {
            PortionSize selectedSize = portionSizeComboBox.getValue();
            BigDecimal pricePerItem = currentMenuItem.getPriceForSize(selectedSize);
            double subtotal = pricePerItem.doubleValue() * qty;

            Cart cartItem = new Cart(
                UUID.randomUUID(),
                currentUser.id(),
                currentMenuItem.itemId(),
                qty,
                subtotal,
                false
            );

            Cart savedCartItem = cartRepository.create(cartItem);
            if (savedCartItem != null) {
                System.out.println("Товар додано до кошика: " + currentMenuItem.name() + " (кількість: " + qty + ")");
            } else {
                System.err.println("Помилка додавання до кошика");
            }
        } catch (Exception e) {
            System.err.println("Помилка додавання до кошика: " + e.getMessage());
            e.printStackTrace();
        }
    }

}