package com.library;

import static org.junit.jupiter.api.Assertions.*;

import com.library.controller.LibraryController;
import com.library.database.Database;
import com.library.model.*;
import com.library.repository.LibraryRepository;
import com.library.service.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/** Real JavaFX controls exercised on the JavaFX application thread, using a temporary database. */
@Tag("ui")
class JavaFxWorkflowTest {
  @TempDir Path directory;

  static {
    System.setProperty("glass.platform", "Monocle");
    System.setProperty("monocle.platform", "Headless");
    System.setProperty("prism.order", "sw");
  }

  @BeforeAll
  static void toolkit() throws Exception {
    CountDownLatch ready = new CountDownLatch(1);
    Platform.startup(ready::countDown);
    assertTrue(ready.await(15, TimeUnit.SECONDS));
    Platform.setImplicitExit(false);
  }

  @AfterAll
  static void stop() {
    Platform.exit();
  }

  @Test
  void fullLibrarianAndMemberWorkflow() throws Exception {
    onFx(
        () -> {
          LibraryRepository repo = new LibraryRepository(new Database(directory.resolve("ui.db")));
          AuthenticationService auth = new AuthenticationService(repo);
          LibraryController ui = new LibraryController(repo, auth);
          Stage stage = new Stage();
          Scene scene = new Scene(ui.root(), 1100, 1000);
          scene
              .getStylesheets()
              .add(Main.class.getResource("/styles/library.css").toExternalForm());
          stage.setScene(scene);
          stage.show();
          try {
            text(scene, "login-name", "Test Librarian");
            text(scene, "login-username", "admin");
            text(scene, "login-password", "Password123");
            click(scene, "login-submit");
            assertEquals(1, repo.users().size());
            snapshot(scene, "01-summary");
            tab(scene, "Books and copies");
            click(scene, "book-add");
            assertTrue(label(scene).contains("required"));
            text(scene, "book-isbn", "9780134685991");
            text(scene, "book-title", "Effective Java");
            text(scene, "book-author", "Joshua Bloch");
            text(scene, "book-category", "Computing");
            click(scene, "book-add");
            TableView<Book> books = table(scene, "books-table");
            assertEquals(1, books.getItems().size());
            books.getSelectionModel().selectFirst();
            click(scene, "copy-add");
            assertEquals(1, repo.copies().size());
            text(scene, "book-title", "Effective Java Third Edition");
            click(scene, "book-update");
            text(scene, "book-search", "missing");
            assertEquals(0, books.getItems().size());
            text(scene, "book-search", "");
            assertEquals(1, books.getItems().size());
            click(scene, "books-refresh");
            books.getSelectionModel().selectFirst();
            snapshot(scene, "02-catalog");
            tab(scene, "Members");
            text(scene, "member-name", "Teddy Member");
            text(scene, "member-username", "teddy");
            text(scene, "member-password", "Member123");
            click(scene, "member-add");
            TableView<User> members = table(scene, "members-table");
            assertEquals(1, members.getItems().size());
            members.getSelectionModel().selectFirst();
            text(scene, "member-name", "Teddy Updated");
            click(scene, "member-update");
            text(scene, "member-search", "updated");
            assertEquals(1, members.getItems().size());
            snapshot(scene, "03-members");
            tab(scene, "Borrowing and returns");
            combo(scene, "issue-member").getSelectionModel().selectFirst();
            combo(scene, "issue-copy").getSelectionModel().selectFirst();
            click(scene, "loan-issue");
            assertEquals(1, repo.loans().size());
            assertEquals(CopyStatus.BORROWED, repo.copies().get(0).getStatus());
            snapshot(scene, "04-issued-loan");
            click(scene, "loans-refresh");
            TableView<Loan> loans = table(scene, "loans-table");
            loans.getSelectionModel().selectFirst();
            click(scene, "loan-return");
            assertFalse(repo.loans().get(0).isActive());
            assertEquals(CopyStatus.AVAILABLE, repo.copies().get(0).getStatus());
            snapshot(scene, "05-returned-loan");
            tab(scene, "Overdue loans");
            assertEquals(0, table(scene, "overdue-table").getItems().size());
            click(scene, "logout");
            text(scene, "login-username", "teddy");
            text(scene, "login-password", "wrong");
            click(scene, "login-submit");
            assertTrue(label(scene).contains("Incorrect"));
            text(scene, "login-password", "Member123");
            click(scene, "login-submit");
            assertEquals(2, ((TabPane) scene.lookup(".tab-pane")).getTabs().size());
            tab(scene, "My loans");
            assertEquals(1, table(scene, "loans-table").getItems().size());
            snapshot(scene, "06-member-history");
            click(scene, "logout");
            text(scene, "login-username", "admin");
            text(scene, "login-password", "Password123");
            click(scene, "login-submit");
            tab(scene, "Books and copies");
            table(scene, "books-table").getSelectionModel().selectFirst();
            table(scene, "copies-table").getSelectionModel().selectFirst();
            click(scene, "copy-deactivate");
            assertEquals(CopyStatus.INACTIVE, repo.copies().get(0).getStatus());
            // Reopen storage and controller to verify records survive a new application session.
            LibraryRepository reopened =
                new LibraryRepository(new Database(directory.resolve("ui.db")));
            assertEquals(1, reopened.loans().size());
            assertEquals(1, reopened.books().size());
          } finally {
            stage.close();
          }
        });
  }

