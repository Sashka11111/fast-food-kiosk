package com.metenkanich.fastfoodkiosk.persistence.repository.impl;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.Role;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.UserRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;

public class UserRepositoryImpl implements UserRepository {
  private final DataSource dataSource;

  public UserRepositoryImpl(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public User findById(UUID id) throws EntityNotFoundException {
    String query = "SELECT * FROM Users WHERE user_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, id.toString());
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
  public User save(User user) {
    String query = user.userId() == null
        ? "INSERT INTO Users (user_id, username, password, role, email, created_at) VALUES (?, ?, ?, ?, ?, ?)"
        : "UPDATE Users SET username = ?, password = ?, role = ?, email = ?, created_at = ? WHERE user_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      UUID id = user.userId() == null ? UUID.randomUUID() : user.userId();
      int index = 1;
      if (user.userId() == null) {
        preparedStatement.setString(index++, id.toString());
      }
      preparedStatement.setString(index++, user.username());
      preparedStatement.setString(index++, user.password());
      preparedStatement.setString(index++, user.role().name());
      preparedStatement.setString(index++, user.email());
      preparedStatement.setObject(index++, user.createdAt());
      if (user.userId() != null) {
        preparedStatement.setString(index, id.toString());
      }
      preparedStatement.executeUpdate();
      return new User(id, user.username(), user.password(), user.role(), user.email(), user.createdAt());
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void deleteById(UUID id) throws EntityNotFoundException {
    String query = "DELETE FROM Users WHERE user_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, id.toString());
      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows == 0) {
        throw new EntityNotFoundException("Користувача з ID " + id + " не знайдено");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private User mapToUser(ResultSet resultSet) throws SQLException {
    return new User(
        UUID.fromString(resultSet.getString("user_id")),
        resultSet.getString("username"),
        resultSet.getString("password"),
        Role.valueOf(resultSet.getString("role")),
        resultSet.getString("email"),
        resultSet.getObject("created_at", ZonedDateTime.class)
    );
  }
}