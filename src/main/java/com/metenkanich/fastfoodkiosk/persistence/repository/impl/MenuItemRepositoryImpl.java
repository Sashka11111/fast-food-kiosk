package com.metenkanich.fastfoodkiosk.persistence.repository.impl;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PortionSize;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.MenuItemRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;

public class MenuItemRepositoryImpl implements MenuItemRepository {
    private final DataSource dataSource;

    public MenuItemRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public MenuItem findById(UUID id) throws EntityNotFoundException {
        String query = "SELECT * FROM MenuItems WHERE item_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, id, Types.OTHER);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToMenuItem(resultSet);
                } else {
                    throw new EntityNotFoundException("Пункт меню з ID " + id + " не знайдено");
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Помилка під час пошуку пункту меню з ID " + id, e);
        }
    }

    @Override
    public MenuItem findByName(String name) throws EntityNotFoundException {
        String query = "SELECT * FROM MenuItems WHERE name = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToMenuItem(resultSet);
                } else {
                    throw new EntityNotFoundException("Пункт меню з назвою " + name + " не знайдено");
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Помилка під час пошуку пункту меню з назвою " + name, e);
        }
    }

    @Override
    public List<MenuItem> findAll() {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM MenuItems";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                menuItems.add(mapToMenuItem(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка під час отримання всіх пунктів меню", e);
        }
        return menuItems;
    }

    @Override
    public List<MenuItem> findByCategory(UUID categoryId) {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM MenuItems WHERE category_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, categoryId, Types.OTHER); // Use setObject for UUID
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    menuItems.add(mapToMenuItem(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка під час пошуку пунктів меню за категорією " + categoryId, e);
        }
        return menuItems;
    }

    @Override
    public MenuItem create(MenuItem menuItem) {
        String query = "INSERT INTO MenuItems (item_id, name, description, price, category_id, is_available, image_path, default_portion_size) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            UUID id = UUID.randomUUID();
            preparedStatement.setObject(1, id, Types.OTHER);
            preparedStatement.setString(2, menuItem.name());
            if (menuItem.description() == null) {
                preparedStatement.setNull(3, Types.VARCHAR);
            } else {
                preparedStatement.setString(3, menuItem.description());
            }
            preparedStatement.setBigDecimal(4, menuItem.price());
            preparedStatement.setObject(5, menuItem.categoryId(), Types.OTHER);
            preparedStatement.setBoolean(6, menuItem.isAvailable());
            if (menuItem.imagePath() == null) {
                preparedStatement.setNull(7, Types.VARCHAR);
            } else {
                preparedStatement.setString(7, menuItem.imagePath());
            }
            if (menuItem.defaultPortionSize() == null) {
                preparedStatement.setNull(8, Types.VARCHAR);
            } else {
                preparedStatement.setString(8, menuItem.defaultPortionSize().name());
            }
            preparedStatement.executeUpdate();
            return new MenuItem(id, menuItem.name(), menuItem.description(), menuItem.price(),
                menuItem.categoryId(), menuItem.isAvailable(), menuItem.imagePath(), menuItem.defaultPortionSize());
        } catch (SQLException e) {
            throw new RuntimeException("Помилка під час створення пункту меню", e);
        }
    }

    @Override
    public MenuItem update(MenuItem menuItem) throws EntityNotFoundException {
        if (menuItem.itemId() == null) {
            throw new EntityNotFoundException("ID пункту меню не може бути null для оновлення");
        }
        String query = "UPDATE MenuItems SET name = ?, description = ?, price = ?, category_id = ?, is_available = ?, image_path = ?, default_portion_size = ? WHERE item_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, menuItem.name());
            if (menuItem.description() == null) {
                preparedStatement.setNull(2, Types.VARCHAR);
            } else {
                preparedStatement.setString(2, menuItem.description());
            }
            preparedStatement.setBigDecimal(3, menuItem.price());
            preparedStatement.setObject(4, menuItem.categoryId(), Types.OTHER);
            preparedStatement.setBoolean(5, menuItem.isAvailable());
            if (menuItem.imagePath() == null) {
                preparedStatement.setNull(6, Types.VARCHAR);
            } else {
                preparedStatement.setString(6, menuItem.imagePath());
            }
            if (menuItem.defaultPortionSize() == null) {
                preparedStatement.setNull(7, Types.VARCHAR);
            } else {
                preparedStatement.setString(7, menuItem.defaultPortionSize().name());
            }
            preparedStatement.setObject(8, menuItem.itemId(), Types.OTHER);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Пункт меню з ID " + menuItem.itemId() + " не знайдено для оновлення");
            }
            return menuItem;
        } catch (SQLException e) {
            throw new EntityNotFoundException("Помилка під час оновлення пункту меню з ID " + menuItem.itemId(), e);
        }
    }

    @Override
    public void deleteById(UUID id) throws EntityNotFoundException {
        String query = "DELETE FROM MenuItems WHERE item_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, id, Types.OTHER);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Пункт меню з ID " + id + " не знайдено");
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Помилка під час видалення пункту меню з ID " + id, e);
        }
    }

    private MenuItem mapToMenuItem(ResultSet resultSet) throws SQLException {
        String portionSizeStr = resultSet.getString("default_portion_size");
        PortionSize defaultPortionSize = portionSizeStr != null ? PortionSize.valueOf(portionSizeStr) : PortionSize.MEDIUM;

        return new MenuItem(
            UUID.fromString(resultSet.getString("item_id")),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getBigDecimal("price"),
            UUID.fromString(resultSet.getString("category_id")),
            resultSet.getBoolean("is_available"),
            resultSet.getString("image_path"),
            defaultPortionSize
        );
    }
}