  @Test
  void applicationEntryPointShowsFirstRunSetup() throws Exception {
    onFx(
        () -> {
          String old = System.getProperty("library.db");
          System.setProperty("library.db", directory.resolve("launch.db").toString());
          Stage stage = new Stage();
          try {
            new Main().start(stage);
            assertTrue(stage.isShowing());
            assertEquals(
                "Create librarian account",
                ((Button) stage.getScene().lookup("#login-submit")).getText());
            snapshot(stage.getScene(), "00-first-run-setup");
          } finally {
            stage.close();
            if (old == null) System.clearProperty("library.db");
            else System.setProperty("library.db", old);
          }
        });
  }

  @Test
  void overdueScreenShowsStoredDatesAndDays() throws Exception {
    onFx(
        () -> {
          LibraryRepository repo =
              new LibraryRepository(new Database(directory.resolve("overdue.db")));
          AuthenticationService auth = new AuthenticationService(repo);
          auth.setup("Librarian", "admin", "Password123");
          var session = auth.login("admin", "Password123");
          User member =
              new MemberService(repo, auth, session)
                  .register("Overdue Member", "member", "Member123");
          BookService service = new BookService(repo, auth, session);
          Book book = service.addBook("978-2", "Database Systems", "Author", "IT");
          BookCopy copy = service.registerCopy(book.getBookId());
          repo.database()
              .transaction(
                  c -> {
                    LibraryRepository.execute(
                        c,
                        "UPDATE book_copies SET status='BORROWED' WHERE copy_id=?",
                        copy.getCopyId());
                    LibraryRepository.insert(
                        c,
                        "INSERT INTO loans(member_id,copy_id,issue_date,due_date,status)"
                            + " VALUES(?,?,?,?,'ACTIVE')",
                        member.getUserId(),
                        copy.getCopyId(),
                        LocalDate.now().minusDays(10).toString(),
                        LocalDate.now().minusDays(3).toString());
                    return null;
                  });
          LibraryController ui = new LibraryController(repo, auth);
          Stage stage = new Stage();
          Scene scene = new Scene(ui.root(), 1100, 760);
          scene
              .getStylesheets()
              .add(Main.class.getResource("/styles/library.css").toExternalForm());
          stage.setScene(scene);
          stage.show();
          try {
            text(scene, "login-username", "admin");
            text(scene, "login-password", "Password123");
            click(scene, "login-submit");
            tab(scene, "Overdue loans");
            TableView<Loan> overdue = table(scene, "overdue-table");
            assertEquals(1, overdue.getItems().size());
            assertEquals(3, overdue.getItems().get(0).overdueDays(LocalDate.now()));
            snapshot(scene, "07-overdue-loans");
            click(scene, "loans-refresh");
          } finally {
            stage.close();
          }
        });
  }

  private static String label(Scene s) {
    return ((Label) s.lookup("#feedback")).getText();
  }

  private static void text(Scene s, String id, String value) {
    ((TextInputControl) s.lookup("#" + id)).setText(value);
  }

  private static void click(Scene s, String id) {
    Button b = (Button) s.lookup("#" + id);
    assertNotNull(b, id);
    assertFalse(b.isDisabled(), id);
    b.fire();
  }

  private static void tab(Scene s, String text) {
    TabPane pane = (TabPane) s.lookup(".tab-pane");
    pane.getSelectionModel()
        .select(
            pane.getTabs().stream()
                .filter(t -> t.getText().equals(text))
                .findFirst()
                .orElseThrow());
    s.getRoot().applyCss();
    s.getRoot().layout();
  }

  @SuppressWarnings("unchecked")
  private static <T> TableView<T> table(Scene s, String id) {
    return (TableView<T>) s.lookup("#" + id);
  }

  @SuppressWarnings("unchecked")
  private static ComboBox<Object> combo(Scene s, String id) {
    return (ComboBox<Object>) s.lookup("#" + id);
  }

  private static void snapshot(Scene s, String name) {
    try {
      s.getRoot().applyCss();
      s.getRoot().layout();
      Path out = Path.of("target/ui-evidence", name + ".png");
      Files.createDirectories(out.getParent());
      ImageIO.write(SwingFXUtils.fromFXImage(s.snapshot(null), null), "png", out.toFile());
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private static void onFx(Runnable work) throws Exception {
    FutureTask<Void> task = new FutureTask<>(work, null);
    Platform.runLater(task);
    task.get(90, TimeUnit.SECONDS);
  }
}
