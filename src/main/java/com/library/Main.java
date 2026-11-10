package com.library;

import com.library.controller.LibraryController;
import com.library.database.Database;
import com.library.repository.LibraryRepository;
import com.library.service.AuthenticationService;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public final class Main extends Application {
  @Override
  public void start(Stage stage) {
    stage.setTitle("Library Mini System");
    try {
      Path path = Path.of(System.getProperty("library.db", "data/library.db"));
      LibraryRepository repo = new LibraryRepository(new Database(path));
      LibraryController controller = new LibraryController(repo, new AuthenticationService(repo));
      Scene scene = new Scene(controller.root(), 1100, 760);
      scene.getStylesheets().add(Main.class.getResource("/styles/library.css").toExternalForm());
      stage.setScene(scene);
      stage.setMinWidth(900);
      stage.setMinHeight(650);
    } catch (RuntimeException e) {
      Label error =
          new Label(
              "Cannot start the library. Check the database path and permissions.\n"
                  + e.getMessage());
      error.setWrapText(true);
      stage.setScene(new Scene(error, 700, 180));
    }
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
