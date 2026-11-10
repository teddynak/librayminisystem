package com.library.controller;

import com.library.exception.LibraryException;
import com.library.model.*;
import com.library.repository.LibraryRepository;
import com.library.service.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** JavaFX events and presentation only; service methods enforce permissions and business rules. */
public final class LibraryController {
  private final BorderPane root = new BorderPane();
  private final Label feedback = new Label();
  private final LibraryRepository repo;
  private final AuthenticationService auth;
  private AuthenticationService.Session session;
  private BookService books;
  private MemberService members;
  private LoanService loans;
  private TabPane tabs;

  public LibraryController(LibraryRepository repo, AuthenticationService auth) {
    this.repo = repo;
    this.auth = auth;
    feedback.setId("feedback");
    feedback.setWrapText(true);
    feedback.setPadding(new Insets(12));
    root.setBottom(feedback);
    showLogin();
  }

  public BorderPane root() {
    return root;
  }

  private void action(Runnable operation) {
    try {
      operation.run();
      feedback.setText("Done.");
      feedback.getStyleClass().remove("error");
    }
    // OOP: Exception Handling - show understandable failures without closing the application.
    catch (LibraryException | java.time.DateTimeException e) {
      feedback.setText(e.getMessage());
      if (!feedback.getStyleClass().contains("error")) feedback.getStyleClass().add("error");
    }
  }

  private Button button(String text, String id, Runnable handler) {
    Button b = new Button(text);
    b.setId(id);
    b.setOnAction(e -> action(handler));
    return b;
  }

  private TextField input(String prompt, String id) {
    TextField t = new TextField();
    t.setPromptText(prompt);
    t.setId(id);
    return t;
  }

  private VBox page(String title, Node... nodes) {
    VBox box = new VBox(12);
    box.setPadding(new Insets(18));
    Label heading = new Label(title);
    heading.getStyleClass().add("heading");
    box.getChildren().add(heading);
    box.getChildren().addAll(nodes);
    return box;
  }

  private Node field(String label, Node input) {
    Label caption = new Label(label);
    caption.setLabelFor(input);
    return new VBox(5, caption, input);
  }

  private FlowPane row(Node... nodes) {
    FlowPane row = new FlowPane(12, 10);
    row.getChildren().addAll(nodes);
    return row;
  }

  private <T> TableView<T> table(String id) {
    TableView<T> t = new TableView<>();
    t.setId(id);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    t.setPlaceholder(new Label("No records to display."));
    t.setPrefHeight(260);
    return t;
  }

