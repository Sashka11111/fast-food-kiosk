package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Category;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CategoryRepositoryImpl;
import com.metenkanich.fastfoodkiosk.presentation.validation.CategoryValidator;
import com.metenkanich.fastfoodkiosk.presentation.validation.ValidationResult;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class CategoryManagementController {

    @FXML
    private TextField nameField;
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
    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, String> nameColumn;

    private CategoryRepositoryImpl categoryRepository;
    private ObservableList<Category> categoriesList;
    private Category selectedCategory;

    public CategoryManagementController() {
        this.categoryRepository = new CategoryRepositoryImpl(new DatabaseConnection().getDataSource());
        this.categoriesList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Налаштування колонки таблиці
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().categoryName()));

        // Завантаження початкового списку категорій
        loadCategories();

        // Обробка пошуку
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchCategories(newValue));

        // Додавання слухачів для кнопок
        addButton.setOnAction(event -> addCategory());
        editButton.setOnAction(event -> editCategory());
        deleteButton.setOnAction(event -> deleteCategory());
        clearFieldsButton.setOnAction(event -> clearFields());

        // Слухач для вибору рядка в таблиці
        categoryTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedCategory = newValue;
                populateFields(newValue);
                editButton.setDisable(newValue == null);
                deleteButton.setDisable(newValue == null);
            });
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            categoriesList.setAll(categories);
            categoryTable.setItems(categoriesList);
            if (categoriesList.isEmpty()) {
                categoryTable.setPlaceholder(new Label("Немає категорій"));
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні категорій: " + e.getMessage());
        }
    }

    private void searchCategories(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadCategories();
            return;
        }

        List<Category> filteredCategories = categoriesList.stream()
            .filter(category -> category.categoryName().toLowerCase().contains(searchText.toLowerCase()))
            .toList();
        if (filteredCategories.isEmpty()) {
            categoryTable.setPlaceholder(new Label("Немає категорій"));
        } else {
            categoryTable.setPlaceholder(null);
        }
        categoryTable.setItems(FXCollections.observableArrayList(filteredCategories));
    }

    private void addCategory() {
        try {
            // Отримання даних із поля
            String name = nameField.getText().trim();

            // Перевірка обов'язкового поля
            if (name.isEmpty()) {
                AlertController.showAlert("Назва є обов'язковим полем і не може бути порожньою");
                return;
            }

            // Створення об'єкта Category
            Category category = new Category(null, name);

            // Валідація даних
            ValidationResult validationResult = CategoryValidator.isCategoryValid(category, false, categoryRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при додаванні категорії:\n" + validationResult.getErrorMessage());
                return;
            }

            // Збереження в базі даних
            Category savedCategory = categoryRepository.save(category);
            if (savedCategory != null) {
                loadCategories();
                clearFields();
                AlertController.showAlert("Категорію успішно додано!");
            } else {
                AlertController.showAlert("Не вдалося зберегти категорію. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при додаванні категорії: " + e.getMessage());
        }
    }

    private void editCategory() {
        if (selectedCategory == null) {
            AlertController.showAlert("Будь ласка, виберіть категорію для редагування");
            return;
        }

        try {
            // Отримання даних із поля
            String name = nameField.getText().trim();

            // Перевірка обов'язкового поля
            if (name.isEmpty()) {
                AlertController.showAlert("Назва є обов'язковим полем і не може бути порожньою");
                return;
            }

            // Створення оновленого об'єкта Category
            Category updatedCategory = new Category(selectedCategory.categoryId(), name);

            // Валідація даних
            ValidationResult validationResult = CategoryValidator.isCategoryValid(updatedCategory, true, categoryRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при редагуванні категорії:\n" + validationResult.getErrorMessage());
                return;
            }

            // Збереження в базі даних
            Category savedCategory = categoryRepository.save(updatedCategory);
            if (savedCategory != null) {
                loadCategories();
                clearFields();
                AlertController.showAlert("Категорію успішно оновлено!");
            } else {
                AlertController.showAlert("Не вдалося оновити категорію. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при редагуванні категорії: " + e.getMessage());
        }
    }

    private void deleteCategory() {
        if (selectedCategory == null) {
            AlertController.showAlert("Будь ласка, виберіть категорію для видалення");
            return;
        }

        try {
            categoryRepository.deleteById(selectedCategory.categoryId());
            loadCategories();
            clearFields();
            AlertController.showAlert("Категорію успішно видалено!");
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Категорію не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при видаленні категорії: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        selectedCategory = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void populateFields(Category category) {
        if (category == null) {
            clearFields();
            return;
        }
        nameField.setText(category.categoryName());
    }
}