package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.persistence.entity.Cart;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.entity.Order;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CartRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.OrderRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.UserRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.OrderStatus;
import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class OrderDetailsController implements Initializable {

    @FXML
    private Label orderIdLabel;
    @FXML
    private Label userIdLabel;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label createdAtLabel;
    @FXML
    private TextArea itemsTextArea;
    @FXML
    private Button closeButton;
    @FXML
    private Button cancelOrderButton;

    private final Order order;
    private final OrderRepositoryImpl orderRepository;
    private final CartRepositoryImpl cartRepository;
    private final MenuItemRepositoryImpl menuItemRepository;
    private final UserRepositoryImpl userRepository;
    private Stage stage;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public OrderDetailsController(Order order, OrderRepositoryImpl orderRepository, CartRepositoryImpl cartRepository, MenuItemRepositoryImpl menuItemRepository, UserRepositoryImpl userRepository) {
        this.order = order;
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (order == null) {
            itemsTextArea.setText("Помилка: замовлення не передано");
            return;
        }
        // Отримуємо ім'я користувача
        String username = "—";
        if (order.userId() != null) {
            try {
                User user = userRepository.findById(order.userId());
                username = user.username();
            } catch (EntityNotFoundException e) {
                username = "Користувач не знайдений";
            } catch (Exception e) {
                username = "Помилка завантаження";
            }
        }
        userIdLabel.setText("Користувач: " + username);

        totalPriceLabel.setText("Загальна сума: " + order.totalPrice() + " грн");
        statusLabel.setText("Статус: " + order.status().getLabel());
        createdAtLabel.setText("Дата створення: " + order.createdAt().format(DATE_TIME_FORMATTER));

        // Load cart items
        List<UUID> cartIds = orderRepository.findCartIdsByOrderId(order.orderId());
        if (cartIds.isEmpty()) {
            itemsTextArea.setText("Товари: немає даних");
        } else {
            StringBuilder items = new StringBuilder("Товари:\n");
            for (UUID cartId : cartIds) {
                try {
                    Cart cartItem = cartRepository.findById(cartId);
                    if (cartItem != null) {
                        MenuItem menuItem = menuItemRepository.findById(cartItem.itemId());
                        if (menuItem != null) {
                            items.append("  - Товар: ").append(menuItem.name()).append("\n")
                                .append("    Кількість: ").append(cartItem.quantity()).append("\n")
                                .append("    Ціна: ").append(String.format("%.2f", cartItem.subtotal())).append(" грн\n");
                        } else {
                            items.append("  - Товар: [видалено] (ID: ").append(cartItem.itemId()).append(")\n")
                                .append("    Кількість: ").append(cartItem.quantity()).append("\n")
                                .append("    Ціна: ").append(String.format("%.2f", cartItem.subtotal())).append(" грн\n");
                        }
                    } else {
                        items.append("  - Товар: [не знайдено] (ID: ").append(cartId).append(")\n");
                    }
                } catch (Exception e) {
                    items.append("  - Помилка при завантаженні товару (ID: ").append(cartId).append("): ").append(e.getMessage()).append("\n");
                }
            }
            itemsTextArea.setText(items.toString());
        }

        closeButton.setOnAction(event -> closeWindow());

        // Налаштування кнопки скасування
        if (order.status() == OrderStatus.PENDING) {
            cancelOrderButton.setDisable(false);
            cancelOrderButton.setOnAction(event -> cancelOrder());
        } else {
            cancelOrderButton.setDisable(true);
        }
    }

    @FXML
    private void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void cancelOrder() {
        if (order.status() != OrderStatus.PENDING) {
            AlertController.showAlert("Скасування можливо лише для замовлень у статусі 'Очікує підтвердження'");
            return;
        }

        try {
            Order updatedOrder = new Order(
                order.orderId(),
                order.userId(),
                order.totalPrice(),
                OrderStatus.CANCELLED,
                order.createdAt()
            );

            Order result = orderRepository.update(updatedOrder);
            if (result != null) {
                AlertController.showAlert("Замовлення успішно скасовано");
                closeWindow();
            } else {
                AlertController.showAlert("Помилка при скасуванні замовлення");
            }
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Замовлення не знайдено");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при скасуванні замовлення: " + e.getMessage());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}