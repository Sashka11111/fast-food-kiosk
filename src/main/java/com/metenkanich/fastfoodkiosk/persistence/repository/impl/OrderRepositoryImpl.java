package com.metenkanich.fastfoodkiosk.persistence.repository.impl;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Order;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.OrderStatus;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.OrderRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;

public class OrderRepositoryImpl implements OrderRepository {
  private final DataSource dataSource;

  public OrderRepositoryImpl(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Order findById(UUID id) throws EntityNotFoundException {
    String query = "SELECT * FROM Orders WHERE order_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, id.toString());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return mapToOrder(resultSet);
        } else {
          throw new EntityNotFoundException("Замовлення з ID " + id + " не знайдено");
        }
      }
    } catch (SQLException e) {
      throw new EntityNotFoundException("Помилка під час пошуку замовлення з ID " + id, e);
    }
  }

  @Override
  public List<Order> findAll() {
    List<Order> orders = new ArrayList<>();
    String query = "SELECT * FROM Orders";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery()) {
      while (resultSet.next()) {
        orders.add(mapToOrder(resultSet));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return orders;
  }

  @Override
  public Order save(Order order) {
    String query = order.orderId() == null
        ? "INSERT INTO Orders (order_id, user_id, total_price, status, created_at) VALUES (?, ?, ?, ?, ?)"
        : "UPDATE Orders SET user_id = ?, total_price = ?, status = ?, created_at = ? WHERE order_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      UUID id = order.orderId() == null ? UUID.randomUUID() : order.orderId();
      int index = 1;
      if (order.orderId() == null) {
        preparedStatement.setString(index++, id.toString());
      }
      preparedStatement.setString(index++, order.userId().toString());
      preparedStatement.setBigDecimal(index++, order.totalPrice());
      preparedStatement.setString(index++, order.status().name());
      preparedStatement.setObject(index++, order.createdAt());
      if (order.orderId() != null) {
        preparedStatement.setString(index, id.toString());
      }
      preparedStatement.executeUpdate();
      return new Order(id, order.userId(), order.totalPrice(), order.status(), order.createdAt());
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void deleteById(UUID id) throws EntityNotFoundException {
    String query = "DELETE FROM Orders WHERE order_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, id.toString());
      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows == 0) {
        throw new EntityNotFoundException("Замовлення з ID " + id + " не знайдено");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private Order mapToOrder(ResultSet resultSet) throws SQLException {
    return new Order(
        UUID.fromString(resultSet.getString("order_id")),
        UUID.fromString(resultSet.getString("user_id")),
        resultSet.getBigDecimal("total_price"),
        OrderStatus.valueOf(resultSet.getString("status")),
        resultSet.getObject("created_at", LocalDateTime.class)
    );
  }
}