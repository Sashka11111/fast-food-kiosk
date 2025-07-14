package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Cart;
import com.metenkanich.fastfoodkiosk.persistence.entity.Category;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.CartRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.CategoryRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.MenuItemRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CartRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CategoryRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class MenuController {

    @FXML
    private HBox categoryBar;

    @FXML
    private GridPane menuGrid;

    @FXML
    private Label errorLabel;

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final CartRepository cartRepository; // Added CartRepository
    private Button selectedCategoryButton;

    public MenuController() {
        // Initialize repositories
        this.categoryRepository = new CategoryRepositoryImpl(DatabaseConnection.getStaticDataSource());
        this.menuItemRepository = new MenuItemRepositoryImpl(DatabaseConnection.getStaticDataSource());
        this.cartRepository = new CartRepositoryImpl(DatabaseConnection.getStaticDataSource()); // Initialize CartRepository
    }

    @FXML
    void initialize() {
        loadCategories();
        loadMenuItems(null); // Load all menu items by default
    }

    private void loadCategories() {
        // Add "All Categories" button
        Button allCategoriesButton = createCategoryButton("Всі категорії", null, null);
        allCategoriesButton.setOnAction(event -> {
            selectCategoryButton(allCategoriesButton);
            loadMenuItems(null);
        });
        categoryBar.getChildren().add(allCategoriesButton);

        // Set "All Categories" as default selected
        selectCategoryButton(allCategoriesButton);

        // Load categories from database
        try {
            List<Category> categories = categoryRepository.findAll();
            for (Category category : categories) {
                Button categoryButton = createCategoryButton(category.categoryName(), category.imagePath(), category.categoryId());
                categoryButton.setOnAction(event -> {
                    selectCategoryButton(categoryButton);
                    loadMenuItems(category.categoryId());
                });
                categoryBar.getChildren().add(categoryButton);
            }
        } catch (Exception e) {
            if (errorLabel != null) {
                errorLabel.setText("Помилка завантаження категорій: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private Button createCategoryButton(String text, String imagePath, UUID categoryId) {
        Button categoryButton = new Button(text);

        String baseStyle = "-fx-background-radius: 25; -fx-padding: 8 16 8 16; " +
            "-fx-font-size: 12; -fx-font-weight: bold; " +
            "-fx-min-width: 120; -fx-pref-height: 60; " +
            "-fx-background-color: #f0f0f0; -fx-text-fill: #666666; " +
            "-fx-border-color: transparent; -fx-border-radius: 25;";

        categoryButton.setStyle(baseStyle);

        // Add category image
        ImageView imageView = createCategoryImageView(imagePath, text);
        if (imageView != null) {
            categoryButton.setGraphic(imageView);
            categoryButton.setContentDisplay(ContentDisplay.LEFT);
            categoryButton.setGraphicTextGap(8);
        }

        return categoryButton;
    }

    private ImageView createCategoryImageView(String imagePath, String categoryName) {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                // Load image from resources
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                if (!image.isError()) {
                    imageView.setImage(image);
                } else {
                    System.err.println("Помилка завантаження зображення для категорії: " + categoryName + " з шляху: " + imagePath);
                    setDefaultCategoryImage(imageView, categoryName);
                }
            } catch (Exception e) {
                System.err.println("Помилка завантаження зображення для категорії: " + categoryName + " з шляху: " + imagePath);
                setDefaultCategoryImage(imageView, categoryName);
            }
        } else {
            // Set default image
            setDefaultCategoryImage(imageView, categoryName);
        }

        return imageView;
    }

    private void setDefaultCategoryImage(ImageView imageView, String categoryName) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/category.png"));
            if (!defaultImage.isError()) {
                imageView.setImage(defaultImage);
            } else {
                System.err.println("Помилка завантаження дефолтного зображення для категорії: " + categoryName);
            }
        } catch (Exception e) {
            System.err.println("Помилка завантаження дефолтного зображення для категорії: " + categoryName + ": " + e.getMessage());
        }
    }

    private void selectCategoryButton(Button button) {
        // Reset style of previously selected button
        if (selectedCategoryButton != null) {
            String baseStyle = "-fx-background-radius: 25; -fx-padding: 8 16 8 16; " +
                "-fx-font-size: 12; -fx-font-weight: bold; " +
                "-fx-min-width: 120; -fx-pref-height: 60; " +
                "-fx-background-color: #f0f0f0; -fx-text-fill: #666666; " +
                "-fx-border-color: transparent; -fx-border-radius: 25;";
            selectedCategoryButton.setStyle(baseStyle);
        }

        // Apply style to new selected button
        String selectedStyle = "-fx-background-radius: 25; -fx-padding: 8 16 8 16; " +
            "-fx-font-size: 12; -fx-font-weight: bold; " +
            "-fx-min-width: 120; -fx-pref-height: 60; " +
            "-fx-background-color: #f0f0f0; -fx-text-fill: #e47d7e; " +
            "-fx-border-color: #e47d7e; -fx-border-radius: 25; -fx-border-width: 2;";
        button.setStyle(selectedStyle);

        selectedCategoryButton = button;
    }

    private void loadMenuItems(UUID categoryId) {
        menuGrid.getChildren().clear();
        try {
            List<MenuItem> menuItems = categoryId == null ? menuItemRepository.findAll() : menuItemRepository.findByCategory(categoryId);
            int row = 0;
            int col = 0;
            for (MenuItem item : menuItems) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menu_item_card.fxml"));
                    VBox card = loader.load();
                    // Set up card
                    MenuItemCardController cardController = loader.getController();
                    cardController.setMenuItem(item);
                    menuGrid.add(card, col, row);
                    col++;
                    if (col > 2) {
                        col = 0;
                        row++;
                    }
                } catch (IOException e) {
                    if (errorLabel != null) {
                        errorLabel.setText("Помилка завантаження картки меню: " + e.getMessage());
                    }
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            if (errorLabel != null) {
                errorLabel.setText("Помилка завантаження пунктів меню: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    public void addToCart(MenuItem item, int quantity) {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (errorLabel != null) {
                errorLabel.setText("Будь ласка, увійдіть у систему");
            }
            return;
        }
        if (quantity <= 0) {
            if (errorLabel != null) {
                errorLabel.setText("Вкажіть коректну кількість");
            }
            return;
        }
        try {
            double subtotal = item.price().doubleValue() * quantity;
            Cart cartItem = new Cart(
                UUID.randomUUID(),
                currentUser.id(),
                item.itemId(),
                quantity,
                (float) subtotal, // Assuming Cart.subtotal is float based on schema
                false
            );
            Cart savedCartItem = cartRepository.create(cartItem);
            if (savedCartItem != null) {
                loadMenuItems(null); // Refresh menu items
                if (errorLabel != null) {
                    errorLabel.setText("Товар додано до кошика");
                }
            } else {
                if (errorLabel != null) {
                    errorLabel.setText("Помилка додавання до кошика");
                }
            }
        } catch (Exception e) {
            if (errorLabel != null) {
                errorLabel.setText("Помилка додавання до кошика: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
}