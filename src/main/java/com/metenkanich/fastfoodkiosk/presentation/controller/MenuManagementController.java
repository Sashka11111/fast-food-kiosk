package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Category;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CategoryRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.PaymentRepositoryImpl;
import com.metenkanich.fastfoodkiosk.presentation.validation.MenuItemValidator;
import com.metenkanich.fastfoodkiosk.presentation.validation.ValidationResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;

public class MenuManagementController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField caloriesField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextArea ingredientsArea;
    @FXML
    private CheckComboBox<Category> categoryCheckComboBox;
    @FXML
    private ImageView imagePreview;
    @FXML
    private Button uploadImageButton;
    @FXML
    private TableView<MenuItem> menuTable;
    @FXML
    private TableColumn<MenuItem, String> nameColumn;
    @FXML
    private TableColumn<MenuItem, String> descriptionColumn;
    @FXML
    private TableColumn<MenuItem, Double> priceColumn;
    @FXML
    private TableColumn<MenuItem, Integer> caloriesColumn;
    @FXML
    private TableColumn<MenuItem, String> ingredientsColumn;
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

    private PaymentRepositoryImpl menuItemRepository;
    private CategoryRepositoryImpl categoryRepository;
    private ObservableList<MenuItem> menuItemsList;
    private MenuItem selectedMenuItem;
    private byte[] selectedImage;
    private Map<UUID, Category> categoryIdToCategory;

    public MenuManagementController() {
        this.menuItemRepository = new PaymentRepositoryImpl(new DatabaseConnection().getDataSource());
        this.menuItemsList = FXCollections.observableArrayList();
        this.categoryRepository = new CategoryRepositoryImpl(new DatabaseConnection().getDataSource());
    }

    @FXML
    public void initialize() {
        // Налаштування колонок таблиці
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().description() != null ? cellData.getValue().description() : ""));
        priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().price()).asObject());
        caloriesColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().calories() != null ? cellData.getValue().calories() : 0).asObject());
        ingredientsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().ingredients() != null ? cellData.getValue().ingredients() : ""));

        // Налаштування відображення категорій у CheckComboBox
        categoryCheckComboBox.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category == null ? null : category.categoryName();
            }

            @Override
            public Category fromString(String string) {
                // Пошук категорії за назвою
                for (Category category : categoryCheckComboBox.getItems()) {
                    if (category.categoryName().equals(string)) {
                        return category;
                    }
                }
                return null;
            }
        });

        // Заповнення CheckComboBox категоріями
        loadCategories();

        // Завантаження початкового списку пунктів меню
        loadMenuItems();

        // Обробка пошуку
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchMenuItems(newValue));

        // Додавання слухачів для кнопок
        addButton.setOnAction(event -> addMenuItem());
        editButton.setOnAction(event -> editMenuItem());
        deleteButton.setOnAction(event -> deleteMenuItem());
        clearFieldsButton.setOnAction(event -> clearFields());
        uploadImageButton.setOnAction(event -> uploadImage());

        // Слухач для вибору рядка в таблиці
        menuTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedMenuItem = newValue;
                populateFields(newValue);
                editButton.setDisable(newValue == null);
                deleteButton.setDisable(newValue == null);
            });
    }

    private void loadCategories() {
        List<Category> categories = categoryRepository.findAll();
        categoryIdToCategory = categories.stream()
            .collect(Collectors.toMap(Category::categoryId, category -> category));
        
        categoryCheckComboBox.getItems().clear();
        categoryCheckComboBox.getItems().addAll(categories);
    }

    private void loadMenuItems() {
        try {
            List<MenuItem> items = menuItemRepository.findAll();
            menuItemsList.setAll(items);
            menuTable.setItems(menuItemsList);
            if (menuItemsList.isEmpty()) {
                menuTable.setPlaceholder(new Label("Немає пунктів меню"));
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні пунктів меню: " + e.getMessage());
        }
    }

    private void searchMenuItems(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadMenuItems();
            return;
        }

        List<MenuItem> filteredItems = menuItemsList.stream()
            .filter(item -> item.name().toLowerCase().contains(searchText.toLowerCase()) ||
                (item.description() != null && item.description().toLowerCase().contains(searchText.toLowerCase())) ||
                (item.ingredients() != null && item.ingredients().toLowerCase().contains(searchText.toLowerCase())))
            .toList();
        if (filteredItems.isEmpty()) {
            menuTable.setPlaceholder(new Label("Немає пунктів меню"));
        } else {
            menuTable.setPlaceholder(null);
        }
        menuTable.setItems(FXCollections.observableArrayList(filteredItems));

    }

    private void addMenuItem() {
        try {
            // Отримання даних із полів
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            String caloriesText = caloriesField.getText().trim();
            String description = descriptionArea.getText().trim();
            String ingredients = ingredientsArea.getText().trim();
            ObservableList<Category> selectedCategories = categoryCheckComboBox.getCheckModel().getCheckedItems();

            // Перевірка обов'язкових полів
            if (name.isEmpty()) {
                AlertController.showAlert("Назва є обов'язковим полем і не може бути порожньою");
                return;
            }
            if (priceText.isEmpty()) {
                AlertController.showAlert("Ціна є обов'язковим полем і не може бути порожньою");
                return;
            }
            if (description.isEmpty()) {
                AlertController.showAlert("Опис є обов'язковим полем і не може бути порожнім");
                return;
            }
            if (selectedCategories.isEmpty()) {
                AlertController.showAlert("Виберіть хоча б одну категорію");
                return;
            }

            // Парсинг числових значень
            double price;
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                AlertController.showAlert("Ціна має бути числовим значенням (наприклад, 10.50)");
                return;
            }

            Integer calories;
            try {
                calories = caloriesText.isEmpty() ? null : Integer.parseInt(caloriesText);
            } catch (NumberFormatException e) {
                AlertController.showAlert("Калорії мають бути цілим числом (наприклад, 200)");
                return;
            }

            // Створення об'єкта MenuItem
            MenuItem menuItem = new MenuItem(null, name, description, price, calories, selectedImage, ingredients);

            // Валідація даних
            ValidationResult validationResult = MenuItemValidator.isMenuItemValid(menuItem, false, menuItemRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при додаванні пункту меню:\n" + validationResult.getErrorMessage());
                return;
            }

            // Збереження в базі даних
            MenuItem savedItem = menuItemRepository.save(menuItem);
            if (savedItem != null) {
                // Зберігаємо зв'язки з категоріями
                List<UUID> categoryIds = selectedCategories.stream()
                    .map(Category::categoryId)
                    .collect(Collectors.toList());
                menuItemRepository.saveItemCategories(savedItem.itemId(), categoryIds);
                
                loadMenuItems();
                clearFields();
                AlertController.showAlert("Пункт меню успішно додано!");
            } else {
                AlertController.showAlert("Не вдалося зберегти пункт меню. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при додаванні пункту меню: " + e.getMessage());
        }
    }

    private void editMenuItem() {
        if (selectedMenuItem == null) {
            AlertController.showAlert("Будь ласка, виберіть пункт меню для редагування");
            return;
        }

        try {
            // Отримання даних із полів
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            String caloriesText = caloriesField.getText().trim();
            String description = descriptionArea.getText().trim();
            String ingredients = ingredientsArea.getText().trim();
            ObservableList<Category> selectedCategories = categoryCheckComboBox.getCheckModel().getCheckedItems();

            // Перевірка обов'язкових полів
            if (name.isEmpty()) {
                AlertController.showAlert("Назва є обов'язковим полем і не може бути порожньою");
                return;
            }
            if (priceText.isEmpty()) {
                AlertController.showAlert("Ціна є обов'язковим полем і не може бути порожньою");
                return;
            }
            if (description.isEmpty()) {
                AlertController.showAlert("Опис є обов'язковим полем і не може бути порожнім");
                return;
            }
            if (selectedCategories.isEmpty()) {
                AlertController.showAlert("Виберіть хоча б одну категорію");
                return;
            }

            // Парсинг числових значень
            double price;
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                AlertController.showAlert("Ціна має бути числовим значенням (наприклад, 10.50)");
                return;
            }

            Integer calories;
            try {
                calories = caloriesText.isEmpty() ? null : Integer.parseInt(caloriesText);
            } catch (NumberFormatException e) {
                AlertController.showAlert("Калорії мають бути цілим числом (наприклад, 200)");
                return;
            }

            // Створення оновленого об'єкта MenuItem
            MenuItem updatedItem = new MenuItem(selectedMenuItem.itemId(), name, description, price, calories, 
                                               selectedImage != null ? selectedImage : selectedMenuItem.image(), 
                                               ingredients);

            // Валідація даних
            ValidationResult validationResult = MenuItemValidator.isMenuItemValid(updatedItem, true, menuItemRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при редагуванні пункту меню:\n" + validationResult.getErrorMessage());
                return;
            }

            // Збереження в базі даних
            MenuItem savedItem = menuItemRepository.save(updatedItem);
            if (savedItem != null) {
                // Оновлюємо зв'язки з категоріями
                List<UUID> categoryIds = selectedCategories.stream()
                    .map(Category::categoryId)
                    .collect(Collectors.toList());
                menuItemRepository.updateItemCategories(savedItem.itemId(), categoryIds);
                
                loadMenuItems();
                clearFields();
                AlertController.showAlert("Пункт меню успішно оновлено!");
            } else {
                AlertController.showAlert("Не вдалося оновити пункт меню. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при редагуванні пункту меню: " + e.getMessage());
        }
    }

    private void deleteMenuItem() {
        if (selectedMenuItem == null) {
            AlertController.showAlert("Будь ласка, виберіть пункт меню для видалення");
            return;
        }

        try {
            // Видаляємо зв'язки з категоріями перед видаленням пункту меню
            menuItemRepository.deleteItemCategories(selectedMenuItem.itemId());
            // Видаляємо сам пункт меню
            menuItemRepository.deleteById(selectedMenuItem.itemId());
            loadMenuItems();
            clearFields();
            AlertController.showAlert("Пункт меню успішно видалено!");
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Пункт меню не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при видаленні пункту меню: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        priceField.clear();
        caloriesField.clear();
        descriptionArea.clear();
        ingredientsArea.clear();
        categoryCheckComboBox.getCheckModel().clearChecks();
        imagePreview.setImage(new Image(getClass().getResourceAsStream("/data/ingredients.png")));
        selectedImage = null;
        selectedMenuItem = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void populateFields(MenuItem menuItem) {
        if (menuItem == null) {
            clearFields();
            return;
        }
        nameField.setText(menuItem.name());
        priceField.setText(String.valueOf(menuItem.price()));
        caloriesField.setText(menuItem.calories() != null ? String.valueOf(menuItem.calories()) : "");
        descriptionArea.setText(menuItem.description() != null ? menuItem.description() : "");
        ingredientsArea.setText(menuItem.ingredients() != null ? menuItem.ingredients() : "");
        
        // Очищаємо попередні вибрані категорії
        categoryCheckComboBox.getCheckModel().clearChecks();
        
        // Завантажуємо категорії для цього пункту меню
        try {
            List<Category> itemCategories = menuItemRepository.findCategoriesByItemId(menuItem.itemId());
            
            // Відмічаємо категорії в CheckComboBox
            for (Category category : itemCategories) {
                for (int i = 0; i < categoryCheckComboBox.getItems().size(); i++) {
                    Category item = categoryCheckComboBox.getItems().get(i);
                    if (item.categoryId().equals(category.categoryId())) {
                        categoryCheckComboBox.getCheckModel().check(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні категорій пункту меню: " + e.getMessage());
        }
        
        if (menuItem.image() != null) {
            selectedImage = menuItem.image();
            imagePreview.setImage(new Image(new java.io.ByteArrayInputStream(menuItem.image())));
        } else {
            imagePreview.setImage(new Image(getClass().getResourceAsStream("/data/ingredients.png")));
            selectedImage = null;
        }
    }

    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Виберіть зображення");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                selectedImage = Files.readAllBytes(file.toPath());
                imagePreview.setImage(new Image(new java.io.FileInputStream(file)));
            } catch (IOException e) {
                AlertController.showAlert("Помилка при завантаженні зображення: " + e.getMessage());
            }
        }
    }
}