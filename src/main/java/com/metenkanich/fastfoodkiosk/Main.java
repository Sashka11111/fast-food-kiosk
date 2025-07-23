package com.metenkanich.fastfoodkiosk;

import atlantafx.base.theme.PrimerLight;
import com.metenkanich.fastfoodkiosk.persistence.connection.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

  private static DatabaseConnection databaseConnection;

  @Override
  public void start(Stage primaryStage) throws Exception {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/burger.png")));
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/authorization.fxml"));
    Parent root = loader.load();
    primaryStage.initStyle(StageStyle.UNDECORATED);
    primaryStage.setScene(new Scene(root, 800, 500));
    primaryStage.setResizable(false);

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
    // Ініціалізуємо DatabaseConnection на початку
    databaseConnection = DatabaseConnection.getInstance();
    try {
      launch(args);
    } finally {
      if (databaseConnection != null) {
        databaseConnection.closePool();
      }
    }
  }
}