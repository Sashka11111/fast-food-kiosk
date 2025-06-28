package com.metenkanich.fastfoodkiosk.persistence.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DatabaseConnection {

  private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/fast_food_kiosk";
  private static final String USERNAME = "postgres";
  private static final String PASSWORD = "";

  private static DatabaseConnection instance;
  private static HikariDataSource dataSource;

  private DatabaseConnection() {
  }

  public static synchronized DatabaseConnection getInstance() {
    if (instance == null) {
      instance = new DatabaseConnection();
    }
    return instance;
  }

  public static void initializeDataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(JDBC_URL);
    config.setUsername(USERNAME);
    config.setPassword(PASSWORD);
    dataSource = new HikariDataSource(config);
  }

  public DataSource getDataSource() {
    if (dataSource == null) {
      initializeDataSource(); // Ініціалізація джерела даних
    }
    return dataSource;
  }

  public void closePool() {
    if (dataSource != null) {
      dataSource.close();
    }
  }
}