  private <T> void column(TableView<T> table, String heading, Function<T, Object> value) {
    TableColumn<T, String> c = new TableColumn<>(heading);
    c.setCellValueFactory(
        data -> new ReadOnlyStringWrapper(Objects.toString(value.apply(data.getValue()), "")));
    c.setPrefWidth(heading.equals("Title") || heading.equals("Book") ? 230 : 125);
    c.setCellFactory(
        ignored ->
            new TableCell<>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTooltip(empty || item == null || item.isBlank() ? null : new Tooltip(item));
              }
            });
    table.getColumns().add(c);
  }

  private <T> T selected(TableView<T> table) {
    T item = table.getSelectionModel().getSelectedItem();
    if (item == null) throw new LibraryException("Select a record first.");
    return item;
  }

  private void showLogin() {
    session = null;
    root.setTop(null);
    boolean setup = auth.needsSetup();
    TextField name = input("Full name", "login-name"),
        username = input("Username", "login-username");
    PasswordField password = new PasswordField();
    password.setPromptText(setup ? "At least 8 characters" : "Password");
    password.setId("login-password");
    Button submit =
        button(
            setup ? "Create librarian account" : "Log in",
            "login-submit",
            () -> {
              if (setup) auth.setup(name.getText(), username.getText(), password.getText());
              session = auth.login(username.getText(), password.getText());
              password.clear();
              showWorkspace();
            });
    Label introduction =
        new Label(
            setup
                ? "Create the first librarian account. Your records stay on this computer."
                : "Log in to manage the library or view your loans.");
    introduction.setWrapText(true);
    VBox form = page(setup ? "Set up your library" : "Library Mini System", introduction);
    if (setup) form.getChildren().add(field("Full name", name));
    form.getChildren().addAll(field("Username", username), field("Password", password), submit);
    form.setMaxWidth(440);
    password.setOnAction(e -> submit.fire());
    StackPane wrapper = new StackPane(form);
    root.setCenter(wrapper);
  }

  private void showWorkspace() {
    User user = auth.require(session);
    books = new BookService(repo, auth, session);
    members = new MemberService(repo, auth, session);
    loans = new LoanService(repo, auth, session);
    Label title = new Label("Library Mini System");
    title.getStyleClass().add("heading");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox header =
        new HBox(
            18,
            title,
            spacer,
            new Label(user.getFullName() + " / " + user.getRole()),
            button(
                "Log out",
                "logout",
                () -> {
                  auth.logout(session);
                  showLogin();
                }));
    header.setPadding(new Insets(16));
    root.setTop(header);
    tabs = new TabPane();
    tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    if (user instanceof Librarian) {
      addTab("Summary", this::summary);
      addTab("Books and copies", () -> catalog(true));
      addTab("Members", this::memberPage);
      addTab("Borrowing and returns", () -> loanPage(false));
      addTab("Overdue loans", () -> loanPage(true));
    } else {
      addTab("Book catalog", () -> catalog(false));
      addTab("My loans", () -> loanPage(false));
    }
    root.setCenter(tabs);
  }

  private void addTab(String title, java.util.function.Supplier<Node> content) {
    Tab tab = new Tab(title);
    tab.setContent(content.get());
    tab.setOnSelectionChanged(
        e -> {
          if (tab.isSelected()) action(() -> tab.setContent(content.get()));
        });
    tabs.getTabs().add(tab);
  }

  private Node summary() {
    VBox counts = new VBox(12);
    books
        .summary()
        .forEach((name, value) -> counts.getChildren().add(new Label(name + ": " + value)));
    return page(
        "Library summary",
        counts,
        button(
            "Refresh summary",
            "summary-refresh",
            () -> tabs.getSelectionModel().getSelectedItem().setContent(summary())));
  }

  private Node catalog(boolean librarian) {
    TextField search = input("Title, author, ISBN or category", "book-search");
    ComboBox<BookService.Field> searchField = new ComboBox<>();
    searchField.getItems().addAll(BookService.Field.values());
    searchField.setValue(BookService.Field.ALL);
    searchField.setId("book-search-field");
    TableView<Book> table = table("books-table");
    column(table, "ID", Book::getBookId);
    column(table, "Title", Book::getTitle);
    column(table, "Author", Book::getAuthor);
    column(table, "ISBN", Book::getIsbn);
    column(table, "Category", Book::getCategory);
    Map<Long, Long> available = new HashMap<>();
    column(table, "Available", b -> available.getOrDefault(b.getBookId(), 0L));
    TableView<BookCopy> copies = table("copies-table");
    copies.setPrefHeight(150);
    column(copies, "Copy ID", BookCopy::getCopyId);
    column(copies, "Book ID", BookCopy::getBookId);
    column(copies, "Status", BookCopy::getStatus);
    Runnable refresh =
        () -> {
          available.clear();
          books.copies().stream()
              .filter(BookCopy::isAvailable)
              .forEach(c -> available.merge(c.getBookId(), 1L, Long::sum));
          table.getItems().setAll(books.search(search.getText(), searchField.getValue()));
          copies.getItems().clear();
        };
    search.textProperty().addListener((o, a, b) -> action(refresh));
    searchField.valueProperty().addListener((o, a, b) -> action(refresh));
    VBox box =
        page(
            librarian ? "Books and physical copies" : "Search the book catalog",
            row(
                field("Search", search),
                field("Search field", searchField),
                button("Refresh", "books-refresh", refresh)),
            table);
    if (librarian) {
      TextField isbn = input("ISBN", "book-isbn"),
          title = input("Title", "book-title"),
          author = input("Author", "book-author"),
          category = input("Category", "book-category");
      Button update =
          button(
              "Update selected book",
              "book-update",
              () -> {
                Book b = selected(table);
                books.updateBook(
                    b.getBookId(),
                    isbn.getText(),
                    title.getText(),
                    author.getText(),
                    category.getText());
                refresh.run();
              });
      Button addCopy =
          button(
              "Add physical copy",
              "copy-add",
              () -> {
                Book b = selected(table);
                books.registerCopy(b.getBookId());
                refresh.run();
                table.getSelectionModel().select(b);
                copies
                    .getItems()
                    .setAll(
                        books.copies().stream()
                            .filter(c -> c.getBookId() == b.getBookId())
                            .toList());
              });
      Button deactivate =
          button(
              "Mark copy inactive",
              "copy-deactivate",
              () -> {
                books.deactivate(selected(copies).getCopyId());
                refresh.run();
              });
      update.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
      addCopy.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
      deactivate.disableProperty().bind(copies.getSelectionModel().selectedItemProperty().isNull());
      table
          .getSelectionModel()
          .selectedItemProperty()
          .addListener(
              (o, a, b) -> {
                if (b != null) {
                  isbn.setText(b.getIsbn());
                  title.setText(b.getTitle());
                  author.setText(b.getAuthor());
                  category.setText(b.getCategory());
                  action(
                      () ->
                          copies
                              .getItems()
                              .setAll(
                                  books.copies().stream()
                                      .filter(c -> c.getBookId() == b.getBookId())
                                      .toList()));
                }
              });
      box.getChildren()
          .addAll(
              row(
                  field("ISBN", isbn),
                  field("Title", title),
                  field("Author", author),
                  field("Category", category)),
              row(
                  button(
                      "Add new book",
                      "book-add",
                      () -> {
                        books.addBook(
                            isbn.getText(), title.getText(), author.getText(), category.getText());
                        refresh.run();
                        isbn.clear();
                        title.clear();
                        author.clear();
                        category.clear();
                      }),
                  update,
                  addCopy),
              new Label("Copies of the selected title"),
              copies,
              deactivate);
    }
    refresh.run();
    return scroll(box);
  }

  private Node memberPage() {
    TextField search = input("Name or username", "member-search");
    TableView<User> table = table("members-table");
    column(table, "ID", User::getUserId);
    column(table, "Full name", User::getFullName);
    column(table, "Username", User::getUsername);
    TextField name = input("Full name", "member-name"),
        username = input("Username", "member-username");
    PasswordField password = new PasswordField();
    password.setId("member-password");
    password.setPromptText("8 to 128 characters; new members only");
    Runnable refresh = () -> table.getItems().setAll(members.search(search.getText()));
    search.textProperty().addListener((o, a, b) -> action(refresh));
    TableView<Loan> history = loanTable("member-history");
    history.setPrefHeight(180);
    table
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (o, a, b) -> {
              if (b != null) {
                name.setText(b.getFullName());
                username.setText(b.getUsername());
                action(() -> history.getItems().setAll(loans.history(b.getUserId())));
              }
            });
    Button update =
        button(
            "Update selected member",
            "member-update",
            () -> {
              members.update(selected(table).getUserId(), name.getText(), username.getText());
              refresh.run();
              history.getItems().clear();
            });
    update.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
    VBox box =
        page(
            "Library members",
            field("Search", search),
            table,
            row(
                field("Full name", name),
                field("Username", username),
                field("Initial password", password)),
            row(
                button(
                    "Register new member",
                    "member-add",
                    () -> {
                      members.register(name.getText(), username.getText(), password.getText());
                      password.clear();
                      name.clear();
                      username.clear();
                      refresh.run();
                    }),
                update),
            new Label("Selected member borrowing history"),
            history);
    refresh.run();
    return scroll(box);
  }

  private TableView<Loan> loanTable(String id) {
    TableView<Loan> table = table(id);
    Map<Long, String> titles = new HashMap<>();
    books.search("").forEach(b -> titles.put(b.getBookId(), b.getTitle()));
    Map<Long, Long> copyBooks = new HashMap<>();
    books.copies().forEach(c -> copyBooks.put(c.getCopyId(), c.getBookId()));
    column(table, "Loan", Loan::getLoanId);
    column(table, "Member", l -> loans.memberName(l.getMemberId()));
    column(table, "Book", l -> titles.get(copyBooks.get(l.getCopyId())));
    column(table, "Copy", Loan::getCopyId);
    column(table, "Issued", Loan::getIssueDate);
    column(table, "Due", Loan::getDueDate);
    column(table, "Returned", Loan::getReturnDate);
    column(table, "Status", Loan::getStatus);
    column(table, "Days overdue", l -> l.overdueDays(LocalDate.now()));
    return table;
  }

  private Node loanPage(boolean overdue) {
    boolean librarian = auth.require(session) instanceof Librarian;
    TableView<Loan> table = loanTable(overdue ? "overdue-table" : "loans-table");
    ComboBox<String> filter = new ComboBox<>();
    filter.getItems().addAll("All loans", "Active loans", "Returned loans");
    filter.setValue("All loans");
    filter.setId("loan-filter");
    Runnable refresh =
        () ->
            table
                .getItems()
                .setAll(
                    (overdue ? loans.findOverdue(LocalDate.now()) : loans.visibleLoans())
                        .stream()
                            .filter(
                                l ->
                                    filter.getValue().equals("All loans")
                                        || (filter.getValue().equals("Active loans")
                                            == l.isActive()))
                            .toList());
    filter.valueProperty().addListener((o, a, b) -> action(refresh));
    VBox box =
        page(
            overdue
                ? "Overdue loans"
                : librarian ? "Borrowing and returns" : "My borrowing records",
            row(field("Show", filter), button("Refresh loans", "loans-refresh", refresh)),
            table);
    if (librarian && !overdue) {
      ComboBox<User> member = new ComboBox<>();
      member.setId("issue-member");
      member.setPromptText("Select member");
      member.getItems().setAll(members.search(""));
      ComboBox<BookCopy> copy = new ComboBox<>();
      copy.setId("issue-copy");
      copy.setPromptText("Select available copy");
      // Copy labels include title to make physical-copy selection practical.
      Map<Long, String> titles = new HashMap<>();
      books.search("").forEach(b -> titles.put(b.getBookId(), b.getTitle()));
      copy.setConverter(
          new javafx.util.StringConverter<>() {
            @Override
            public String toString(BookCopy c) {
              return c == null ? "" : "Copy " + c.getCopyId() + " - " + titles.get(c.getBookId());
            }

            @Override
            public BookCopy fromString(String text) {
              throw new UnsupportedOperationException("Selection only");
            }
          });
      Runnable copies =
          () ->
              copy.getItems()
                  .setAll(books.copies().stream().filter(BookCopy::isAvailable).toList());
      copies.run();
      DatePicker due = new DatePicker(LocalDate.now().plusDays(14));
      due.setId("issue-due");
      due.setEditable(false);
      Button issue =
          button(
              "Issue copy",
              "loan-issue",
              () -> {
                loans.issueCopy(
                    member.getValue().getUserId(), copy.getValue().getCopyId(), due.getValue());
                copies.run();
                refresh.run();
              });
      issue
          .disableProperty()
          .bind(
              member
                  .valueProperty()
                  .isNull()
                  .or(copy.valueProperty().isNull())
                  .or(due.valueProperty().isNull()));
      Button returned =
          button(
              "Return selected loan",
              "loan-return",
              () -> {
                loans.returnCopy(selected(table).getLoanId(), LocalDate.now());
                copies.run();
                refresh.run();
              });
      returned
          .disableProperty()
          .bind(
              javafx.beans.binding.Bindings.createBooleanBinding(
                  () ->
                      table.getSelectionModel().getSelectedItem() == null
                          || !table.getSelectionModel().getSelectedItem().isActive(),
                  table.getSelectionModel().selectedItemProperty()));
      box.getChildren()
          .addAll(
              row(field("Member", member), field("Available copy", copy), field("Due date", due)),
              row(issue, returned));
    }
    refresh.run();
    return scroll(box);
  }

  private ScrollPane scroll(Node node) {
    ScrollPane pane = new ScrollPane(node);
    pane.setFitToWidth(true);
    return pane;
  }
}
