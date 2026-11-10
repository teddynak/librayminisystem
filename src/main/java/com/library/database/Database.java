package com.library.database;

import com.library.exception.LibraryException;
import java.nio.file.*;
import java.sql.*;

/** Opens short-lived connections; each operation owns and closes its connection. */
public final class Database {
  private final String url;

  public Database(Path path) {
    try {
      Path absolute = path.toAbsolutePath();
      if (absolute.getParent() != null) Files.createDirectories(absolute.getParent());
      Class.forName("org.sqlite.JDBC");
      url = "jdbc:sqlite:" + absolute;
      initialize();
    } catch (Exception e) {
      throw new LibraryException(
          "Cannot open library storage. Check the data folder permissions.", e);
    }
  }

  public Connection open() throws SQLException {
    Connection c = DriverManager.getConnection(url);
    try (Statement s = c.createStatement()) {
      s.execute("PRAGMA foreign_keys=ON");
      s.execute("PRAGMA busy_timeout=5000");
    }
    return c;
  }

  @FunctionalInterface
  public interface Work<T> {
    T run(Connection c) throws SQLException;
  }

  // OOP: Data Persistence - transaction boundaries keep loans and copy states consistent.
  public <T> T transaction(Work<T> work) {
    try (Connection c = open()) {
      c.setAutoCommit(false);
      try {
        T result = work.run(c);
        c.commit();
        return result;
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new LibraryException(
          "The record could not be saved. Check for duplicates or a locked database.", e);
    }
  }

  public <T> T read(Work<T> work) {
    try (Connection c = open()) {
      return work.run(c);
    } catch (SQLException e) {
      throw new LibraryException(
          "Cannot read library records. Check the database and try again.", e);
    }
  }

  private void initialize() {
    transaction(
        c -> {
          try (Statement s = c.createStatement()) {
            s.execute(
                """
CREATE TABLE IF NOT EXISTS users (
  user_id INTEGER PRIMARY KEY, full_name TEXT NOT NULL CHECK(length(trim(full_name))>0),
  username TEXT NOT NULL COLLATE NOCASE UNIQUE, password_hash TEXT NOT NULL,
  role TEXT NOT NULL CHECK(role IN ('LIBRARIAN','MEMBER')), active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0,1)))
""");
            s.execute(
                """
                CREATE TABLE IF NOT EXISTS books (
                  book_id INTEGER PRIMARY KEY, isbn TEXT NOT NULL COLLATE NOCASE UNIQUE,
                  title TEXT NOT NULL, author TEXT NOT NULL, category TEXT NOT NULL)
                """);
            s.execute(
                """
                CREATE TABLE IF NOT EXISTS book_copies (
                  copy_id INTEGER PRIMARY KEY, book_id INTEGER NOT NULL REFERENCES books(book_id),
                  status TEXT NOT NULL CHECK(status IN ('AVAILABLE','BORROWED','INACTIVE')))
                """);
            s.execute(
                """
CREATE TABLE IF NOT EXISTS loans (
  loan_id INTEGER PRIMARY KEY, member_id INTEGER NOT NULL REFERENCES users(user_id),
  copy_id INTEGER NOT NULL REFERENCES book_copies(copy_id), issue_date TEXT NOT NULL,
  due_date TEXT NOT NULL CHECK(due_date>=issue_date), return_date TEXT,
  status TEXT NOT NULL CHECK(status IN ('ACTIVE','RETURNED')),
  CHECK((status='ACTIVE' AND return_date IS NULL) OR
        (status='RETURNED' AND return_date IS NOT NULL AND return_date>=issue_date)))
""");
            s.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS one_active_loan_per_copy ON loans(copy_id) WHERE"
                    + " status='ACTIVE'");
            s.execute(
                """
CREATE TRIGGER IF NOT EXISTS loan_member_role BEFORE INSERT ON loans
WHEN NOT EXISTS (SELECT 1 FROM users WHERE user_id=NEW.member_id AND role='MEMBER' AND active=1)
BEGIN SELECT RAISE(ABORT,'Borrower must be an active member'); END
""");
          }
          return null;
        });
  }
}
