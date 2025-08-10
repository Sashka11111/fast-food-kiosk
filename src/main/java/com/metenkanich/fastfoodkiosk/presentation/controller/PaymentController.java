package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Payment;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PaymentMethod;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PaymentStatus;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.PaymentRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.presentation.validation.PaymentValidator;
import com.metenkanich.fastfoodkiosk.presentation.validation.ValidationResult;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class PaymentController {

    @FXML
    private ComboBox<PaymentMethod> paymentMethodComboBox;
    @FXML
    private ComboBox<PaymentStatus> paymentStatusComboBox;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearFieldsButton;
    @FXML
    private TableView<Payment> paymentTable;
    @FXML
    private TableColumn<Payment, String> cartIdColumn;
    @FXML
    private TableColumn<Payment, PaymentMethod> paymentMethodColumn;
    @FXML
    private TableColumn<Payment, PaymentStatus> paymentStatusColumn;
    @FXML
    private TableColumn<Payment, String> createdAtColumn;

    private PaymentRepositoryImpl paymentRepository;
    private ObservableList<Payment> paymentsList;
    private Payment selectedPayment;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PaymentController() {
        DataSource dataSource = DatabaseConnection.getInstance().getDataSource();
        this.paymentRepository = new PaymentRepositoryImpl(dataSource);
        this.paymentsList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        cartIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().cartId().toString()));
        paymentMethodColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().paymentMethod()));
        paymentStatusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().paymentStatus()));
        createdAtColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().createdAt().format(DATE_TIME_FORMATTER)));

        paymentStatusComboBox.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        loadPayments();

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchPayments(newValue));

        editButton.setOnAction(event -> editPayment());
        deleteButton.setOnAction(event -> deletePayment());
        clearFieldsButton.setOnAction(event -> clearFields());

        paymentTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedPayment = newValue;
                populateFields(newValue);
                editButton.setDisable(newValue == null);
                deleteButton.setDisable(newValue == null);
            });
    }

    private void loadPayments() {
        try {
            List<Payment> payments = paymentRepository.findAll();
            paymentsList.setAll(payments);
            paymentTable.setItems(paymentsList);
            if (paymentsList.isEmpty()) {
                paymentTable.setPlaceholder(new Label("Немає платежів"));
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні платежів: " + e.getMessage());
        }
    }

    private void searchPayments(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadPayments();
            return;
        }

        List<Payment> filteredPayments = paymentsList.stream()
            .filter(payment ->
                payment.paymentMethod().toString().toLowerCase().contains(searchText.toLowerCase())
            )
            .toList();
        if (filteredPayments.isEmpty()) {
            paymentTable.setPlaceholder(new Label("Немає платежів"));
        } else {
            paymentTable.setPlaceholder(null);
        }
        paymentTable.setItems(FXCollections.observableArrayList(filteredPayments));
    }

    private void editPayment() {
        if (selectedPayment == null) {
            AlertController.showAlert("Будь ласка, виберіть платіж для редагування");
            return;
        }

        try {
            PaymentMethod paymentMethod = paymentMethodComboBox.getValue();
            PaymentStatus paymentStatus = paymentStatusComboBox.getValue();

            Payment updatedPayment = new Payment(
                selectedPayment.id(),
                selectedPayment.cartId(),
                paymentMethod,
                paymentStatus,
                selectedPayment.createdAt()
            );

            ValidationResult validationResult = PaymentValidator.isPaymentValid(updatedPayment, true);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при редагуванні платежу:\n" + validationResult.getErrorMessage());
                return;
            }

            Payment savedPayment = paymentRepository.update(updatedPayment);
            if (savedPayment != null) {
                loadPayments();
                clearFields();
                AlertController.showAlert("Платіж успішно оновлено!");
            } else {
                AlertController.showAlert("Не вдалося оновити платіж. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при редагуванні платежу: " + e.getMessage());
        }
    }

    private void deletePayment() {
        if (selectedPayment == null) {
            AlertController.showAlert("Будь ласка, виберіть платіж для видалення");
            return;
        }

        try {
            paymentRepository.deleteById(selectedPayment.id());
            loadPayments();
            clearFields();
            AlertController.showAlert("Платіж успішно видалено!");
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Платіж не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при видаленні платежу: " + e.getMessage());
        }
    }

    private void clearFields() {
        paymentMethodComboBox.getSelectionModel().clearSelection();
        paymentStatusComboBox.getSelectionModel().clearSelection();
        selectedPayment = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void populateFields(Payment payment) {
        if (payment == null) {
            clearFields();
            return;
        }
        paymentMethodComboBox.setValue(payment.paymentMethod());
        paymentStatusComboBox.setValue(payment.paymentStatus());
    }
}