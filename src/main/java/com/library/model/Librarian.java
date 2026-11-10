package com.library.model;

// OOP: Inheritance - Librarian reuses the attributes and validation of User.
public final class Librarian extends User {
  // OOP: Constructor
  public Librarian(long id, String name, String username, String hash, boolean active) {
    super(id, name, username, hash, active);
  }

  // OOP: Polymorphism - Method Overriding
  @Override
  public String getRole() {
    return "LIBRARIAN";
  }
}
