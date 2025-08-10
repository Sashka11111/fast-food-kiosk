package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Category;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.CartRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.CategoryRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.MenuItemRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CartRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CategoryRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MenuController {

    @FXML
    private HBox categoryBar;

    @FXML
    private GridPane menuGrid;

    @FXML
    private Label errorLabel;

    @FXML
    private ProgressIndicator loadingIndicator;

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final CartRepository cartRepository;
    private Button selectedCategoryButton;
    private final Map<UUID, List<MenuItem>> menuItemsCache = new HashMap<>();
    private final Map<String, Image> imageCache = new HashMap<>();

    public MenuController() {
        DataSource dataSource = DatabaseConnection.getInstance().getDataSource();
        this.categoryRepository = new CategoryRepositoryImpl(dataSource);
        this.menuItemRepository = new MenuItemRepositoryImpl(dataSource);
        this.cartRepository = new CartRepositoryImpl(dataSource);
    }

    @FXML
    void initialize() {
        setupGridPane();
        loadCategories();
        loadMenuItems(null);
    }

    private void setupGridPane() {
        menuGrid.setHgap(10.0);
        menuGrid.setVgap(10.0);
        menuGrid.getColumnConstraints().clear();
        for (int i = 0; i < 3; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(33.33);
            columnConstraints.setHgrow(Priority.SOMETIMES);
            menuGrid.getColumnConstraints().add(columnConstraints);
        }
    }

    private void loadCategories() {
        Button allCategoriesButton = createCategoryButton("Всі категорії", null, null);
        allCategoriesButton.setOnAction(event -> {
            selectCategoryButton(allCategoriesButton);
            loadMenuItems(null);
        });
        categoryBar.getChildren().add(allCategoriesButton);
        selectCategoryButton(allCategoriesButton);

        Task<List<Category>> task = new Task<>() {
            @Override
            protected List<Category> call() {
                return categoryRepository.findAll();
            }
        };

        task.setOnSucceeded(event -> {
            List<Category> categories = task.getValue();
            for (Category category : categories) {
                Button categoryButton = createCategoryButton(category.categoryName(), category.imagePath(), category.categoryId());
                categoryButton.setOnAction(e -> {
                    selectCategoryButton(categoryButton);
                    loadMenuItems(category.categoryId());
                });
                categoryBar.getChildren().add(categoryButton);
            }
        });

        task.setOnFailed(event -> {
            if (errorLabel != null) {
                errorLabel.setText("Помилка завантаження категорій: " + task.getException().getMessage());
            }
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private Button createCategoryButton(String text, String imagePath, UUID categoryId) {
        Button categoryButton = new Button(text);
        String baseStyle = "-fx-background-radius: 25; -fx-padding: 8 16 8 16; " +
            "-fx-font-size: 12; -fx-font-weight: bold; " +
            "-fx-min-width: 120; -fx-pref-height: 60; " +
            "-fx-background-color: #f0f0f0; -fx-text-fill: #666666; " +
            "-fx-border-color: transparent; -fx-border-radius: 25;";
        categoryButton.setStyle(baseStyle);

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
            loadCategoryImage(imageView, imagePath, categoryName);
        } else {
            setDefaultCategoryImage(imageView, categoryName);
        }

        return imageView;
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
        String defaultImagePath = "/images/categories/category.png";
        try {
            Image defaultImage = imageCache.computeIfAbsent(defaultImagePath, path -> {
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
                System.err.println("Помилка завантаження дефолтного зображення для категорії: " + categoryName);
                imageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Критична помилка при завантаженні дефолтного зображення: " + e.getMessage());
            imageView.setImage(null);
        }
    }

    private void selectCategoryButton(Button button) {
        if (selectedCategoryButton != null) {
            String baseStyle = "-fx-background-radius: 25; -fx-padding: 8 16 8 16; " +
                "-fx-font-size: 12; -fx-font-weight: bold; " +
                "-fx-min-width: 120; -fx-pref-height: 60; " +
                "-fx-background-color: #f0f0f0; -fx-text-fill: #666666; " +
                "-fx-border-color: transparent; -fx-border-radius: 25;";
            selectedCategoryButton.setStyle(baseStyle);
        }

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
        loadingIndicator.setVisible(true);
        if (errorLabel != null) {
            errorLabel.setText("");
        }


        Task<List<MenuItem>> task = new Task<>() {
            @Override
            protected List<MenuItem> call() {
                if (menuItemsCache.containsKey(categoryId)) {
                    return menuItemsCache.get(categoryId);
                }
                List<MenuItem> items = categoryId == null ? menuItemRepository.findAll() : menuItemRepository.findByCategory(categoryId);
                menuItemsCache.put(categoryId, items);
                return items;
            }
        };

        task.setOnSucceeded(event -> {
            List<MenuItem> menuItems = task.getValue();
            createMenuCards(menuItems);
        });

        task.setOnFailed(event -> {
            loadingIndicator.setVisible(false);
            if (errorLabel != null) {
                errorLabel.setText("Помилка завантаження пунктів меню: " + task.getException().getMessage());
            }
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void createMenuCards(List<MenuItem> menuItems) {
        if (menuItems.isEmpty()) {
            if (errorLabel != null) {
                errorLabel.setText("Немає доступних страв");
            }
            loadingIndicator.setVisible(false);
            return;
        }
        if (errorLabel != null) {
            errorLabel.setText("");
        }

        Task<Void> cardCreationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i < menuItems.size(); i++) {
                    if (isCancelled()) {
                        break;
                    }

                    final MenuItem item = menuItems.get(i);
                    final int col = i % 3;
                    final int row = i / 3;

                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menu_item_card.fxml"));
                            AnchorPane card = loader.load();
                            MenuItemCardController cardController = loader.getController();
                            cardController.setMenuItem(item);
                            menuGrid.add(card, col, row);
                        } catch (IOException e) {
                            if (errorLabel != null) {
                                errorLabel.setText("Помилка завантаження картки меню: " + e.getMessage());
                            }
                            e.printStackTrace();
                        }
                    });

                    Thread.sleep(20);
                }
                return null;
            }
        };

        cardCreationTask.setOnSucceeded(event -> {
            loadingIndicator.setVisible(false);
        });

        cardCreationTask.setOnFailed(event -> {
            loadingIndicator.setVisible(false);
            if (errorLabel != null) {
                errorLabel.setText("Помилка під час створення карток меню.");
            }
            cardCreationTask.getException().printStackTrace();
        });

        new Thread(cardCreationTask).start();
    }
}