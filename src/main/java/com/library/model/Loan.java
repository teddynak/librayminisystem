package com.library.model;

import com.library.exception.LibraryException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class Loan {
  private final long loanId, memberId, copyId;
  private final LocalDate issueDate, dueDate;
  private LocalDate returnDate;

  public Loan(long id, long member, long copy, LocalDate issue, LocalDate due, LocalDate returned) {
    loanId = Validation.id(id);
    memberId = Validation.id(member);
    copyId = Validation.id(copy);
    if (issue == null || due == null || due.isBefore(issue))
      throw new LibraryException("Due date must be on or after the issue date.");
    if (returned != null && returned.isBefore(issue))
      throw new LibraryException("Invalid return date.");
    issueDate = issue;
    dueDate = due;
    returnDate = returned;
  }

  public long getLoanId() {
    return loanId;
  }

  public long getMemberId() {
    return memberId;
  }

  public long getCopyId() {
    return copyId;
  }

  public LocalDate getIssueDate() {
    return issueDate;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public LocalDate getReturnDate() {
    return returnDate;
  }

  public LoanStatus getStatus() {
    return isActive() ? LoanStatus.ACTIVE : LoanStatus.RETURNED;
  }

  public boolean isActive() {
    return returnDate == null;
  }

  public boolean isOverdue(LocalDate onDate) {
    return isActive() && dueDate.isBefore(onDate);
  }

  public long overdueDays(LocalDate onDate) {
    return isOverdue(onDate) ? ChronoUnit.DAYS.between(dueDate, onDate) : 0;
  }

  public void recordReturn(LocalDate date) {
    if (!isActive()) throw new LibraryException("This loan has already been returned.");
    if (date == null || date.isBefore(issueDate) || date.isAfter(LocalDate.now()))
      throw new LibraryException("Return date must be between the issue date and today.");
    returnDate = date;
  }
}
