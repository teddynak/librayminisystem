package com.library.service;

import static com.library.repository.LibraryRepository.*;

import com.library.exception.LibraryException;
import com.library.model.*;
import com.library.repository.LibraryRepository;
import java.time.LocalDate;
import java.util.*;

public final class LoanService {
  private final LibraryRepository repo;
  private final AuthenticationService auth;
  private final AuthenticationService.Session session;

  public LoanService(
      LibraryRepository repo, AuthenticationService auth, AuthenticationService.Session session) {
    this.repo = repo;
    this.auth = auth;
    this.session = session;
  }

  public Loan issueCopy(long memberId, long copyId, LocalDate dueDate) {
    auth.librarian(session);
    LocalDate today = LocalDate.now();
    new Loan(1, memberId, copyId, today, dueDate, null); // Validate before opening a transaction.
    return repo.database()
        .transaction(
            c -> {
              User member =
                  one(
                      query(
                          c,
                          "SELECT * FROM users WHERE user_id=? AND role='MEMBER' AND active=1",
                          LibraryRepository::user,
                          memberId));
              BookCopy copy =
                  one(
                      query(
                          c,
                          "SELECT * FROM book_copies WHERE copy_id=?",
                          LibraryRepository::copy,
                          copyId));
              copy.markBorrowed();
              // Conditional update and unique index protect against duplicate issues.
              if (execute(
                      c,
                      "UPDATE book_copies SET status='BORROWED' WHERE copy_id=? AND"
                          + " status='AVAILABLE'",
                      copyId)
                  != 1) throw new LibraryException("This copy is no longer available.");
              long id =
                  insert(
                      c,
                      "INSERT INTO loans(member_id,copy_id,issue_date,due_date,status)"
                          + " VALUES(?,?,?,?,'ACTIVE')",
                      member.getUserId(),
                      copyId,
                      today.toString(),
                      dueDate.toString());
              return new Loan(id, memberId, copyId, today, dueDate, null);
            });
  }

  public void returnCopy(long loanId, LocalDate date) {
    auth.librarian(session);
    repo.database()
        .transaction(
            c -> {
              Loan loan =
                  one(
                      query(
                          c,
                          "SELECT * FROM loans WHERE loan_id=?",
                          LibraryRepository::loan,
                          loanId));
              loan.recordReturn(date);
              if (execute(
                      c,
                      "UPDATE loans SET return_date=?,status='RETURNED' WHERE loan_id=? AND"
                          + " status='ACTIVE'",
                      date.toString(),
                      loanId)
                  != 1) throw new LibraryException("This loan has already been returned.");
              if (execute(
                      c,
                      "UPDATE book_copies SET status='AVAILABLE' WHERE copy_id=? AND"
                          + " status='BORROWED'",
                      loan.getCopyId())
                  != 1)
                throw new LibraryException("Copy state is inconsistent. Return was cancelled.");
              return null;
            });
  }

  public List<Loan> visibleLoans() {
    User user = auth.require(session);
    return repo.loans().stream()
        .filter(l -> user instanceof Librarian || l.getMemberId() == user.getUserId())
        .toList();
  }

  public List<Loan> history(long memberId) {
    User user = auth.require(session);
    if (!(user instanceof Librarian) && user.getUserId() != memberId)
      throw new LibraryException("You may only view your own loans.");
    return repo.loans().stream().filter(l -> l.getMemberId() == memberId).toList();
  }

  public List<Loan> findOverdue(LocalDate asOf) {
    auth.librarian(session);
    if (asOf == null) throw new LibraryException("A date is required.");
    return visibleLoans().stream().filter(l -> l.isOverdue(asOf)).toList();
  }

  public String memberName(long id) {
    // Resolve names only for members whose records are visible to this session.
    if (visibleLoans().stream().noneMatch(l -> l.getMemberId() == id))
      throw new LibraryException("Member not accessible.");
    return repo.user(id).getFullName();
  }
}
