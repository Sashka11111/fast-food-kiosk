package com.metenkanich.fastfoodkiosk.presentation.controller;

import com.metenkanich.fastfoodkiosk.domain.security.AuthenticatedUser;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import com.metenkanich.fastfoodkiosk.persistence.entity.Category;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.CategoryRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.MenuItemRepository;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.MenuItemRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.CategoryRepositoryImpl;
import com.metenkanich.fastfoodkiosk.persistence.repository.impl.PaymentRepositoryImpl;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class MenuController {

    @FXML
    private Label errorLabel;

    @FXML
    private GridPane menuGridPane;

    @FXML
    private ScrollPane menuScrollPane;

    @FXML
    private TextField searchTextField;

    @FXML
    private ComboBox<Category> categoryComboBox;

    private MenuItemRepository menuItemRepository;
    private MenuItemRepositoryImpl cartRepository;
    private CategoryRepository categoryRepository;
    private List<MenuItem> cartItems;

    public MenuController() {
        this.menuItemRepository = new PaymentRepositoryImpl(new DatabaseConnection().getDataSource());
        this.cartRepository = new MenuItemRepositoryImpl(new DatabaseConnection().getDataSource());
        this.categoryRepository = new CategoryRepositoryImpl(new DatabaseConnection().getDataSource());
    }

    @FXML
    public void initialize() {
        // Ініціалізація поля пошуку
        if (searchTextField != null) {
            searchTextField.setPromptText("Пошук страв...");
            searchTextField.setOnKeyReleased(event -> searchMenuItems());
        } else {
            System.err.println("searchTextField is null");
        }

        // Ініціалізація ComboBox для категорій
        if (categoryComboBox != null) {
            categoryComboBox.getItems().add(new Category(null, "Усі категорії")); // Додаємо опцію "Усі категорії"
            categoryComboBox.getItems().addAll(categoryRepository.findAll());
            categoryComboBox.setValue(categoryComboBox.getItems().get(0)); // Вибираємо "Усі категорії" за замовчуванням
            categoryComboBox.setCellFactory(param -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.categoryName()); // Відображаємо лише назву категорії
                    }
                }
            });
            categoryComboBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.categoryName()); // Відображаємо лише назву категорії
                    }
                }
            });
            categoryComboBox.setOnAction(event -> filterMenuItemsByCategory());
        } else {
            System.err.println("categoryComboBox is null");
        }

        loadMenuItems();
    }

    private void loadMenuItems() {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser != null) {
            cartItems = menuItemRepository.findCartItemsByUserId(currentUser.id());
        } else {
            cartItems = List.of();
        }
        List<MenuItem> menuItems = menuItemRepository.findAll();
        displayMenuCards(menuItems);
    }

    private void displayMenuCards(List<MenuItem> menuItems) {
        if (menuGridPane == null || menuScrollPane == null || errorLabel == null) {
            System.err.println("Один із компонентів GridPane, ScrollPane або errorLabel є null");
            return;
        }
        menuGridPane.getChildren().clear();
        if (menuItems.isEmpty()) {
            errorLabel.setText("Немає доступних страв");
            menuScrollPane.setVisible(false);
            return;
        } else {
            errorLabel.setText("");
            menuScrollPane.setVisible(true);
        }

        int column = 0;
        int row = 0;
        int cardsPerRow = 4;

        // Додаємо інтервали між картками
        menuGridPane.setHgap(10); // Горизонтальний інтервал 10 пікселів
        menuGridPane.setVgap(10); // Вертикальний інтервал 10 пікселів

        menuGridPane.getColumnConstraints().clear();
        menuGridPane.getRowConstraints().clear();

        for (int i = 0; i < cardsPerRow; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / cardsPerRow);
            menuGridPane.getColumnConstraints().add(columnConstraints);
        }

        for (int i = 0; i < (int) Math.ceil((double) menuItems.size() / cardsPerRow); i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(280);
            menuGridPane.getRowConstraints().add(rowConstraints);
        }

        for (MenuItem item : menuItems) {
            AnchorPane card = loadMenuItemCard(item);
            if (card != null) {
                menuGridPane.add(card, column, row);
                column++;
                if (column == cardsPerRow) {
                    column = 0;
                    row++;
                }
            } else {
                System.err.println("Помилка завантаження картки для страви: " + item.name());
            }
        }
    }

    private AnchorPane loadMenuItemCard(MenuItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menuItemCard.fxml"));
            AnchorPane card = loader.load();
            MenuItemCard controller = loader.getController();
            if (controller != null) {
                controller.setMenuItem(item);
                controller.setParentController(this);
                return card;
            } else {
                System.err.println("Контролер MenuItemCard є null");
                return null;
            }
        } catch (IOException e) {
            if (errorLabel != null) {
                errorLabel.setText("Помилка завантаження картки: " + e.getMessage());
            }
            e.printStackTrace();
            return null;
        }
    }

    private void searchMenuItems() {
        if (searchTextField == null) {
            System.err.println("searchTextField is null in searchMenuItems");
            return;
        }
        String query = searchTextField.getText().toLowerCase().trim();
        List<MenuItem> allMenuItems = getFilteredItemsByCategory();
        List<MenuItem> result = allMenuItems.stream()
            .filter(item -> item.name().toLowerCase().contains(query) || (item.description() != null && item.description().toLowerCase().contains(query)))
            .collect(Collectors.toList());
        displayMenuCards(result);
    }

    private void filterMenuItemsByCategory() {
        List<MenuItem> filteredItems = getFilteredItemsByCategory();
        String query = searchTextField.getText().toLowerCase().trim();
        List<MenuItem> result = filteredItems.stream()
            .filter(item -> item.name().toLowerCase().contains(query) || (item.description() != null && item.description().toLowerCase().contains(query)))
            .collect(Collectors.toList());
        displayMenuCards(result);
    }

    private List<MenuItem> getFilteredItemsByCategory() {
        Category selectedCategory = categoryComboBox.getValue();
        if (selectedCategory == null || selectedCategory.categoryId() == null) {
            return menuItemRepository.findAll();
        }
        return menuItemRepository.findByCategoryId(selectedCategory.categoryId());
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
        double subtotal = item.price() * quantity;
        Cart cartItem = new Cart(
            UUID.randomUUID(),
            currentUser.id(),
            item.itemId(),
            quantity,
            subtotal,
            false
        );
        Cart savedCartItem = cartRepository.create(cartItem);
        if (savedCartItem != null) {
            loadMenuItems(); // Оновити відображення
        } else {
            if (errorLabel != null) {
                errorLabel.setText("Помилка додавання до кошика");
            }
        }
    }
}