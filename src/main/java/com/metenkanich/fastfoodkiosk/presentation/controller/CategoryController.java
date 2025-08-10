package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Category;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CategoryRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.presentation.validation.CategoryValidator;
import com.metenkanich.fastfoodkiosk.presentation.validation.ValidationResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryController {

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
    private Button chooseImageButton;
    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, String> nameColumn;
    @FXML
    private ImageView imagePreview;

    private CategoryRepositoryImpl categoryRepository;
    private ObservableList<Category> categoriesList;
    private Category selectedCategory;
    private String selectedImagePath;
    private final Map<String, Image> imageCache = new HashMap<>();
    private static final String DEFAULT_IMAGE_PATH = "/images/categories/category.png";

    public CategoryController() {
        DataSource dataSource = DatabaseConnection.getInstance().getDataSource();
        this.categoryRepository = new CategoryRepositoryImpl(dataSource);
        this.categoriesList = FXCollections.observableArrayList();
        this.selectedImagePath = DEFAULT_IMAGE_PATH;
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().categoryName()));

        loadCategories();

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchCategories(newValue));

        addButton.setOnAction(event -> addCategory());
        editButton.setOnAction(event -> editCategory());
        deleteButton.setOnAction(event -> deleteCategory());
        clearFieldsButton.setOnAction(event -> clearFields());
        chooseImageButton.setOnAction(event -> chooseImage());

        categoryTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedCategory = newValue;
                populateFields(newValue);
                editButton.setDisable(newValue == null);
                deleteButton.setDisable(newValue == null);
            });

        setDefaultCategoryImage(imagePreview, "");
    }

    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Виберіть зображення");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(categoryTable.getScene().getWindow());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            updateImagePreview();
        }
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
            String name = nameField.getText().trim();
            String imagePath = selectedImagePath;

            if (name.isEmpty()) {
                AlertController.showAlert("Назва є обов'язковим полем і не може бути порожньою");
                return;
            }

            if (imagePath == null || imagePath.isEmpty()) {
                imagePath = DEFAULT_IMAGE_PATH;
            } else if (!imagePath.startsWith("/images/categories/")) {
                File file = new File(imagePath);
                if (!file.exists() || !file.isFile()) {
                    AlertController.showAlert("Файл зображення не існує: " + imagePath);
                    return;
                }
            }

            Category category = new Category(null, name, imagePath);

            ValidationResult validationResult = CategoryValidator.isCategoryValid(category, false, categoryRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при додаванні категорії:\n" + validationResult.getErrorMessage());
                return;
            }

            Category savedCategory = categoryRepository.create(category);
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
            String name = nameField.getText().trim();
            String imagePath = selectedImagePath;

            if (name.isEmpty()) {
                AlertController.showAlert("Назва є обов'язковим полем і не може бути порожньою");
                return;
            }

            if (imagePath == null || imagePath.isEmpty()) {
                imagePath = DEFAULT_IMAGE_PATH;
            } else if (!imagePath.startsWith("/images/categories/")) {
                File file = new File(imagePath);
                if (!file.exists() || !file.isFile()) {
                    AlertController.showAlert("Файл зображення не існує: " + imagePath);
                    return;
                }
            }

            Category updatedCategory = new Category(selectedCategory.categoryId(), name, imagePath);

            ValidationResult validationResult = CategoryValidator.isCategoryValid(updatedCategory, true, categoryRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при редагуванні категорії:\n" + validationResult.getErrorMessage());
                return;
            }

            Category savedCategory = categoryRepository.update(updatedCategory);
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
        selectedImagePath = DEFAULT_IMAGE_PATH;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        setDefaultCategoryImage(imagePreview, "");
    }

    private void populateFields(Category category) {
        if (category == null) {
            clearFields();
            return;
        }
        nameField.setText(category.categoryName());
        selectedImagePath = category.imagePath() != null ? category.imagePath() : DEFAULT_IMAGE_PATH;
        updateImagePreview();
    }

    private void updateImagePreview() {
        String imagePath = selectedImagePath;
        if (imagePath == null || imagePath.isEmpty()) {
            setDefaultCategoryImage(imagePreview, "");
        } else {
            loadCategoryImage(imagePreview, imagePath, "");
        }
    }

    private void loadCategoryImage(ImageView imageView, String imagePath, String categoryName) {
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            Image image = imageCache.computeIfAbsent(imagePath, path -> {
                try {
                    if (path.startsWith("/images/categories/")) {
                        InputStream inputStream = getClass().getResourceAsStream(path);
                        if (inputStream == null) {
                            System.err.println("Resource not found: " + path);
                            return null;
                        }
                        Image resourceImage = new Image(inputStream, 0, 0, true, true);
                        if (!resourceImage.isError()) {
                            return resourceImage;
                        }
                    }
                    File file = new File(path);
                    if (file.exists() && file.isFile()) {
                        return new Image("file:" + path, 0, 0, true, true);
                    } else {
                        System.err.println("File not found or inaccessible: " + path);
                        return null;
                    }
                } catch (Exception e) {
                    System.err.println("Помилка завантаження зображення з шляху: " + path + ", error: " + e.getMessage());
                    return null;
                }
            });

            if (image != null && !image.isError()) {
                imageView.setImage(image);
            } else {
                System.err.println("Помилка завантаження зображення для категорії: " + categoryName + " з шляху: " + imagePath);
                setDefaultCategoryImage(imageView, categoryName);
            }
        } else {
            setDefaultCategoryImage(imageView, categoryName);
        }
    }

    private void setDefaultCategoryImage(ImageView imageView, String categoryName) {
        try {
            Image defaultImage = imageCache.computeIfAbsent(DEFAULT_IMAGE_PATH, path -> {
                try {
                    InputStream inputStream = getClass().getResourceAsStream(path);
                    if (inputStream == null) {
                        System.err.println("Default image resource not found: " + path);
                        return null;
                    }
                    Image image = new Image(inputStream, 0, 0, true, true);
                    if (image.isError()) {
                        throw new IllegalStateException("Default image is invalid: " + path);
                    }
                    return image;
                } catch (Exception e) {
                    System.err.println("Помилка завантаження зображення за замовчуванням: " + path + ", error: " + e.getMessage());
                    return null;
                }
            });

            if (defaultImage != null && !defaultImage.isError()) {
                imageView.setImage(defaultImage);
            } else {
                System.err.println("Не вдалося встановити зображення за замовчуванням для категорії: " + categoryName);
                imageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Помилка встановлення зображення за замовчуванням для категорії: " + categoryName + ", error: " + e.getMessage());
            imageView.setImage(null);
        }
    }
}