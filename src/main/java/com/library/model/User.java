package com.library.model;

// OOP: Abstraction - shared user state; each subclass must identify its role.
public abstract class User {
  // OOP: Encapsulation - callers cannot replace identity or password information.
  private final long userId;
  private String fullName;
  private final String username;
  private final String passwordHash;
  private final boolean active;

  // OOP: Constructor - validates shared account information.
  protected User(
      long userId, String fullName, String username, String passwordHash, boolean active) {
    this.userId = Validation.id(userId);
    this.fullName = Validation.required(fullName, "Full name");
    this.username = Validation.required(username, "Username");
    this.passwordHash = Validation.required(passwordHash, "Password hash");
    this.active = active;
  }

  public abstract String getRole();

  public long getUserId() {
    return userId;
  }

  public String getFullName() {
    return fullName;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public boolean isActive() {
    return active;
  }

  public void updateName(String name) {
    fullName = Validation.required(name, "Full name");
  }

  @Override
  public String toString() {
    return userId + " - " + fullName + " (" + username + ")";
  }
}
