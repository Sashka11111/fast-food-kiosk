package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.domain.security.PasswordHashing;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.Role;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.UserRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.UserRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.presentation.validation.UserValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.sql.DataSource;

public class UserManagementController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> createdAtColumn;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearFieldsButton;

    private UserRepository userRepository;
    private ObservableList<User> usersList;
    private User selectedUser;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserManagementController() {
        DataSource dataSource = DatabaseConnection.getInstance().getDataSource();
        this.userRepository = new UserRepositoryImpl(dataSource);
        this.usersList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Налаштування колонок таблиці
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().username()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().email()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().role().getLabel()));
        createdAtColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().createdAt().format(DATE_TIME_FORMATTER)));

        // Налаштування ComboBox для ролей
        roleComboBox.setConverter(new StringConverter<Role>() {
            @Override
            public String toString(Role role) {
                return role == null ? null : role.getLabel();
            }

            @Override
            public Role fromString(String string) {
                return string == null ? null : Role.valueOf(string);
            }
        });
        roleComboBox.getItems().addAll(Role.values());

        // Завантаження початкового списку користувачів
        loadUsers();

        // Обробка пошуку
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchUsers(newValue));

        // Додавання слухачів для кнопок
        addButton.setOnAction(event -> addUser());
        editButton.setOnAction(event -> editUser());
        deleteButton.setOnAction(event -> deleteUser());
        clearFieldsButton.setOnAction(event -> clearFields());

        // Слухач для вибору рядка в таблиці
        userTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedUser = newValue;
                populateFields(newValue);
                editButton.setDisable(newValue == null);
                deleteButton.setDisable(newValue == null);
            });
    }

    private void loadUsers() {
        try {
            List<User> users = userRepository.findAll();
            usersList.setAll(users);
            userTable.setItems(usersList);
            if (usersList.isEmpty()) {
                userTable.setPlaceholder(new Label("Немає користувачів"));
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні користувачів: " + e.getMessage());
        }
    }

    private void searchUsers(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadUsers();
            return;
        }

        List<User> filteredUsers = usersList.stream()
            .filter(user -> user.username().toLowerCase().contains(searchText.toLowerCase()) ||
                user.email().toLowerCase().contains(searchText.toLowerCase()) ||
                user.role().getLabel().toLowerCase().contains(searchText.toLowerCase()))
            .toList();
        if (filteredUsers.isEmpty()) {
            userTable.setPlaceholder(new Label("Немає користувачів"));
        } else {
            userTable.setPlaceholder(null);
        }
        userTable.setItems(FXCollections.observableArrayList(filteredUsers));
    }

    private void addUser() {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String hashedPassword = selectedUser.password();
            String email = emailField.getText().trim();
            Role role = roleComboBox.getValue();

            // Перевірка обов'язкових полів
            if (username.isEmpty()) {
                AlertController.showAlert("Ім'я користувача є обов'язковим полем і не може бути порожнім");
                return;
            }
            if (password.isEmpty()) {
                AlertController.showAlert("Пароль є обов'язковим полем і не може бути порожнім");
                return;
            }
            if (email.isEmpty()) {
                AlertController.showAlert("Email є обов'язковим полем і не може бути порожнім");
                return;
            }
            if (role == null) {
                AlertController.showAlert("Роль є обов'язковим полем і не може бути порожньою");
                return;
            }

            // Валідація через UserValidator
            if (!UserValidator.isUsernameValid(username)) {
                AlertController.showAlert("Ім'я користувача невалідне або вже існує");
                return;
            }
            if (!password.isEmpty()) {
                if (!UserValidator.isPasswordValid(password)) {
                    AlertController.showAlert("Пароль не відповідає вимогам: має бути від 6 до 20 символів, містити цифри, малі та великі літери");
                    return;
                }
                hashedPassword = PasswordHashing.getInstance().hashedPassword(password); // оновлення
            }
            if (!UserValidator.isEmailValid(email)) {
                AlertController.showAlert("Невірний формат email або email перевищує 100 символів");
                return;
            }

            // Перевірка унікальності імені користувача та email
            try {
                userRepository.findByUsername(username);
                AlertController.showAlert("Користувач з ім'ям " + username + " уже існує");
                return;
            } catch (EntityNotFoundException e) {
                // Ім'я вільне, продовжуємо
            }
            for (User user : userRepository.findAll()) {
                if (user.email().equalsIgnoreCase(email)) {
                    AlertController.showAlert("Користувач з email " + email + " уже існує");
                    return;
                }
            }

            // Створення об'єкта User
            User user = new User(null, username, hashedPassword, role, email, LocalDateTime.now());

            // Збереження в базі даних
            userRepository.addUser(user);
            loadUsers();
            clearFields();
            AlertController.showAlert("Користувача успішно додано!");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при додаванні користувача");
        }
    }

    private void editUser() {
        if (selectedUser == null) {
            AlertController.showAlert("Будь ласка, виберіть користувача для редагування");
            return;
        }

        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String hashedPassword = selectedUser.password();
            String email = emailField.getText().trim();
            Role role = roleComboBox.getValue();

            // Перевірка обов'язкових полів
            if (username.isEmpty()) {
                AlertController.showAlert("Ім'я користувача є обов'язковим полем і не може бути порожнім");
                return;
            }
            if (!password.isEmpty()) {
                if (!UserValidator.isPasswordValid(password)) {
                    AlertController.showAlert("Пароль не відповідає вимогам: має бути від 6 до 20 символів, містити цифри, малі та великі літери");
                    return;
                }
                hashedPassword = PasswordHashing.getInstance().hashedPassword(password);
            }
            if (email.isEmpty()) {
                AlertController.showAlert("Email є обов'язковим полем і не може бути порожнім");
                return;
            }
            if (role == null) {
                AlertController.showAlert("Роль є обов'язковим полем і не може бути порожньою");
                return;
            }

            if (!UserValidator.isUsernameValid(username)) {
                AlertController.showAlert("Ім'я користувача невалідне");
                return;
            }

            if (!UserValidator.isEmailValid(email)) {
                AlertController.showAlert("Невірний формат email або email перевищує 100 символів");
                return;
            }

            // Перевірка унікальності імені користувача та email (крім поточного користувача)
            if (!username.equals(selectedUser.username())) {
                try {
                    userRepository.findByUsername(username);
                    AlertController.showAlert("Користувач з ім'ям " + username + " уже існує");
                    return;
                } catch (EntityNotFoundException e) {
                }
            }
            for (User user : userRepository.findAll()) {
                if (!user.id().equals(selectedUser.id()) && user.email().equalsIgnoreCase(email)) {
                    AlertController.showAlert("Користувач з email " + email + " уже існує");
                    return;
                }
            }

            // Створення оновленого об'єкта User
            User updatedUser = new User(selectedUser.id(), username, hashedPassword, role, email, selectedUser.createdAt());

            // Збереження в базі даних
            userRepository.deleteUser(selectedUser.username()); // Видаляємо старий запис
            userRepository.addUser(updatedUser); // Додаємо оновлений запис
            loadUsers();
            clearFields();
            AlertController.showAlert("Користувача успішно оновлено!");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при редагуванні користувача: " + e.getMessage());
        }
    }

    private void deleteUser() {
        if (selectedUser == null) {
            AlertController.showAlert("Будь ласка, виберіть користувача для видалення");
            return;
        }

        try {
            userRepository.deleteUser(selectedUser.username());
            loadUsers();
            clearFields();
            AlertController.showAlert("Користувача успішно видалено!");
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Користувача не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при видаленні користувача: " + e.getMessage());
        }
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        emailField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        selectedUser = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void populateFields(User user) {
        if (user == null) {
            clearFields();
            return;
        }
        usernameField.setText(user.username());
        passwordField.setText("");
        emailField.setText(user.email());
        roleComboBox.setValue(user.role());
    }
}