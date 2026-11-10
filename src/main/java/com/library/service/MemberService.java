package com.library.service;

import static com.library.repository.LibraryRepository.*;

import com.library.exception.LibraryException;
import com.library.model.*;
import com.library.repository.LibraryRepository;
import java.util.*;

public final class MemberService {
  private final LibraryRepository repo;
  private final AuthenticationService auth;
  private final AuthenticationService.Session session;

  public MemberService(
      LibraryRepository repo, AuthenticationService auth, AuthenticationService.Session session) {
    this.repo = repo;
    this.auth = auth;
    this.session = session;
  }

  public List<User> search(String keyword) {
    auth.librarian(session);
    String key = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    return repo.users().stream()
        .filter(u -> u instanceof Member)
        .filter(
            u -> (u.getFullName() + " " + u.getUsername()).toLowerCase(Locale.ROOT).contains(key))
        .toList();
  }

  public User register(String name, String username, String password) {
    auth.librarian(session);
    String n = Validation.required(name, "Full name"),
        u = Validation.required(username, "Username");
    String hash = Passwords.hash(password);
    return repo.database()
        .transaction(
            c -> {
              long id =
                  insert(
                      c,
                      "INSERT INTO users(full_name,username,password_hash,role)"
                          + " VALUES(?,?,?,'MEMBER')",
                      n,
                      u,
                      hash);
              return new Member(id, n, u, hash, true);
            });
  }

  public void update(long id, String name, String username) {
    auth.librarian(session);
    String n = Validation.required(name, "Full name"),
        u = Validation.required(username, "Username");
    repo.database()
        .transaction(
            c -> {
              if (execute(
                      c,
                      "UPDATE users SET full_name=?,username=? WHERE user_id=? AND role='MEMBER'",
                      n,
                      u,
                      id)
                  != 1) throw new LibraryException("Member not found.");
              return null;
            });
  }
}
