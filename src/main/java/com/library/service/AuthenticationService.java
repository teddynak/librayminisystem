package com.library.service;

import static com.library.repository.LibraryRepository.*;

import com.library.exception.LibraryException;
import com.library.model.*;
import com.library.repository.LibraryRepository;
import java.util.*;

public final class AuthenticationService {
  private final LibraryRepository repo;
  private final Set<Session> sessions = Collections.newSetFromMap(new IdentityHashMap<>());

  public AuthenticationService(LibraryRepository repo) {
    this.repo = repo;
  }

  /** Only successful authentication can construct a session; a forged User is insufficient. */
  public static final class Session {
    private final long userId;

    private Session(long id) {
      userId = id;
    }
  }

  public boolean needsSetup() {
    return repo.users().isEmpty();
  }

  public void setup(String name, String username, String password) {
    String n = Validation.required(name, "Full name"),
        u = Validation.required(username, "Username");
    String hash = Passwords.hash(password);
    repo.database()
        .transaction(
            c -> {
              if (!query(c, "SELECT user_id FROM users", r -> r.getLong(1)).isEmpty())
                throw new LibraryException("Initial setup is already complete.");
              insert(
                  c,
                  "INSERT INTO users(full_name,username,password_hash,role)"
                      + " VALUES(?,?,?,'LIBRARIAN')",
                  n,
                  u,
                  hash);
              return null;
            });
  }

  public Session login(String username, String password) {
    String u = Validation.required(username, "Username");
    List<User> found =
        repo.database()
            .read(
                c ->
                    query(
                        c,
                        "SELECT * FROM users WHERE username=? AND active=1",
                        LibraryRepository::user,
                        u));
    if (found.isEmpty() || !Passwords.verify(password, found.get(0).getPasswordHash()))
      throw new LibraryException("Incorrect username or password.");
    Session session = new Session(found.get(0).getUserId());
    sessions.add(session);
    return session;
  }

  public User require(Session session) {
    if (session == null || !sessions.contains(session))
      throw new LibraryException("Please log in again.");
    User user = repo.user(session.userId);
    if (!user.isActive()) throw new LibraryException("This account is inactive.");
    return user;
  }

  public User librarian(Session session) {
    User user = require(session);
    if (!user.getRole().equals("LIBRARIAN"))
      throw new LibraryException("Only a librarian may perform this operation.");
    return user;
  }

  public void logout(Session session) {
    sessions.remove(session);
  }
}
