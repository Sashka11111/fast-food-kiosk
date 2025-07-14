package com.metenkanich.fastfoodkiosk.persistence.repository.impl;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.Role;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.UserRepository;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.format.DateTimeFormatter;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserRepositoryImpl implements UserRepository {
  private final DataSource dataSource;
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  public UserRepositoryImpl(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public User findById(UUID id) throws EntityNotFoundException {
    String query = "SELECT * FROM Users WHERE user_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setObject(1, id, Types.OTHER); // Use setObject for UUID
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return mapToUser(resultSet);
        } else {
          throw new EntityNotFoundException("Користувача з ID " + id + " не знайдено");
        }
      }
    } catch (SQLException e) {
      throw new EntityNotFoundException("Помилка під час пошуку користувача з ID " + id, e);
    }
  }

  @Override
  public User findByUsername(String username) throws EntityNotFoundException {
    String query = "SELECT * FROM Users WHERE username = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, username);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return mapToUser(resultSet);
        } else {
          throw new EntityNotFoundException("Користувача з ім'ям " + username + " не знайдено");
        }
      }
    } catch (SQLException e) {
      throw new EntityNotFoundException("Помилка під час пошуку користувача з ім'ям " + username, e);
    }
  }

  @Override
  public List<User> findAll() {
    List<User> users = new ArrayList<>();
    String query = "SELECT * FROM Users";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery()) {
      while (resultSet.next()) {
        users.add(mapToUser(resultSet));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return users;
  }

  @Override
  public void addUser(User user) {
    String query = "INSERT INTO Users (user_id, username, password, role, email, created_at) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      UUID id = UUID.randomUUID();
      preparedStatement.setObject(1, id, Types.OTHER); // Для UUID
      preparedStatement.setString(2, user.username());
      preparedStatement.setString(3, user.password());
      preparedStatement.setString(4, user.role().name());
      preparedStatement.setString(5, user.email());
      preparedStatement.setTimestamp(6, Timestamp.valueOf(user.createdAt())); // Використовуємо Timestamp
      preparedStatement.executeUpdate();
      new User(id, user.username(), user.password(), user.role(), user.email(), user.createdAt());
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void updateUser(User user) throws EntityNotFoundException {
    String query = "UPDATE Users SET username = ?, password = ?, role = ?, email = ? WHERE user_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, user.username());
      preparedStatement.setString(2, user.password());
      preparedStatement.setString(3, user.role().name());
      preparedStatement.setString(4, user.email());
      preparedStatement.setObject(5, user.id(), Types.OTHER); // Use setObject for UUID
      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows == 0) {
        throw new EntityNotFoundException("Користувача з ID " + user.id() + " не знайдено");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Помилка при оновленні користувача: " + e.getMessage(), e);
    }
  }

  @Override
  public void updateUserRole(String username, Role newRole) throws EntityNotFoundException {
    String query = "UPDATE Users SET role = ? WHERE username = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, newRole.toString());
      preparedStatement.setString(2, username);
      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows == 0) {
        throw new EntityNotFoundException("Користувача з ім'ям " + username + " не знайдено");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deleteUser(String username) throws EntityNotFoundException {
    String query = "DELETE FROM Users WHERE username = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, username);
      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows == 0) {
        throw new EntityNotFoundException("Користувача з ім'ям " + username + " не знайдено");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private User mapToUser(ResultSet resultSet) throws SQLException {
    LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();

    return new User(
        UUID.fromString(resultSet.getString("user_id")),
        resultSet.getString("username"),
        resultSet.getString("password"),
        Role.valueOf(resultSet.getString("role")),
        resultSet.getString("email"),
        createdAt
    );
  }
}