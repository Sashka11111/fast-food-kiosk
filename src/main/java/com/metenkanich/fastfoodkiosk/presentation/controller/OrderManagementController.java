package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Order;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.OrderStatus;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.OrderRepositoryImpl;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;

public class OrderManagementController {

    @FXML
    private TextField searchTextField;
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, String> orderDateColumn;
    @FXML
    private TableColumn<Order, Double> totalAmountColumn;
    @FXML
    private TableColumn<Order, OrderStatus> statusColumn;
    @FXML
    private TableColumn<Order, String> deliveryAddressColumn;
    @FXML
    private TableColumn<Order, String> phoneNumberColumn;
    @FXML
    private TableColumn<Order, Button> actionColumn;

    private OrderRepositoryImpl orderRepository;
    private ObservableList<Order> ordersList;

    public OrderManagementController() {
        this.orderRepository = new OrderRepositoryImpl(new DatabaseConnection().getDataSource());
        this.ordersList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        orderDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().orderDate().toString()));
        totalAmountColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().totalAmount()));
        statusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().status()));
        statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(OrderStatus.values())));
        statusColumn.setOnEditCommit(event -> {
            Order order = event.getRowValue();
            OrderStatus newStatus = event.getNewValue();
            updateOrderStatus(order, newStatus);
        });
        deliveryAddressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().deliveryAddress()));
        phoneNumberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().phoneNumber()));
        actionColumn.setCellValueFactory(cellData -> {
            Button deleteButton = new Button("Видалити");
            deleteButton.getStyleClass().add("cancel-button");
            deleteButton.setOnAction(e -> deleteOrder(cellData.getValue()));
            return new SimpleObjectProperty<>(deleteButton);
        });

        // Увімкнення редагування таблиці
        orderTable.setEditable(true);

        // Завантаження початкового списку замовлень
        loadOrders();

        // Обробка пошуку
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchOrders(newValue));
    }

    private void loadOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            ordersList.setAll(orders);
            orderTable.setItems(ordersList);
            if (ordersList.isEmpty()) {
                orderTable.setPlaceholder(new Label("Немає замовлень"));
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні замовлень: " + e.getMessage());
        }
    }

    private void searchOrders(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadOrders();
            return;
        }

        List<Order> filteredOrders = ordersList.stream()
            .filter(order -> order.orderId().toString().toLowerCase().contains(searchText.toLowerCase()) ||
                order.deliveryAddress().toLowerCase().contains(searchText.toLowerCase()) ||
                order.phoneNumber().toLowerCase().contains(searchText.toLowerCase()) ||
                (order.notes() != null && order.notes().toLowerCase().contains(searchText.toLowerCase())))
            .toList();
        if (filteredOrders.isEmpty()) {
            orderTable.setPlaceholder(new Label("Немає замовлень"));
        } else {
            orderTable.setPlaceholder(null);
        }
        orderTable.setItems(FXCollections.observableArrayList(filteredOrders));
    }

    private void updateOrderStatus(Order order, OrderStatus newStatus) {
        try {
            Order updatedOrder = new Order(
                order.orderId(),
                order.orderDate(),
                order.totalAmount(),
                newStatus,
                order.deliveryAddress(),
                order.notes(),
                order.phoneNumber()
            );
            // Отримуємо пов'язані cartIds
            List<String> cartIds = orderRepository.findCartIdsByOrderId(order.orderId());
            Order savedOrder = orderRepository.update(updatedOrder, cartIds);
            if (savedOrder != null) {
                loadOrders();
                AlertController.showAlert("Статус замовлення успішно оновлено!");
            } else {
                AlertController.showAlert("Не вдалося оновити статус замовлення.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при оновленні статусу: " + e.getMessage());
        }
    }

    private void deleteOrder(Order order) {
        try {
            orderRepository.deleteById(order.orderId());
            loadOrders();
            AlertController.showAlert("Замовлення успішно видалено!");
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Замовлення не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при видаленні замовлення: " + e.getMessage());
        }
    }
}