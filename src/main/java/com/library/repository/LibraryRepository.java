package com.library.repository;

import com.library.database.Database;
import com.library.exception.LibraryException;
import com.library.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** JDBC mapping only. Services decide permissions and circulation rules. */
public final class LibraryRepository {
  private final Database db;

  public LibraryRepository(Database db) {
    this.db = db;
  }

  public Database database() {
    return db;
  }

  @FunctionalInterface
  public interface Mapper<T> {
    T map(ResultSet r) throws SQLException;
  }

  // OOP: Collections - result rows become typed objects in a mutable ArrayList.
  public static <T> List<T> query(Connection c, String sql, Mapper<T> mapper, Object... args)
      throws SQLException {
    try (PreparedStatement p = c.prepareStatement(sql)) {
      bind(p, args);
      try (ResultSet r = p.executeQuery()) {
        List<T> results = new ArrayList<>();
        while (r.next()) results.add(mapper.map(r));
        return results;
      }
    }
  }

  public static int execute(Connection c, String sql, Object... args) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(sql)) {
      bind(p, args);
      return p.executeUpdate();
    }
  }

  public static long insert(Connection c, String sql, Object... args) throws SQLException {
    execute(c, sql, args);
    return query(c, "SELECT last_insert_rowid()", r -> r.getLong(1)).get(0);
  }

  private static void bind(PreparedStatement p, Object[] args) throws SQLException {
    for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
  }

  public static User user(ResultSet r) throws SQLException {
    // OOP: Polymorphism - repository returns User, preserving the concrete account type.
    return r.getString("role").equals("LIBRARIAN")
        ? new Librarian(
            r.getLong("user_id"),
            r.getString("full_name"),
            r.getString("username"),
            r.getString("password_hash"),
            r.getBoolean("active"))
        : new Member(
            r.getLong("user_id"),
            r.getString("full_name"),
            r.getString("username"),
            r.getString("password_hash"),
            r.getBoolean("active"));
  }

  public static Book book(ResultSet r) throws SQLException {
    return new Book(
        r.getLong("book_id"),
        r.getString("isbn"),
        r.getString("title"),
        r.getString("author"),
        r.getString("category"));
  }

  public static BookCopy copy(ResultSet r) throws SQLException {
    return new BookCopy(
        r.getLong("copy_id"), r.getLong("book_id"), CopyStatus.valueOf(r.getString("status")));
  }

  public static Loan loan(ResultSet r) throws SQLException {
    String returned = r.getString("return_date");
    return new Loan(
        r.getLong("loan_id"),
        r.getLong("member_id"),
        r.getLong("copy_id"),
        LocalDate.parse(r.getString("issue_date")),
        LocalDate.parse(r.getString("due_date")),
        returned == null ? null : LocalDate.parse(returned));
  }

  public static <T> T one(List<T> rows) {
    if (rows.isEmpty()) throw new LibraryException("The selected record was not found.");
    return rows.get(0);
  }

  public List<User> users() {
    return db.read(
        c -> query(c, "SELECT * FROM users ORDER BY full_name", LibraryRepository::user));
  }

  public User user(long id) {
    return db.read(
        c -> one(query(c, "SELECT * FROM users WHERE user_id=?", LibraryRepository::user, id)));
  }

  public List<Book> books() {
    return db.read(c -> query(c, "SELECT * FROM books ORDER BY title", LibraryRepository::book));
  }

  public List<BookCopy> copies() {
    return db.read(
        c -> query(c, "SELECT * FROM book_copies ORDER BY copy_id", LibraryRepository::copy));
  }

  public List<Loan> loans() {
    return db.read(
        c -> query(c, "SELECT * FROM loans ORDER BY loan_id DESC", LibraryRepository::loan));
  }
}
