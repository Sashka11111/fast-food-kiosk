package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Category;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PortionSize;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CategoryRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.presentation.validation.MenuItemValidator;
import com.metenkanich.fastfoodkiosk.presentation.validation.ValidationResult;
import javafx.beans.property.SimpleObjectProperty;
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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MenuItemController {

    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField priceField;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private ComboBox<PortionSize> portionSizeComboBox;
    @FXML
    private CheckBox isAvailableCheckBox;
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
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button chooseImageButton;
    @FXML
    private TableView<MenuItem> menuItemTable;
    @FXML
    private TableColumn<MenuItem, String> nameColumn;
    @FXML
    private TableColumn<MenuItem, String> descriptionColumn;
    @FXML
    private TableColumn<MenuItem, BigDecimal> priceColumn;
    @FXML
    private TableColumn<MenuItem, String> categoryColumn;
    @FXML
    private TableColumn<MenuItem, Boolean> availableColumn;
    @FXML
    private TableColumn<MenuItem, PortionSize> portionSizeColumn;
    @FXML
    private ImageView imagePreview;

    private MenuItemRepositoryImpl menuItemRepository;
    private CategoryRepositoryImpl categoryRepository;
    private ObservableList<MenuItem> menuItemsList;
    private MenuItem selectedMenuItem;
    private String selectedImagePath;
    private final Map<String, Image> imageCache = new HashMap<>();
    private static final String DEFAULT_IMAGE_PATH = "/images/food.png";

    public MenuItemController() {
        DataSource dataSource = DatabaseConnection.getInstance().getDataSource();
        this.menuItemRepository = new MenuItemRepositoryImpl(dataSource);
        this.categoryRepository = new CategoryRepositoryImpl(dataSource);
        this.menuItemsList = FXCollections.observableArrayList();
        this.selectedImagePath = DEFAULT_IMAGE_PATH;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        loadMenuItems();
        loadCategories();

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchMenuItems(newValue));

        addButton.setOnAction(event -> addMenuItem());
        editButton.setOnAction(event -> editMenuItem());
        deleteButton.setOnAction(event -> deleteMenuItem());
        clearFieldsButton.setOnAction(event -> clearFields());
        chooseImageButton.setOnAction(event -> chooseImage());

        menuItemTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedMenuItem = newValue;
            populateFields(newValue);
            editButton.setDisable(newValue == null);
            deleteButton.setDisable(newValue == null);
        });

        updateImagePreview();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name()));

        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().description()));

        priceColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().price()));
        priceColumn.setCellFactory(column -> new TableCell<MenuItem, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ₴", price));
                }
            }
        });

        categoryColumn.setCellValueFactory(cellData -> {
            UUID categoryId = cellData.getValue().categoryId();
            if (categoryId != null) {
                try {
                    Category category = categoryRepository.findById(categoryId);
                    return new SimpleStringProperty(category.categoryName());
                } catch (EntityNotFoundException e) {
                    return new SimpleStringProperty("Невідома категорія");
                }
            }
            return new SimpleStringProperty("Без категорії");
        });

        availableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().isAvailable()));
        availableColumn.setCellFactory(column -> new TableCell<MenuItem, Boolean>() {
            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                if (empty || available == null) {
                    setText(null);
                } else {
                    setText(available ? "Доступний" : "Недоступний");
                }
            }
        });

        portionSizeColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().defaultPortionSize()));
        portionSizeColumn.setCellFactory(column -> new TableCell<MenuItem, PortionSize>() {
            @Override
            protected void updateItem(PortionSize portionSize, boolean empty) {
                super.updateItem(portionSize, empty);
                if (empty || portionSize == null) {
                    setText(null);
                } else {
                    setText(portionSize.getDisplayName());
                }
            }
        });
    }

    private void setupComboBoxes() {
        portionSizeComboBox.setItems(FXCollections.observableArrayList(PortionSize.values()));
        portionSizeComboBox.setValue(PortionSize.MEDIUM);

        categoryComboBox.setCellFactory(listView -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(category.categoryName());
                }
            }
        });
        
        categoryComboBox.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(category.categoryName());
                }
            }
        });
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            AlertController.showAlert("Помилка завантаження категорій: " + e.getMessage());
        }
    }

    private void loadMenuItems() {
        try {
            List<MenuItem> menuItems = menuItemRepository.findAll();
            menuItemsList.setAll(menuItems);
            menuItemTable.setItems(menuItemsList);
        } catch (Exception e) {
            AlertController.showAlert("Помилка завантаження елементів меню: " + e.getMessage());
        }
    }

    private void searchMenuItems(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            menuItemTable.setItems(menuItemsList);
            return;
        }

        ObservableList<MenuItem> filteredList = FXCollections.observableArrayList();
        String lowerCaseFilter = searchText.toLowerCase();

        for (MenuItem menuItem : menuItemsList) {
            if (menuItem.name().toLowerCase().contains(lowerCaseFilter) ||
                (menuItem.description() != null && menuItem.description().toLowerCase().contains(lowerCaseFilter))) {
                filteredList.add(menuItem);
            }
        }

        menuItemTable.setItems(filteredList);
    }

    private void addMenuItem() {
        try {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String priceText = priceField.getText().trim();
            Category selectedCategory = categoryComboBox.getValue();
            PortionSize selectedPortionSize = portionSizeComboBox.getValue();
            boolean isAvailable = isAvailableCheckBox.isSelected();

            if (name.isEmpty()) {
                AlertController.showAlert("Будь ласка, введіть назву елемента меню");
                return;
            }

            if (priceText.isEmpty()) {
                AlertController.showAlert("Будь ласка, введіть ціну");
                return;
            }

            if (selectedCategory == null) {
                AlertController.showAlert("Будь ласка, виберіть категорію");
                return;
            }

            BigDecimal price;
            try {
                price = new BigDecimal(priceText);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    AlertController.showAlert("Ціна повинна бути більше нуля");
                    return;
                }
            } catch (NumberFormatException e) {
                AlertController.showAlert("Будь ласка, введіть коректну ціну");
                return;
            }

            String imagePath = selectedImagePath.equals(DEFAULT_IMAGE_PATH) ? null : selectedImagePath;
            
            MenuItem menuItem = new MenuItem(null, name, description.isEmpty() ? null : description, 
                price, selectedCategory.categoryId(), isAvailable, imagePath, selectedPortionSize);

            ValidationResult validationResult = MenuItemValidator.isMenuItemValid(menuItem, false, menuItemRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при додаванні елемента меню:\n" + validationResult.getErrorMessage());
                return;
            }

            MenuItem savedMenuItem = menuItemRepository.create(menuItem);
            if (savedMenuItem != null) {
                loadMenuItems();
                clearFields();
                AlertController.showAlert("Елемент меню успішно додано!");
            } else {
                AlertController.showAlert("Не вдалося зберегти елемент меню. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при додаванні елемента меню: " + e.getMessage());
        }
    }

    private void editMenuItem() {
        if (selectedMenuItem == null) {
            AlertController.showAlert("Будь ласка, виберіть елемент меню для редагування");
            return;
        }

        try {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String priceText = priceField.getText().trim();
            Category selectedCategory = categoryComboBox.getValue();
            PortionSize selectedPortionSize = portionSizeComboBox.getValue();
            boolean isAvailable = isAvailableCheckBox.isSelected();

            if (name.isEmpty()) {
                AlertController.showAlert("Будь ласка, введіть назву елемента меню");
                return;
            }

            if (priceText.isEmpty()) {
                AlertController.showAlert("Будь ласка, введіть ціну");
                return;
            }

            if (selectedCategory == null) {
                AlertController.showAlert("Будь ласка, виберіть категорію");
                return;
            }

            BigDecimal price;
            try {
                price = new BigDecimal(priceText);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    AlertController.showAlert("Ціна повинна бути більше нуля");
                    return;
                }
            } catch (NumberFormatException e) {
                AlertController.showAlert("Будь ласка, введіть коректну ціну");
                return;
            }

            String imagePath = selectedImagePath.equals(DEFAULT_IMAGE_PATH) ? null : selectedImagePath;

            MenuItem updatedMenuItem = new MenuItem(selectedMenuItem.itemId(), name,
                description.isEmpty() ? null : description, price, selectedCategory.categoryId(),
                isAvailable, imagePath, selectedPortionSize);

            ValidationResult validationResult = MenuItemValidator.isMenuItemValid(updatedMenuItem, true, menuItemRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при редагуванні елемента меню:\n" + validationResult.getErrorMessage());
                return;
            }

            MenuItem savedMenuItem = menuItemRepository.update(updatedMenuItem);
            if (savedMenuItem != null) {
                loadMenuItems();
                clearFields();
                AlertController.showAlert("Елемент меню успішно оновлено!");
            } else {
                AlertController.showAlert("Не вдалося оновити елемент меню. Спробуйте ще раз.");
            }
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Елемент меню не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при редагуванні елемента меню: " + e.getMessage());
        }
    }

    private void deleteMenuItem() {
        if (selectedMenuItem == null) {
            AlertController.showAlert("Будь ласка, виберіть елемент меню для видалення");
            return;
        }

        try {
            menuItemRepository.deleteById(selectedMenuItem.itemId());
            loadMenuItems();
            clearFields();
            AlertController.showAlert("Елемент меню успішно видалено!");
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Елемент меню не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при видаленні елемента меню: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        descriptionField.clear();
        priceField.clear();
        categoryComboBox.setValue(null);
        portionSizeComboBox.setValue(PortionSize.MEDIUM);
        isAvailableCheckBox.setSelected(true);
        selectedMenuItem = null;
        selectedImagePath = DEFAULT_IMAGE_PATH;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        updateImagePreview();
    }

    private void populateFields(MenuItem menuItem) {
        if (menuItem == null) {
            clearFields();
            return;
        }

        nameField.setText(menuItem.name());
        descriptionField.setText(menuItem.description() != null ? menuItem.description() : "");
        priceField.setText(menuItem.price().toString());
        isAvailableCheckBox.setSelected(menuItem.isAvailable() != null ? menuItem.isAvailable() : true);

        for (Category category : categoryComboBox.getItems()) {
            if (category.categoryId().equals(menuItem.categoryId())) {
                categoryComboBox.setValue(category);
                break;
            }
        }

        portionSizeComboBox.setValue(menuItem.defaultPortionSize() != null ?
            menuItem.defaultPortionSize() : PortionSize.MEDIUM);

        selectedImagePath = menuItem.imagePath() != null ? menuItem.imagePath() : DEFAULT_IMAGE_PATH;
        updateImagePreview();
    }

    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Виберіть зображення для елемента меню");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            updateImagePreview();
        }
    }

    private void updateImagePreview() {
        try {
            Image image;
            if (selectedImagePath.equals(DEFAULT_IMAGE_PATH)) {
                InputStream imageStream = getClass().getResourceAsStream(DEFAULT_IMAGE_PATH);
                if (imageStream != null) {
                    image = new Image(imageStream);
                } else {
                    image = null;
                }
            } else {
                if (imageCache.containsKey(selectedImagePath)) {
                    image = imageCache.get(selectedImagePath);
                } else {
                    File imageFile = new File(selectedImagePath);
                    if (imageFile.exists()) {
                        image = new Image(imageFile.toURI().toString());
                        imageCache.put(selectedImagePath, image);
                    } else {
                        selectedImagePath = DEFAULT_IMAGE_PATH;
                        InputStream imageStream = getClass().getResourceAsStream(DEFAULT_IMAGE_PATH);
                        image = imageStream != null ? new Image(imageStream) : null;
                    }
                }
            }

            imagePreview.setImage(image);
        } catch (Exception e) {
            System.err.println("Помилка завантаження зображення: " + e.getMessage());
            imagePreview.setImage(null);
        }
    }
}
