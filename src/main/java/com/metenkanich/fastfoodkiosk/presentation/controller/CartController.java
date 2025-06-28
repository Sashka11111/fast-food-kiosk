//package com.metenkanich.fastfoodkiosk.presentation.controller;
//
//import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
//import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
//import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
//import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
//import com.metenkanich.fastfoodkiosk.persistence.entity.Order;
//import com.metenkanich.fastfoodkiosk.persistence.entity.User;
//import com.metenkanich.fastfoodkiosk.persistence.entity.enums.OrderStatus;
//import com.metenkanich.fastfoodkiosk.persistence.repository.contract.MenuItemRepository;
//import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
//import com.metenkanich.fastfoodkiosk.persistence.repository.impl.PaymentRepositoryImpl;
//import com.metenkanich.fastfoodkiosk.persistence.repository.impl.OrderRepositoryImpl;
//import com.metenkanich.fastfoodkiosk.presentation.validation.CartValidator;
//import com.metenkanich.fastfoodkiosk.presentation.validation.OrderValidator;
//import com.metenkanich.fastfoodkiosk.presentation.validation.ValidationResult;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.ScrollPane;
//import javafx.scene.control.TextArea;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.AnchorPane;
//import javafx.scene.layout.ColumnConstraints;
//import javafx.scene.layout.GridPane;
//
//public class CartController {
//
//    @FXML
//    private Label cartLabel;
//
//    @FXML
//    private ScrollPane cartScrollPane;
//
//    @FXML
//    private GridPane cartGridPane;
//
//    @FXML
//    private Label totalAmountLabel;
//
//    @FXML
//    private TextArea deliveryAddressArea;
//
//    @FXML
//    private TextArea notesArea;
//
//    @FXML
//    private TextField phoneNumberField;
//
//    @FXML
//    private Button placeOrderButton;
//
//    private MenuItemRepositoryImpl cartRepository;
//    private MenuItemRepository menuItemRepository;
//    private OrderRepositoryImpl orderRepository;
//    private List<Cart> cartItems;
//
//    public CartController() {
//        this.cartRepository = new MenuItemRepositoryImpl(new DatabaseConnection().getDataSource());
//        this.menuItemRepository = new PaymentRepositoryImpl(new DatabaseConnection().getDataSource());
//        this.orderRepository = new OrderRepositoryImpl(new DatabaseConnection().getDataSource());
//    }
//
//    @FXML
//    public void initialize() {
//        if (placeOrderButton != null) {
//            placeOrderButton.setOnAction(event -> placeOrder());
//        } else {
//            System.err.println("placeOrderButton is null in CartController.initialize");
//        }
//        loadCartItems();
//    }
//
//    public void loadCartItems() {
//        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            cartLabel.setText("Будь ласка, увійдіть у систему");
//            updateTotalAmount(0.0);
//            return;
//        }
//
//        cartItems = cartRepository.findUnorderedByUserId(currentUser.id());
//        cartGridPane.getChildren().clear();
//
//        if (cartItems == null || cartItems.isEmpty()) {
//            cartLabel.setText("Наразі Ваш кошик порожній");
//            cartScrollPane.setVisible(false);
//            updateTotalAmount(0.0);
//            return;
//        } else {
//            cartLabel.setText("");
//            cartScrollPane.setVisible(true);
//        }
//
//        int column = 0;
//        int row = 0;
//        int cardsPerRow = 3;
//        double totalAmount = 0.0;
//
//        cartGridPane.getColumnConstraints().clear();
//        cartGridPane.setHgap(10);
//        cartGridPane.setVgap(10);
//        for (int i = 0; i < cardsPerRow; i++) {
//            ColumnConstraints columnConstraints = new ColumnConstraints();
//            columnConstraints.setPercentWidth(100.0 / cardsPerRow);
//            cartGridPane.getColumnConstraints().add(columnConstraints);
//        }
//
//        for (Cart cartItem : cartItems) {
//            if (cartItem == null) {
//                cartLabel.setText("Помилка: Один із елементів кошика має невалідний формат.");
//                continue;
//            }
//
//            MenuItem menuItem;
//            try {
//                menuItem = menuItemRepository.findById(cartItem.itemId());
//                if (menuItem == null) {
//                    cartLabel.setText("Помилка: Один із елементів кошика не знайдено.");
//                    continue;
//                }
//            } catch (EntityNotFoundException e) {
//                continue;
//            }
//
//            totalAmount += cartItem.subtotal();
//
//            try {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cartItemCard.fxml"));
//                AnchorPane card = loader.load();
//                CartItemCard controller = loader.getController();
//                if (controller != null) {
//                    controller.setCartItem(cartItem, menuItem);
//                    controller.setParentController(this);
//                    cartGridPane.add(card, column, row);
//                    column++;
//                    if (column >= cardsPerRow) {
//                        column = 0;
//                        row++;
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                cartLabel.setText("Помилка завантаження картки: " + e.getMessage());
//            }
//        }
//
//        updateTotalAmount(totalAmount);
//    }
//
//    private void updateTotalAmount(double totalAmount) {
//        if (totalAmountLabel != null) {
//            totalAmountLabel.setText(String.format("Загальна сума: %.2f грн", totalAmount));
//        }
//    }
//
//    private void placeOrder() {
//        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            AlertController.showAlert("Будь ласка, увійдіть у систему");
//            return;
//        }
//
//        if (cartItems == null || cartItems.isEmpty()) {
//            cartLabel.setText("Наразі Ваш кошик порожній");
//            return;
//        }
//
//        // Валідація елементів кошика
//        for (Cart cartItem : cartItems) {
//            ValidationResult cartValidationResult = CartValidator.isCartValid(cartItem, true, cartRepository);
//            if (!cartValidationResult.isValid()) {
//                AlertController.showAlert("Помилки валідації елемента кошика:\n" + cartValidationResult.getErrorMessage());
//                return;
//            }
//        }
//
//        String deliveryAddress = deliveryAddressArea.getText().trim();
//        if (deliveryAddress.isEmpty()) {
//            AlertController.showAlert("Введіть адресу доставки");
//            return;
//        }
//
//        String phone = phoneNumberField.getText().trim();
//        if (phone.isEmpty()) {
//            AlertController.showAlert("Введіть номер телефону");
//            return;
//        }
//
//        ValidationResult phoneValidationResult = OrderValidator.isPhoneNumberValid(phone);
//        if (!phoneValidationResult.isValid()) {
//            AlertController.showAlert("Помилки валідації номера телефону:\n" + phoneValidationResult.getErrorMessage());
//            return;
//        }
//
//        String notes = notesArea.getText().trim();
//        double totalAmount = cartItems.stream().mapToDouble(Cart::subtotal).sum();
//        List<String> cartIds = cartItems.stream()
//            .map(cart -> cart.cartId().toString())
//            .collect(Collectors.toList());
//
//        Order order = new Order(
//            UUID.randomUUID(),
//            LocalDateTime.now(),
//            totalAmount,
//            OrderStatus.PENDING,
//            deliveryAddress,
//            notes.isEmpty() ? null : notes,
//            phone
//        );
//
//        ValidationResult orderValidationResult = OrderValidator.isOrderValid(order, false, orderRepository);
//        if (!orderValidationResult.isValid()) {
//            AlertController.showAlert("Помилки валідації замовлення:\n" + orderValidationResult.getErrorMessage());
//            return;
//        }
//
//        Order createdOrder = orderRepository.create(order, cartIds);
//        if (createdOrder != null) {
//            cartRepository.markCartItemsAsOrdered(cartIds);
//
//            deliveryAddressArea.clear();
//            notesArea.clear();
//            phoneNumberField.clear();
//            AlertController.showAlert("Замовлення успішно оформлено!");
//            loadCartItems();
//        } else {
//            cartLabel.setText("Помилка при оформленні замовлення");
//        }
//    }
//}