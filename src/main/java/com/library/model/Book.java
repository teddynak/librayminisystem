package com.library.model;

// OOP: Classes and Objects - a Book is a title; BookCopy represents physical stock.
public final class Book {
  private final long bookId;
  private String isbn, title, author, category;

  // OOP: Constructor
  public Book(long id, String isbn, String title, String author, String category) {
    bookId = Validation.id(id);
    updateDetails(isbn, title, author, category);
  }

  // OOP: Polymorphism - Constructor Overloading
  public Book(long id, String isbn, String title, String author) {
    this(id, isbn, title, author, "General");
  }

  // Validate all fields before changing any of them.
  public void updateDetails(String isbn, String title, String author, String category) {
    String i = Validation.required(isbn, "ISBN"), t = Validation.required(title, "Title");
    String a = Validation.required(author, "Author"), c = Validation.required(category, "Category");
    this.isbn = i;
    this.title = t;
    this.author = a;
    this.category = c;
  }

  public long getBookId() {
    return bookId;
  }

  public String getIsbn() {
    return isbn;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
  }

  public String getCategory() {
    return category;
  }

  @Override
  public String toString() {
    return bookId + " - " + title;
  }
}
