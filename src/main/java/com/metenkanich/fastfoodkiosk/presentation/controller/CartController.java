package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Cart;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.entity.Order;
import com.metenkanich.fastfoodkiosk.persistence.entity.Payment;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.OrderStatus;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PaymentMethod;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PaymentStatus;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.MenuItemRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CartRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.OrderRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.PaymentRepositoryImpl;
import com.metenkanich.fastfoodkiosk.presentation.validation.CartValidator;
import com.metenkanich.fastfoodkiosk.presentation.validation.OrderValidator;
import com.metenkanich.fastfoodkiosk.presentation.validation.ValidationResult;
import java.time.LocalDateTime;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;

public class CartController {

    @FXML
    private Label cartLabel;

    @FXML
    private ScrollPane cartScrollPane;

    @FXML
    private GridPane cartGridPane;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Button placeOrderButton;

    @FXML
    private ComboBox<PaymentMethod> paymentMethodComboBox;

    private CartRepositoryImpl cartRepository;
    private MenuItemRepository menuItemRepository;
    private OrderRepositoryImpl orderRepository;
    private PaymentRepositoryImpl paymentRepository;
    private List<Cart> cartItems;

    public CartController() {
        DataSource dataSource = DatabaseConnection.getInstance().getDataSource();
        this.cartRepository = new CartRepositoryImpl(dataSource);
        this.menuItemRepository = new MenuItemRepositoryImpl(dataSource);
        this.orderRepository = new OrderRepositoryImpl(dataSource);
        this.paymentRepository = new PaymentRepositoryImpl(dataSource);
    }

    @FXML
    public void initialize() {
        if (placeOrderButton != null) {
            placeOrderButton.setOnAction(event -> placeOrder());
        } else {
            System.err.println("placeOrderButton is null in CartController.initialize");
        }

        if (paymentMethodComboBox != null) {
            paymentMethodComboBox.getItems().addAll(PaymentMethod.values());
            paymentMethodComboBox.setValue(PaymentMethod.CASH); // Значення за замовчуванням
        }

        loadCartItems();
    }

    public void loadCartItems() {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser == null) {
            cartLabel.setText("Будь ласка, увійдіть у систему");
            updateTotalAmount(0.0);
            return;
        }

        cartItems = cartRepository.findUnorderedByUserId(currentUser.id());
        cartGridPane.getChildren().clear();

        if (cartItems == null || cartItems.isEmpty()) {
            cartLabel.setText("Наразі Ваш кошик порожній");
            cartScrollPane.setVisible(false);
            updateTotalAmount(0.0);
            return;
        } else {
            cartLabel.setText("");
            cartScrollPane.setVisible(true);
        }

        int column = 0;
        int row = 0;
        int cardsPerRow = 1;
        double totalAmount = 0.0;

        cartGridPane.getColumnConstraints().clear();
        cartGridPane.setHgap(10);
        cartGridPane.setVgap(10);
        for (int i = 0; i < cardsPerRow; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / cardsPerRow);
            cartGridPane.getColumnConstraints().add(columnConstraints);
        }

        for (Cart cartItem : cartItems) {
            if (cartItem == null) {
                cartLabel.setText("Помилка: Один із елементів кошика має невалідний формат.");
                continue;
            }

            MenuItem menuItem;
            try {
                menuItem = menuItemRepository.findById(cartItem.itemId());
                if (menuItem == null) {
                    cartLabel.setText("Помилка: Один із елементів кошика не знайдено.");
                    continue;
                }
            } catch (EntityNotFoundException e) {
                continue;
            }

            totalAmount += cartItem.subtotal();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/view/cart_item_card.fxml"));
                AnchorPane card = loader.load();
                CartItemCard controller = loader.getController();
                if (controller != null) {
                    controller.setCartItem(cartItem, menuItem);
                    controller.setParentController(this);
                    cartGridPane.add(card, column, row);
                    column++;
                    if (column >= cardsPerRow) {
                        column = 0;
                        row++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                cartLabel.setText("Помилка завантаження картки: " + e.getMessage());
            }
        }

        updateTotalAmount(totalAmount);
    }

    private void updateTotalAmount(double totalAmount) {
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(String.format("Загальна сума: %.2f грн", totalAmount));
        }
    }

    private void placeOrder() {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertController.showAlert("Будь ласка, увійдіть у систему");
            return;
        }

        if (cartItems == null || cartItems.isEmpty()) {
            AlertController.showAlert("Кошик порожній. Додайте страви до кошика перед оформленням замовлення.");
            return;
        }

        // Валідація елементів кошика
        for (Cart cartItem : cartItems) {
            ValidationResult cartValidationResult = CartValidator.isCartValid(cartItem, true, cartRepository);
            if (!cartValidationResult.isValid()) {
                AlertController.showAlert("Помилки валідації елемента кошика:\n" + cartValidationResult.getErrorMessage());
                return;
            }
        }

        PaymentMethod selectedPaymentMethod = paymentMethodComboBox.getValue();
        if (selectedPaymentMethod == null) {
            AlertController.showAlert("Оберіть спосіб оплати\n\nБудь ласка, виберіть спосіб оплати зі списку.");
            return;
        }

        BigDecimal totalPrice = cartItems.stream()
            .map(Cart::subtotal)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<UUID> cartIds = cartItems.stream()
            .map(Cart::cartId)
            .collect(Collectors.toList());

        Order order = new Order(
            null,
            currentUser.id(),
            totalPrice,
            OrderStatus.PENDING,
            LocalDateTime.now()
        );

        ValidationResult orderValidationResult = OrderValidator.isOrderValid(order, false);
        if (!orderValidationResult.isValid()) {
            AlertController.showAlert("Помилки валідації замовлення:\n" + orderValidationResult.getErrorMessage());
            return;
        }

        Order createdOrder = orderRepository.create(order);
        if (createdOrder != null) {
            boolean allPaymentsCreated = true;
            for (Cart cartItem : cartItems) {
                Payment payment = new Payment(
                    null,
                    cartItem.cartId(),
                    selectedPaymentMethod,
                    PaymentStatus.PENDING,
                    LocalDateTime.now()
                );

                Payment createdPayment = paymentRepository.create(payment);
                if (createdPayment == null) {
                    allPaymentsCreated = false;
                    break;
                }
            }

            if (allPaymentsCreated) {
                // Позначаємо елементи кошика як замовлені
                cartRepository.markCartItemsAsOrdered(cartIds);
                paymentMethodComboBox.setValue(PaymentMethod.CASH);

                // Вибір повідомлення залежно від способу оплати
                String successMessage = selectedPaymentMethod == PaymentMethod.CARD
                    ? "Замовлення успішно оформлено!\nБудь ласка, прикладіть картку до терміналу."
                    : "Замовлення успішно оформлено!\nБудь ласка, підійдіть до каси для оплати.";
                AlertController.showAlert(successMessage);

                loadCartItems();
            } else {
                AlertController.showAlert("Помилка при створенні оплати");
            }
        } else {
            AlertController.showAlert("Помилка при створенні замовлення");
        }
    }
}