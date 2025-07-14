package com.metenkanich.fastfoodkiosk;

import atlantafx.base.theme.PrimerLight;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

  private static DatabaseConnection databaseConnection;

  @Override
  public void start(Stage primaryStage) throws Exception {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/authorization.fxml"));
    Parent root = loader.load();
    primaryStage.initStyle(StageStyle.UNDECORATED); // Відключення верхнього меню
    primaryStage.setScene(new Scene(root, 800, 500)); // Set fixed size 1280x800
    primaryStage.setResizable(false); // Disable window resizing

    primaryStage.show();
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    if (databaseConnection != null) {
      databaseConnection.closePool();
    }
  }

  public static void main(String[] args) {
    System.setProperty("file.encoding", "UTF-8");

    databaseConnection = DatabaseConnection.getInstance();
    databaseConnection.initializeDataSource(); // Initialize before launching GUI

    launch(args);

    if (databaseConnection != null) {
      databaseConnection.closePool(); // Close after exit
    }
  }
}