package com.metenkanich.fastfoodkiosk.persistence.repository.impl;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Order;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.OrderStatus;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.OrderRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;

public class OrderRepositoryImpl implements OrderRepository {
  private final DataSource dataSource;
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public OrderRepositoryImpl(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Order findById(UUID id) throws EntityNotFoundException {
    String query = "SELECT * FROM Orders WHERE order_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setObject(1, id, Types.OTHER);
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
  public List<UUID> findCartIdsByOrderId(UUID orderId) {
    List<UUID> cartIds = new ArrayList<>();

    String sql = "SELECT DISTINCT p.cart_id FROM Payments p " +
                "INNER JOIN Cart c ON p.cart_id = c.cart_id " +
                "INNER JOIN Orders o ON c.user_id = o.user_id " +
                "WHERE o.order_id = ? AND c.is_ordered = TRUE " +
                "AND datetime(p.created_at) >= datetime(o.created_at, '-2 minutes') " +
                "AND datetime(p.created_at) <= datetime(o.created_at, '+2 minutes')";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setObject(1, orderId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String cartIdStr = rs.getString("cart_id");
          if (cartIdStr != null) {
            cartIds.add(UUID.fromString(cartIdStr));
          }
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching cart IDs through Payments for order " + orderId + ": " + e.getMessage());
    }

    if (cartIds.isEmpty()) {
      cartIds = findCartIdsByOrderIdAlternative(orderId);
    }

    return cartIds;
  }

  private List<UUID> findCartIdsByOrderIdAlternative(UUID orderId) {
    List<UUID> cartIds = new ArrayList<>();
    String sql = "SELECT c.cart_id FROM Cart c " +
                "INNER JOIN Orders o ON c.user_id = o.user_id " +
                "WHERE o.order_id = ? AND c.is_ordered = TRUE";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setObject(1, orderId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String cartIdStr = rs.getString("cart_id");
          if (cartIdStr != null) {
            cartIds.add(UUID.fromString(cartIdStr));
          }
        }
      }
    } catch (SQLException e) {
      System.err.println("Error in alternative cart ID search for order " + orderId + ": " + e.getMessage());
      e.printStackTrace();
    }

    return cartIds;
  }
  @Override
  public Order create(Order order) {
    String query = "INSERT INTO Orders (order_id, user_id, total_price, status, created_at) VALUES (?, ?, ?, ?, ?)";
    System.out.println("DEBUG OrderRepository: Створюємо замовлення з query: " + query);
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      UUID id = UUID.randomUUID();
      System.out.println("DEBUG OrderRepository: order ID = " + id);
      preparedStatement.setObject(1, id, Types.OTHER);
      preparedStatement.setObject(2, order.userId(), Types.OTHER);
      preparedStatement.setBigDecimal(3, order.totalPrice());
      preparedStatement.setString(4, order.status().name());
      preparedStatement.setObject(5, order.createdAt());
      System.out.println("DEBUG OrderRepository: Виконуємо executeUpdate()");
      preparedStatement.executeUpdate();
      System.out.println("DEBUG OrderRepository: executeUpdate() виконано успішно");
      return new Order(id, order.userId(), order.totalPrice(), order.status(), order.createdAt());
    } catch (SQLException e) {
      System.err.println("DEBUG OrderRepository: SQL Exception при створенні: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public Order update(Order order) throws EntityNotFoundException {
    if (order.orderId() == null) {
      throw new EntityNotFoundException("ID замовлення не може бути null для оновлення");
    }
    String query = "UPDATE Orders SET user_id = ?, total_price = ?, status = ?, created_at = ? WHERE order_id = ?";
    System.out.println("DEBUG OrderRepository: Оновлюємо замовлення з query: " + query);
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      System.out.println("DEBUG OrderRepository: order ID = " + order.orderId());
      preparedStatement.setObject(1, order.userId(), Types.OTHER);
      preparedStatement.setBigDecimal(2, order.totalPrice());
      preparedStatement.setString(3, order.status().name());
      preparedStatement.setObject(4, order.createdAt());
      preparedStatement.setObject(5, order.orderId(), Types.OTHER);
      System.out.println("DEBUG OrderRepository: Виконуємо executeUpdate()");
      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows == 0) {
        throw new EntityNotFoundException("Замовлення з ID " + order.orderId() + " не знайдено для оновлення");
      }
      System.out.println("DEBUG OrderRepository: executeUpdate() виконано успішно");
      return order;
    } catch (SQLException e) {
      System.err.println("DEBUG OrderRepository: SQL Exception при оновленні: " + e.getMessage());
      e.printStackTrace();
      throw new EntityNotFoundException("Помилка під час оновлення замовлення з ID " + order.orderId(), e);
    }
  }

  @Override
  public void deleteById(UUID id) throws EntityNotFoundException {
    String query = "DELETE FROM Orders WHERE order_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setObject(1, id, Types.OTHER);
      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows == 0) {
        throw new EntityNotFoundException("Замовлення з ID " + id + " не знайдено");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private Order mapToOrder(ResultSet resultSet) throws SQLException {
    String createdAtStr = resultSet.getString("created_at");
    LocalDateTime createdAt;

    try {
      createdAt = LocalDateTime.parse(createdAtStr, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      try {
        createdAt = LocalDateTime.parse(createdAtStr);
      } catch (DateTimeParseException e2) {
        createdAt = LocalDateTime.now();
      }
    }
    return new Order(
        UUID.fromString(resultSet.getString("order_id")),
        UUID.fromString(resultSet.getString("user_id")),
        resultSet.getBigDecimal("total_price"),
        OrderStatus.valueOf(resultSet.getString("status")),
        createdAt
    );
  }
}