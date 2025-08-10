package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Cart;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PortionSize;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CartRepositoryImpl;
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
    private Label itemDescription;

    @FXML
    private ComboBox<PortionSize> portionSizeComboBox;

    @FXML
    private Text itemPrice;

    @FXML
    private Button addToCartButton;
    @FXML
    private Spinner<Integer> quantity;

    private MenuItem currentMenuItem;
    private CartRepositoryImpl cartRepository;

    public MenuItemCardController() {
        this.cartRepository = new CartRepositoryImpl(DatabaseConnection.getInstance().getDataSource());
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

        if (cartRepository == null) {
            cartRepository = new CartRepositoryImpl(DatabaseConnection.getInstance().getDataSource());
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

        portionSizeComboBox.setOnAction(event -> updatePrice());

        updatePrice();

        setItemImage(menuItem.imagePath(), menuItem.name());

        if (menuItem.isAvailable() != null && menuItem.isAvailable()) {
            itemName.getParent().getStyleClass().remove("unavailable");
            addToCartButton.setDisable(false);
            portionSizeComboBox.setDisable(false);
            quantity.setDisable(false);
        } else {
            if (!itemName.getParent().getStyleClass().contains("unavailable")) {
                itemName.getParent().getStyleClass().add("unavailable");
            }
            addToCartButton.setDisable(true);
            portionSizeComboBox.setDisable(true);
            quantity.setDisable(true);
        }

        addToCartButton.setOnAction(event -> {
            addToCart();
        });
    }

    private void setItemImage(String imagePath, String itemName) {
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                java.net.URL resourceUrl = getClass().getResource(imagePath);
                if (resourceUrl != null) {
                    Image image = new Image(resourceUrl.toExternalForm(), true);
                    itemImage.setImage(image);

                    image.errorProperty().addListener((obs, oldError, newError) -> {
                        if (newError) {
                            System.err.println("Помилка фонового завантаження зображення для страви: " + itemName + " з шляху: " + imagePath);
                            setDefaultItemImage(itemName);
                        }
                    });
                } else {
                    java.io.File imageFile = new java.io.File(imagePath);
                    if (imageFile.exists() && imageFile.isFile()) {
                        Image image = new Image(imageFile.toURI().toString(), true);
                        itemImage.setImage(image);

                        image.errorProperty().addListener((obs, oldError, newError) -> {
                            if (newError) {
                                System.err.println("Помилка фонового завантаження зображення файлу для страви: " + itemName + " з шляху: " + imagePath);
                                setDefaultItemImage(itemName);
                            }
                        });
                    } else {
                        System.err.println("Зображення не знайдено ні як ресурс, ні як файл: " + imagePath + " для страви: " + itemName);
                        setDefaultItemImage(itemName);
                    }
                }
            } catch (Exception e) {
                System.err.println("Помилка створення шляху до зображення: " + itemName + " з шляху: " + imagePath + ": " + e.getMessage());
                setDefaultItemImage(itemName);
            }
        } else {
            setDefaultItemImage(itemName);
        }
    }

    private void setDefaultItemImage(String itemName) {
        try {
            String defaultImagePath = "/images/fast-food.jpg";
            // Перевіряємо, чи існує ресурс перед використанням
            java.net.URL resourceUrl = getClass().getResource(defaultImagePath);
            if (resourceUrl != null) {
                Image defaultImage = new Image(resourceUrl.toExternalForm(), true);
                itemImage.setImage(defaultImage);

                defaultImage.errorProperty().addListener((obs, oldError, newError) -> {
                    if (newError) {
                        System.err.println("Помилка фонового завантаження дефолтного зображення для страви: " + itemName);
                        itemImage.setImage(null);
                    }
                });
            } else {
                System.err.println("Дефолтне зображення не знайдено: " + defaultImagePath + " для страви: " + itemName);
                itemImage.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Помилка створення шляху до дефолтного зображення: " + itemName + ": " + e.getMessage());
            itemImage.setImage(null);
        }
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
            AlertController.showAlert("Будь ласка, увійдіть у систему, щоб додати елемент до кошика");
            return;
        }

        if (currentMenuItem == null) {
            AlertController.showAlert("Товар не вибрано");
            return;
        }

        if (portionSizeComboBox.getValue() == null) {
            AlertController.showAlert("Оберіть розмір порції");
            return;
        }

        int qty = getQuantity();
        if (qty <= 0) {
            AlertController.showAlert("Виберіть коректну кількість (більше 0)");
            return;
        }

        if (cartRepository.existsByUserIdAndItemId(currentUser.id(), currentMenuItem.itemId())) {
            AlertController.showAlert("Цей товар вже є у вашому кошику");
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
                AlertController.showAlert("Товар додано до кошика");
            } else {
                AlertController.showAlert("Помилка додавання до кошика");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка додавання до кошика: " + e.getMessage());
        }
    }
}