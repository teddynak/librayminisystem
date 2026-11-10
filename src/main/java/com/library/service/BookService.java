package com.library.service;

import static com.library.repository.LibraryRepository.*;

import com.library.exception.LibraryException;
import com.library.interfaces.Searchable;
import com.library.model.*;
import com.library.repository.LibraryRepository;
import java.util.*;

public final class BookService implements Searchable<Book> {
  public enum Field {
    ALL,
    TITLE,
    AUTHOR,
    ISBN,
    CATEGORY
  }

  private final LibraryRepository repo;
  private final AuthenticationService auth;
  private final AuthenticationService.Session session;

  public BookService(
      LibraryRepository repo, AuthenticationService auth, AuthenticationService.Session session) {
    this.repo = repo;
    this.auth = auth;
    this.session = session;
  }

  // OOP: Interface / Method Overriding - the default search checks all title fields.
  @Override
  public List<Book> search(String keyword) {
    return search(keyword, Field.ALL);
  }

  // OOP: Polymorphism - Method Overloading selects a particular search field.
  public List<Book> search(String keyword, Field field) {
    auth.require(session);
    if (field == null) throw new LibraryException("Choose a search field.");
    String key = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    return repo.books().stream()
        .filter(
            b ->
                switch (field) {
                  case TITLE -> b.getTitle().toLowerCase(Locale.ROOT).contains(key);
                  case AUTHOR -> b.getAuthor().toLowerCase(Locale.ROOT).contains(key);
                  case ISBN -> b.getIsbn().toLowerCase(Locale.ROOT).contains(key);
                  case CATEGORY -> b.getCategory().toLowerCase(Locale.ROOT).contains(key);
                  case ALL ->
                      (b.getTitle()
                              + " "
                              + b.getAuthor()
                              + " "
                              + b.getIsbn()
                              + " "
                              + b.getCategory())
                          .toLowerCase(Locale.ROOT)
                          .contains(key);
                })
        .toList();
  }

  public Book addBook(String isbn, String title, String author, String category) {
    auth.librarian(session);
    Book validated = new Book(1, isbn, title, author, category);
    return repo.database()
        .transaction(
            c -> {
              long id =
                  insert(
                      c,
                      "INSERT INTO books(isbn,title,author,category) VALUES(?,?,?,?)",
                      validated.getIsbn(),
                      validated.getTitle(),
                      validated.getAuthor(),
                      validated.getCategory());
              return new Book(
                  id,
                  validated.getIsbn(),
                  validated.getTitle(),
                  validated.getAuthor(),
                  validated.getCategory());
            });
  }

  public void updateBook(long id, String isbn, String title, String author, String category) {
    auth.librarian(session);
    Book b = new Book(id, isbn, title, author, category);
    repo.database()
        .transaction(
            c -> {
              if (execute(
                      c,
                      "UPDATE books SET isbn=?,title=?,author=?,category=? WHERE book_id=?",
                      b.getIsbn(),
                      b.getTitle(),
                      b.getAuthor(),
                      b.getCategory(),
                      id)
                  != 1) throw new LibraryException("Book not found.");
              return null;
            });
  }

  public List<BookCopy> copies() {
    auth.require(session);
    return repo.copies();
  }

  public BookCopy registerCopy(long bookId) {
    auth.librarian(session);
    Validation.id(bookId);
    return repo.database()
        .transaction(
            c -> {
              long id =
                  insert(
                      c, "INSERT INTO book_copies(book_id,status) VALUES(?,'AVAILABLE')", bookId);
              return new BookCopy(id, bookId, CopyStatus.AVAILABLE);
            });
  }

  public void deactivate(long copyId) {
    auth.librarian(session);
    repo.database()
        .transaction(
            c -> {
              BookCopy copy =
                  one(
                      query(
                          c,
                          "SELECT * FROM book_copies WHERE copy_id=?",
                          LibraryRepository::copy,
                          copyId));
              copy.deactivate();
              execute(c, "UPDATE book_copies SET status='INACTIVE' WHERE copy_id=?", copyId);
              return null;
            });
  }

  public Map<String, Long> summary() {
    auth.librarian(session);
    Map<String, Long> counts = new LinkedHashMap<>();
    List<BookCopy> copies = repo.copies();
    List<Loan> loans = repo.loans();
    counts.put("Book titles", (long) repo.books().size());
    counts.put("Physical copies", (long) copies.size());
    counts.put("Available copies", copies.stream().filter(BookCopy::isAvailable).count());
    counts.put(
        "Borrowed copies",
        copies.stream().filter(c -> c.getStatus() == CopyStatus.BORROWED).count());
    counts.put(
        "Registered members", repo.users().stream().filter(u -> u instanceof Member).count());
    counts.put("Active loans", loans.stream().filter(Loan::isActive).count());
    counts.put(
        "Overdue loans",
        loans.stream().filter(l -> l.isOverdue(java.time.LocalDate.now())).count());
    return counts;
  }
}
