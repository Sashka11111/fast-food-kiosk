package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Order;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.OrderStatus;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.PaymentRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.OrderRepositoryImpl;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OrdersController {

    @FXML
    private TextField searchTextField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<Order> ordersTableView;

    @FXML
    private TableColumn<Order, String> phoneColumn;

    @FXML
    private TableColumn<Order, String> orderDateColumn;

    @FXML
    private TableColumn<Order, Double> totalAmountColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private TableColumn<Order, String> deliveryAddressColumn;

    @FXML
    private TableColumn<Order, String> notesColumn;

    @FXML
    private TableColumn<Order, Void> detailsColumn;

    private final OrderRepositoryImpl orderRepository;
    private final MenuItemRepositoryImpl cartRepository;
    private final PaymentRepositoryImpl menuItemRepository;
    private final ObservableList<Order> ordersList;

    public OrdersController() {
        this.orderRepository = new OrderRepositoryImpl(new DatabaseConnection().getDataSource());
        this.cartRepository = new MenuItemRepositoryImpl(new DatabaseConnection().getDataSource());
        this.menuItemRepository = new PaymentRepositoryImpl(new DatabaseConnection().getDataSource());
        this.ordersList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupStatusComboBox();
        setupTableColumns();
        loadOrders();

        statusComboBox.setOnAction(event -> loadOrders());
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> loadOrders());
    }

    private void setupStatusComboBox() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList("Усі статуси");
        statusOptions.addAll(Arrays.stream(OrderStatus.values())
            .map(OrderStatus::getUkrainianName)
            .collect(Collectors.toList()));
        statusComboBox.setItems(statusOptions);
        statusComboBox.setValue("Усі статуси");
    }

    private void setupTableColumns() {
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().phoneNumber() != null ? cellData.getValue().phoneNumber() : "—"));
        orderDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().orderDate().toString()));
        totalAmountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().totalAmount()).asObject());
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().status().getUkrainianName()));
        deliveryAddressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().deliveryAddress()));
        notesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().notes() != null ? cellData.getValue().notes() : "—"));

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
                    if (order.status() != OrderStatus.PENDING) {
                        cancelButton.setDisable(true);
                    } else {
                        cancelButton.setDisable(false);
                    }
                    setGraphic(buttonBox);
                }
            }
        });
    }

    private void loadOrders() {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser == null) {
            messageLabel.setText("Будь ласка, увійдіть у систему");
            ordersList.clear();
            return;
        }

        List<Order> orders;
        String selectedStatus = statusComboBox.getValue();
        if (selectedStatus == null || selectedStatus.equals("Усі статуси")) {
            orders = orderRepository.findByUserId(currentUser.id());
        } else {
            OrderStatus status = Arrays.stream(OrderStatus.values())
                .filter(s -> s.getUkrainianName().equals(selectedStatus))
                .findFirst()
                .orElse(null);

            if (status != null) {
                List<Order> allOrders = orderRepository.findByUserId(currentUser.id());
                orders = allOrders.stream()
                    .filter(order -> order.status().equals(status))
                    .collect(Collectors.toList());
            } else {
                orders = orderRepository.findByUserId(currentUser.id());
            }
        }

        String searchId = searchTextField.getText().trim().toLowerCase();
        if (!searchId.isEmpty()) {
            orders = orders.stream()
                .filter(order -> {
                    String phone = order.phoneNumber() != null ? order.phoneNumber().toLowerCase() : "";
                    String date = order.orderDate().toString().toLowerCase();
                    String amount = String.valueOf(order.totalAmount()).toLowerCase();
                    String status = order.status().getUkrainianName().toLowerCase();
                    String address = order.deliveryAddress() != null ? order.deliveryAddress().toLowerCase() : "";
                    String notes = order.notes() != null ? order.notes().toLowerCase() : "";

                    return phone.contains(searchId) ||
                        date.contains(searchId) ||
                        amount.contains(searchId) ||
                        status.contains(searchId) ||
                        address.contains(searchId) ||
                        notes.contains(searchId);
                })
                .toList();
        }

        ordersList.clear();
        if (orders.isEmpty()) {
            messageLabel.setText("У Вас поки немає замовлень");
        } else {
            messageLabel.setText("");
            ordersList.addAll(orders);
        }

        if (ordersList.isEmpty()) {
            ordersTableView.setPlaceholder(new Label("Наразі таких замовлень немає"));
        }

        ordersTableView.setItems(ordersList);
    }

    private void cancelOrder(Order order) {
        if (!order.status().equals(OrderStatus.PENDING)) {
            messageLabel.setText("Скасування можливо лише для замовлень у статусі 'В обробці'");
            return;
        }

        try {
            Order updatedOrder = new Order(
                order.orderId(),
                order.orderDate(),
                order.totalAmount(),
                OrderStatus.CANCELLED,
                order.deliveryAddress(),
                order.notes(),
                order.phoneNumber()
            );
            List<String> cartIds = orderRepository.findCartIdsByOrderId(order.orderId());
            Order result = orderRepository.update(updatedOrder, cartIds);

            if (result != null) {
                AlertController.showAlert("Замовлення успішно скасовано");
                loadOrders();
            } else {
                AlertController.showAlert("Помилка при скасуванні замовлення");
            }
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Замовлення не знайдено");
        }
    }

    private void viewOrderDetails(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/orderDetails.fxml"));
            loader.setControllerFactory(param -> new OrderDetailsController(order, orderRepository, cartRepository, menuItemRepository));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Деталі замовлення");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(false);
            
            // Встановлюємо модальність вікна, щоб інші вікна були неактивними
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Додаємо іконку
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/data/icon.png")));

            OrderDetailsController controller = loader.getController();
            controller.setStage(stage);
            
            // Показуємо вікно і чекаємо, поки воно закриється
            stage.showAndWait();
        } catch (IOException e) {
            AlertController.showAlert("Помилка при завантаженні деталей замовлення: " + e.getMessage());
        } catch (Exception e) {
            AlertController.showAlert("Помилка при отриманні деталей замовлення: " + e.getMessage());
        }
    }
}