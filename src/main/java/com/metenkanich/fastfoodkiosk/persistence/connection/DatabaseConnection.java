package com.metenkanich.fastfoodkiosk.persistence.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DatabaseConnection {

  private static final String JDBC_URL = "jdbc:sqlite:db/fast-food-kiosk.sqlite";
  private static DatabaseConnection instance;
  private static HikariDataSource dataSource;

  private DatabaseConnection() {
    // Приватний конструктор для Singleton
  }

  public static synchronized DatabaseConnection getInstance() {
    if (instance == null) {
      instance = new DatabaseConnection();
      initializeDataSource(); // Ініціалізуємо одразу
    }
    return instance;
  }

  private static void initializeDataSource() {
    if (dataSource == null) {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(JDBC_URL);
      config.setMaximumPoolSize(10);
      config.setMinimumIdle(2);
      config.setConnectionTimeout(30000);
      config.setIdleTimeout(600000);
      config.setMaxLifetime(1800000);
      dataSource = new HikariDataSource(config);
    }
  }

  public DataSource getDataSource() {
    if (dataSource == null) {
      getInstance();
    }
    return dataSource;
  }

  public void closePool() {
    if (dataSource != null) {
      dataSource.close();
      dataSource = null;
    }
  }
}