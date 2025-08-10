package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Order;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.OrderStatus;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CartRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.OrderRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.UserRepositoryImpl;
import com.metenkanich.fastfoodkiosk.presentation.validation.OrderValidator;
import com.metenkanich.fastfoodkiosk.presentation.validation.ValidationResult;
import java.net.URL;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OrdersController {

    @FXML
    private TextField searchTextField;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TableView<Order> ordersTableView;
    @FXML
    private TableColumn<Order, String> userIdColumn;
    @FXML
    private TableColumn<Order, String> totalPriceColumn;
    @FXML
    private TableColumn<Order, String> statusColumn;
    @FXML
    private TableColumn<Order, String> createdAtColumn;
    @FXML
    private TableColumn<Order, Void> detailsColumn;
    @FXML
    private ComboBox<OrderStatus> editStatusComboBox;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearFieldsButton;

    private final OrderRepositoryImpl orderRepository;
    private final CartRepositoryImpl cartRepository;
    private final MenuItemRepositoryImpl menuItemRepository;
    private final UserRepositoryImpl userRepository;
    private final ObservableList<Order> ordersList;
    private Order selectedOrder;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public OrdersController() {
        this.orderRepository = new OrderRepositoryImpl(DatabaseConnection.getInstance().getDataSource());
        this.cartRepository = new CartRepositoryImpl(DatabaseConnection.getInstance().getDataSource());
        this.menuItemRepository = new MenuItemRepositoryImpl(DatabaseConnection.getInstance().getDataSource());
        this.userRepository = new UserRepositoryImpl(DatabaseConnection.getInstance().getDataSource());
        this.ordersList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupStatusComboBox();
        setupTableColumns();
        setupEditStatusComboBox();
        loadOrders();

        statusComboBox.setOnAction(event -> loadOrders());
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> loadOrders());

        editButton.setOnAction(event -> editOrder());
        deleteButton.setOnAction(event -> deleteOrder());
        clearFieldsButton.setOnAction(event -> clearFields());

        ordersTableView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedOrder = newValue;
                populateFields(newValue);
                editButton.setDisable(newValue == null);
                deleteButton.setDisable(newValue == null);
            });
    }

    private void setupStatusComboBox() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList("Усі статуси");
        statusOptions.addAll(Arrays.stream(OrderStatus.values())
            .map(OrderStatus::getLabel)
            .collect(Collectors.toList()));
        statusComboBox.setItems(statusOptions);
        statusComboBox.setValue("Усі статуси");
    }

    private void setupEditStatusComboBox() {
        editStatusComboBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));
    }

    private void setupTableColumns() {
        userIdColumn.setCellValueFactory(cellData -> {
            try {
                User user = userRepository.findById(cellData.getValue().userId());
                return new SimpleStringProperty(user.username());
            } catch (EntityNotFoundException e) {
                return new SimpleStringProperty("Користувач не знайдений");
            } catch (Exception e) {
                return new SimpleStringProperty("Помилка завантаження");
            }
        });
        totalPriceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().totalPrice().toString()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().status().getLabel()));
        createdAtColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().createdAt().format(DATE_TIME_FORMATTER)));

        detailsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewDetailsButton = new Button("Деталі");
            private final Button cancelButton = new Button("Скасувати");
            private final VBox buttonBox = new VBox(viewDetailsButton, cancelButton);

            {
                viewDetailsButton.getStyleClass().add("details-button");
                cancelButton.getStyleClass().add("cancel-button");

                viewDetailsButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    viewOrderDetails(order);
                });

                cancelButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    cancelOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    cancelButton.setDisable(order.status() != OrderStatus.PENDING);
                    setGraphic(buttonBox);
                }
            }
        });
    }

    private void loadOrders() {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertController.showAlert("Будь ласка, увійдіть у систему");
            ordersList.clear();
            ordersTableView.setPlaceholder(new Label("Наразі таких замовлень немає"));
            return;
        }

        List<Order> orders;
        String selectedStatus = statusComboBox.getValue();
        if (selectedStatus == null || selectedStatus.equals("Усі статуси")) {
            orders = orderRepository.findAll();
        } else {
            OrderStatus status = Arrays.stream(OrderStatus.values())
                .filter(s -> s.getLabel().equals(selectedStatus))
                .findFirst()
                .orElse(null);
            orders = status != null
                ? orderRepository.findAll().stream()
                .filter(order -> order.status().equals(status))
                .collect(Collectors.toList())
                : orderRepository.findAll();
        }

        String searchText = searchTextField.getText().trim().toLowerCase();
        if (!searchText.isEmpty()) {
            orders = orders.stream()
                .filter(order -> {
                    String username;
                    try {
                        User user = userRepository.findById(order.userId());
                        username = user.username().toLowerCase();
                    } catch (Exception e) {
                        username = "невідомий користувач";
                    }
                    String totalPrice = order.totalPrice().toString().toLowerCase();
                    String status = order.status().getLabel().toLowerCase();
                    String createdAt = order.createdAt().format(DATE_TIME_FORMATTER).toLowerCase();
                    return username.contains(searchText) ||
                        totalPrice.contains(searchText) ||
                        status.contains(searchText) ||
                        createdAt.contains(searchText);
                })
                .toList();
        }

        ordersList.clear();
        if (orders.isEmpty()) {
            ordersTableView.setPlaceholder(new Label("Наразі таких замовлень немає"));
        } else {
            ordersList.addAll(orders);
        }

        ordersTableView.setItems(ordersList);
    }

    private void editOrder() {
        if (selectedOrder == null) {
            AlertController.showAlert("Будь ласка, виберіть замовлення для редагування");
            return;
        }

        OrderStatus newStatus = editStatusComboBox.getValue();
        if (newStatus == null) {
            AlertController.showAlert("Будь ласка, виберіть новий статус для замовлення");
            return;
        }

        if (newStatus.equals(selectedOrder.status())) {
            AlertController.showAlert("Новий статус збігається з поточним. Зміни не потрібні.");
            return;
        }

        try {
            Order updatedOrder = new Order(
                selectedOrder.orderId(),
                selectedOrder.userId(),
                selectedOrder.totalPrice(),
                newStatus,
                selectedOrder.createdAt()
            );

            Order savedOrder = orderRepository.update(updatedOrder);
            if (savedOrder != null) {
                loadOrders();
                clearFields();
                AlertController.showAlert("Статус замовлення успішно оновлено!");
            } else {
                AlertController.showAlert("Не вдалося оновити статус замовлення. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при оновленні статусу замовлення: " + e.getMessage());
        }
    }

    private void deleteOrder() {
        if (selectedOrder == null) {
            AlertController.showAlert("Будь ласка, виберіть замовлення для видалення");
            return;
        }

        try {
            orderRepository.deleteById(selectedOrder.orderId());
            loadOrders();
            clearFields();
            AlertController.showAlert("Замовлення успішно видалено!");
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Замовлення не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при видаленні замовлення: " + e.getMessage());
        }
    }

    private void clearFields() {
        editStatusComboBox.getSelectionModel().clearSelection();
        selectedOrder = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void populateFields(Order order) {
        if (order == null) {
            clearFields();
            return;
        }
        editStatusComboBox.setValue(order.status());;
    }

    private void cancelOrder(Order order) {
        if (!order.status().equals(OrderStatus.PENDING)) {
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
            List<UUID> cartIds = orderRepository.findCartIdsByOrderId(order.orderId());
            Order result = orderRepository.update(updatedOrder);

            if (result != null) {
                AlertController.showAlert("Замовлення успішно скасовано");
                loadOrders();
            } else {
                AlertController.showAlert("Помилка при скасуванні замовлення");
            }
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Замовлення не знайдено");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при скасуванні замовлення: " + e.getMessage());
        }
    }

    private void viewOrderDetails(Order order) {
        try {
            URL resource = getClass().getResource("/view/order_details.fxml");
            if (resource == null) {
                throw new IOException("Resource /view/order_details.fxml not found in classpath");
            }
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(param -> new OrderDetailsController(order, orderRepository, cartRepository, menuItemRepository, userRepository));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Деталі замовлення");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);

            OrderDetailsController controller = loader.getController();
            controller.setStage(stage);

            stage.showAndWait();
        } catch (IOException e) {
            AlertController.showAlert("Помилка при завантаженні деталей замовлення");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при отриманні деталей замовлення: " + e.getMessage());
        }
    }
}