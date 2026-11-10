package com.library;

import com.library.model.*;
import java.util.List;

public final class OopDemo {
  public static void main(String[] args) {
    // OOP: Classes and Objects - this demonstration does not write to the database.
    Book book = new Book(1, "9780134685991", "Effective Java", "Joshua Bloch");
    BookCopy copy = new BookCopy(1, book.getBookId(), CopyStatus.AVAILABLE);
    copy.markBorrowed();
    // OOP: Polymorphism - runtime dispatch through the abstract parent reference.
    List<User> users =
        List.of(
            new Member(1, "Demo Member", "member", "demo-hash", true),
            new Librarian(2, "Demo Librarian", "librarian", "demo-hash", true));
    users.forEach(user -> System.out.println(user.getFullName() + ": " + user.getRole()));
    System.out.println(book + " / " + copy.getStatus());
  }
}
