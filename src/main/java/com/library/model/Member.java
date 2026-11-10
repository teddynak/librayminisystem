package com.library.model;

// OOP: Inheritance - Member reuses the attributes and validation of User.
public final class Member extends User {
  // OOP: Constructor
  public Member(long id, String name, String username, String hash, boolean active) {
    super(id, name, username, hash, active);
  }

  // OOP: Polymorphism - Method Overriding
  @Override
  public String getRole() {
    return "MEMBER";
  }
}
