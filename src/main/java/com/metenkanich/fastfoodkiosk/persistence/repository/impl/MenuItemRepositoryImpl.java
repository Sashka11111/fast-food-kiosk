package com.metenkanich.fastfoodkiosk.persistence.repository.impl;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.MenuItemRepository;

import java.math.BigDecimal;
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
            preparedStatement.setString(1, id.toString());
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
            e.printStackTrace();
        }
        return menuItems;
    }

    @Override
    public MenuItem save(MenuItem menuItem) {
        String query = menuItem.itemId() == null
            ? "INSERT INTO MenuItems (item_id, name, description, price, category_id, is_available, image) VALUES (?, ?, ?, ?, ?, ?, ?)"
            : "UPDATE MenuItems SET name = ?,寡�, description = ?, price = ?, category_id = ?, is_available = ?, image = ? WHERE item_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            UUID id = menuItem.itemId() == null ? UUID.randomUUID() : menuItem.itemId();
            int index = 1;
            if (menuItem.itemId() == null) {
                preparedStatement.setString(index++, id.toString());
            }
            preparedStatement.setString(index++, menuItem.name());
            if (menuItem.description() == null) {
                preparedStatement.setNull(index++, Types.VARCHAR);
            } else {
                preparedStatement.setString(index++, menuItem.description());
            }
            preparedStatement.setBigDecimal(index++, menuItem.price());
            preparedStatement.setString(index++, menuItem.categoryId().toString());
            preparedStatement.setBoolean(index++, menuItem.isAvailable());
            if (menuItem.image() == null) {
                preparedStatement.setNull(index++, Types.BINARY);
            } else {
                preparedStatement.setBytes(index++, menuItem.image());
            }
            if (menuItem.itemId() != null) {
                preparedStatement.setString(index, id.toString());
            }
            preparedStatement.executeUpdate();
            return new MenuItem(id, menuItem.name(), menuItem.description(), menuItem.price(), menuItem.categoryId(), menuItem.isAvailable(), menuItem.image());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteById(UUID id) throws EntityNotFoundException {
        String query = "DELETE FROM MenuItems WHERE item_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Пункт меню з ID " + id + " не знайдено");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private MenuItem mapToMenuItem(ResultSet resultSet) throws SQLException {
        return new MenuItem(
            UUID.fromString(resultSet.getString("item_id")),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getBigDecimal("price"),
            UUID.fromString(resultSet.getString("category_id")),
            resultSet.getBoolean("is_available"),
            resultSet.getBytes("image")
        );
    }
}