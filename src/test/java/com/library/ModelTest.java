package com.library;

import static org.junit.jupiter.api.Assertions.*;

import com.library.exception.LibraryException;
import com.library.model.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ModelTest {
  @Test
  void bookConstructorsAndValidatedUpdates() {
    Book book = new Book(1, "isbn", "Java", "Author");
    assertEquals("General", book.getCategory());
    book.updateDetails("new", "New title", "New author", "IT");
    assertEquals("New title", book.getTitle());
    assertThrows(LibraryException.class, () -> book.updateDetails("x", "", "author", "IT"));
    assertEquals("New title", book.getTitle());
  }

  @Test
  void invalidIdentityIsRejected() {
    assertThrows(LibraryException.class, () -> new Book(0, "i", "t", "a"));
  }

  @Test
  void requiredFieldsAreRejected() {
    assertThrows(LibraryException.class, () -> new Book(1, "i", " ", "a"));
  }

  @Test
  void polymorphismUsesConcreteRole() {
    User member = new Member(1, "Member", "m", "hash", true),
        librarian = new Librarian(2, "Librarian", "l", "hash", true);
    assertEquals("MEMBER", member.getRole());
    assertEquals("LIBRARIAN", librarian.getRole());
    member.updateName("Changed");
    assertEquals("Changed", member.getFullName());
    assertThrows(LibraryException.class, () -> member.updateName(""));
  }

  @Test
  void copyStateTransitionsAreControlled() {
    BookCopy copy = new BookCopy(1, 1, CopyStatus.AVAILABLE);
    copy.markBorrowed();
    assertFalse(copy.isAvailable());
    assertThrows(LibraryException.class, copy::markBorrowed);
    assertThrows(LibraryException.class, copy::deactivate);
    copy.markAvailable();
    copy.deactivate();
    assertEquals(CopyStatus.INACTIVE, copy.getStatus());
    assertThrows(LibraryException.class, copy::markBorrowed);
  }

  @Test
  void overdueBoundaryAndReturnedLoan() {
    LocalDate today = LocalDate.now();
    Loan loan = new Loan(1, 1, 1, today.minusDays(3), today.minusDays(1), null);
    assertTrue(loan.isOverdue(today));
    assertEquals(1, loan.overdueDays(today));
    assertFalse(loan.isOverdue(today.minusDays(1)));
    loan.recordReturn(today);
    assertEquals(LoanStatus.RETURNED, loan.getStatus());
    assertFalse(loan.isOverdue(today));
    assertThrows(LibraryException.class, () -> loan.recordReturn(today));
  }

  @Test
  void invalidLoanDatesAreRejected() {
    LocalDate today = LocalDate.now();
    assertThrows(LibraryException.class, () -> new Loan(1, 1, 1, today, today.minusDays(1), null));
    Loan loan = new Loan(1, 1, 1, today, today, null);
    assertThrows(LibraryException.class, () -> loan.recordReturn(today.plusDays(1)));
    assertThrows(LibraryException.class, () -> loan.recordReturn(today.minusDays(1)));
  }
}
