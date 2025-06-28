package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.entity.Order;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.PaymentRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.OrderRepositoryImpl;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class OrderDetailsController implements Initializable {

    @FXML
    private Label orderDateLabel;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label deliveryAddressLabel;
    @FXML
    private Label phoneNumberLabel;
    @FXML
    private Label notesLabel;
    @FXML
    private TextArea itemsTextArea;
    @FXML
    private Button closeButton;

    private final Order order;
    private final OrderRepositoryImpl orderRepository;
    private final MenuItemRepositoryImpl cartRepository;
    private final PaymentRepositoryImpl menuItemRepository;
    private Stage stage;

    public OrderDetailsController(Order order, OrderRepositoryImpl orderRepository, MenuItemRepositoryImpl cartRepository, PaymentRepositoryImpl menuItemRepository) {
        this.order = order;
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (order == null) {
            itemsTextArea.setText("Помилка: замовлення не передано");
            return;
        }

        orderDateLabel.setText("Дата замовлення: " + order.orderDate());
        totalAmountLabel.setText("Загальна сума: " + order.totalAmount() + " грн");
        statusLabel.setText("Статус: " + order.status().getUkrainianName());
        deliveryAddressLabel.setText("Адреса доставки: " + order.deliveryAddress());
        phoneNumberLabel.setText("Номер телефону: " + (order.phoneNumber() != null ? order.phoneNumber() : "—"));
        notesLabel.setText("Примітки: " + (order.notes() != null ? order.notes() : "—"));

        List<String> cartIds = orderRepository.findCartIdsByOrderId(order.orderId());
        if (cartIds.isEmpty()) {
            itemsTextArea.setText("Товари: немає даних");
        } else {
            StringBuilder items = new StringBuilder("Товари:\n");
            for (String cartIdStr : cartIds) {
                try {
                    UUID cartId = UUID.fromString(cartIdStr);
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
                        System.out.println("Товар не знайдено");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Товар не знайдено");
                } catch (Exception e) {
                    System.out.println("Товар не знайдено");
                }
            }
            itemsTextArea.setText(items.toString());
        }

        closeButton.setOnAction(event -> closeWindow());
    }

    @FXML
    private void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}