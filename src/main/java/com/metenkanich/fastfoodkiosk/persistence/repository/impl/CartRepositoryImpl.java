package com.metenkanich.fastfoodkiosk.persistence.repository.impl;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Cart;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.CartRepository;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartRepositoryImpl implements CartRepository {
    private final DataSource dataSource;

    public CartRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Cart> findAll() {
        List<Cart> carts = new ArrayList<>();
        String query = "SELECT * FROM Cart";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                carts.add(mapToCart(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні всіх елементів кошика: " + e.getMessage());
            e.printStackTrace();
        }
        return carts;
    }

    @Override
    public Cart findById(UUID cartId) throws EntityNotFoundException {
        String query = "SELECT * FROM Cart WHERE cart_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, cartId, Types.OTHER);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToCart(resultSet);
                } else {
                    throw new EntityNotFoundException("Елемент кошика з ID " + cartId + " не знайдено");
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Помилка під час пошуку елемента кошика з ID " + cartId, e);
        }
    }

    @Override
    public List<Cart> findByUserId(UUID userId) {
        List<Cart> items = new ArrayList<>();
        String query = "SELECT * FROM Cart WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, userId, Types.OTHER);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapToCart(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні елементів кошика користувача: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public List<Cart> findByCategoryId(UUID categoryId) {
        List<Cart> items = new ArrayList<>();
        String query = "SELECT c.* FROM Cart c " +
                      "INNER JOIN MenuItems m ON c.item_id = m.item_id " +
                      "WHERE m.category_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, categoryId, Types.OTHER);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapToCart(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні елементів кошика за категорією: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }
    @Override
    public void deleteById(UUID cartId) throws EntityNotFoundException {
        String query = "DELETE FROM Cart WHERE cart_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, cartId, Types.OTHER);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Елемент кошика з ID " + cartId + " не знайдено");
            }
        } catch (SQLException e) {
            System.err.println("Помилка при видаленні елемента кошика з ID " + cartId + ": " + e.getMessage());
            e.printStackTrace();
            throw new EntityNotFoundException("Помилка під час видалення елемента кошика з ID " + cartId, e);
        }
    }

    @Override
    public Cart create(Cart cartItem) {
        UUID id = cartItem.cartId() != null ? cartItem.cartId() : UUID.randomUUID();
        String query = "INSERT INTO Cart (cart_id, user_id, item_id, quantity, subtotal, is_ordered) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, id, Types.OTHER);
            preparedStatement.setObject(2, cartItem.userId(), Types.OTHER);
            preparedStatement.setObject(3, cartItem.itemId(), Types.OTHER);
            preparedStatement.setInt(4, cartItem.quantity());
            preparedStatement.setDouble(5, cartItem.subtotal());
            preparedStatement.setBoolean(6, cartItem.isOrdered());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                return new Cart(id, cartItem.userId(), cartItem.itemId(), cartItem.quantity(), cartItem.subtotal(), cartItem.isOrdered());
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Помилка при створенні елемента кошика: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Метод для позначення елементів кошика як замовлених
    public void markCartItemsAsOrdered(List<UUID> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            return;
        }

        StringBuilder query = new StringBuilder("UPDATE Cart SET is_ordered = TRUE WHERE cart_id IN (");
        for (int i = 0; i < cartIds.size(); i++) {
            query.append("?");
            if (i < cartIds.size() - 1) {
                query.append(",");
            }
        }
        query.append(")");

        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < cartIds.size(); i++) {
                preparedStatement.setObject(i + 1, cartIds.get(i), Types.OTHER);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Помилка при позначенні елементів кошика як замовлених: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Метод для отримання елементів кошика користувача, які ще не замовлені
    public List<Cart> findUnorderedByUserId(UUID userId) {
        List<Cart> cartItems = new ArrayList<>();
        String query = "SELECT * FROM Cart WHERE user_id = ? AND is_ordered = FALSE";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, userId, Types.OTHER);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    cartItems.add(mapToCart(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні елементів кошика: " + e.getMessage());
            e.printStackTrace();
        }
        return cartItems;
    }

    // Метод для перевірки чи існує товар у кошику користувача
    public boolean existsByUserIdAndItemId(UUID userId, UUID itemId) {
        String query = "SELECT COUNT(*) FROM Cart WHERE user_id = ? AND item_id = ? AND is_ordered = FALSE";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, userId, Types.OTHER);
            preparedStatement.setObject(2, itemId, Types.OTHER);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при перевірці існування товару в кошику: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private Cart mapToCart(ResultSet resultSet) throws SQLException {
        return new Cart(
            UUID.fromString(resultSet.getString("cart_id")),
            UUID.fromString(resultSet.getString("user_id")),
            UUID.fromString(resultSet.getString("item_id")),
            resultSet.getInt("quantity"),
            resultSet.getDouble("subtotal"),
            resultSet.getBoolean("is_ordered")
        );
    }
}