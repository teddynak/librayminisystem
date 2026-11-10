package com.library;

import static com.library.repository.LibraryRepository.*;
import static org.junit.jupiter.api.Assertions.*;

import com.library.database.Database;
import com.library.exception.LibraryException;
import com.library.model.*;
import com.library.repository.LibraryRepository;
import com.library.service.*;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

class LibraryIntegrationTest {
  @TempDir Path directory;
  LibraryRepository repo;
  AuthenticationService auth;
  AuthenticationService.Session admin;
  BookService books;
  MemberService members;
  LoanService loans;
  User member;
  Book book;
  BookCopy copy;

  @BeforeEach
  void setup() {
    repo = new LibraryRepository(new Database(directory.resolve("test.db")));
    auth = new AuthenticationService(repo);
    auth.setup("Librarian", "admin", "Password123");
    admin = auth.login("admin", "Password123");
    books = new BookService(repo, auth, admin);
    members = new MemberService(repo, auth, admin);
    loans = new LoanService(repo, auth, admin);
    member = members.register("Teddy", "teddy", "Member123");
    book = books.addBook("978-1", "Java OOP", "Author", "Computing");
    copy = books.registerCopy(book.getBookId());
  }

  @Test
  void credentialsAndFirstRunSetup() {
    assertFalse(auth.needsSetup());
    assertThrows(LibraryException.class, () -> auth.setup("Other", "other", "Password123"));
    assertNotNull(auth.login("ADMIN", "Password123"));
    assertThrows(LibraryException.class, () -> auth.login("admin", "wrong"));
    assertThrows(LibraryException.class, () -> auth.login("missing", "Password123"));
  }

  @Test
  void saltedPasswordHashesAndVerification() {
    String a = Passwords.hash("Password123"), b = Passwords.hash("Password123");
    assertNotEquals(a, b);
    assertTrue(Passwords.verify("Password123", a));
    assertFalse(Passwords.verify("wrong", a));
    assertFalse(Passwords.verify("x", "invalid"));
    assertThrows(LibraryException.class, () -> Passwords.hash("short"));
  }

  @Test
  void duplicateUsernameIsRejectedIgnoringCase() {
    assertThrows(
        LibraryException.class, () -> members.register("Duplicate", "TEDDY", "Password123"));
  }

  @Test
  void duplicateIsbnIsRejected() {
    assertThrows(LibraryException.class, () -> books.addBook("978-1", "Other", "Author", "IT"));
  }

  @Test
  void searchOverloadsAndUpdates() {
    assertEquals(1, books.search("java").size());
    assertEquals(1, books.search("author", BookService.Field.AUTHOR).size());
    assertEquals(0, books.search("java", BookService.Field.ISBN).size());
    books.updateBook(book.getBookId(), "978-2", "Databases", "Writer", "IT");
    assertEquals(0, books.search("java").size());
    assertEquals(1, books.search("databases").size());
  }

  @Test
  void memberSearchAndUpdate() {
    members.update(member.getUserId(), "Teddy Updated", "teddy2");
    assertEquals(1, members.search("updated").size());
    assertNotNull(auth.login("teddy2", "Member123"));
    assertThrows(LibraryException.class, () -> members.update(999, "Missing", "missing"));
  }

  @Test
  void successfulIssueAndReturnChangesBothRecords() {
    Loan loan = issue();
    assertEquals(CopyStatus.BORROWED, books.copies().getFirst().getStatus());
    assertTrue(loans.visibleLoans().getFirst().isActive());
    loans.returnCopy(loan.getLoanId(), LocalDate.now());
    assertEquals(CopyStatus.AVAILABLE, books.copies().getFirst().getStatus());
    assertEquals(LoanStatus.RETURNED, loans.visibleLoans().getFirst().getStatus());
    assertNotNull(loans.visibleLoans().getFirst().getReturnDate());
  }

  @Test
  void secondIssueIsRejected() {
    issue();
    assertThrows(LibraryException.class, this::issue);
    assertEquals(1, loans.visibleLoans().size());
  }

  @Test
  void duplicateReturnIsRejected() {
    Loan loan = issue();
    loans.returnCopy(loan.getLoanId(), LocalDate.now());
    assertThrows(LibraryException.class, () -> loans.returnCopy(loan.getLoanId(), LocalDate.now()));
  }

  @Test
  void invalidBorrowerAndDueDateDoNotChangeCopy() {
    assertThrows(
        LibraryException.class, () -> loans.issueCopy(999, copy.getCopyId(), LocalDate.now()));
    assertThrows(
        LibraryException.class,
        () -> loans.issueCopy(adminId(), copy.getCopyId(), LocalDate.now()));
    assertThrows(
        LibraryException.class,
        () -> loans.issueCopy(member.getUserId(), copy.getCopyId(), LocalDate.now().minusDays(1)));
    assertTrue(books.copies().getFirst().isAvailable());
    assertTrue(loans.visibleLoans().isEmpty());
  }

  @Test
  void inactiveCopyCannotBeIssuedAndHistoryIsKept() {
    Loan loan = issue();
    assertThrows(LibraryException.class, () -> books.deactivate(copy.getCopyId()));
    loans.returnCopy(loan.getLoanId(), LocalDate.now());
    books.deactivate(copy.getCopyId());
    assertThrows(LibraryException.class, this::issue);
    assertEquals(1, loans.history(member.getUserId()).size());
  }

  @Test
  void memberCannotCallLibrarianServices() {
    var session = auth.login("teddy", "Member123");
    BookService b = new BookService(repo, auth, session);
    LoanService l = new LoanService(repo, auth, session);
    MemberService m = new MemberService(repo, auth, session);
    assertEquals(1, b.search("").size());
    assertThrows(LibraryException.class, () -> b.addBook("x", "x", "x", "x"));
    assertThrows(LibraryException.class, () -> b.updateBook(book.getBookId(), "x", "x", "x", "x"));
    assertThrows(LibraryException.class, () -> b.registerCopy(book.getBookId()));
    assertThrows(LibraryException.class, () -> b.deactivate(copy.getCopyId()));
    assertThrows(LibraryException.class, b::summary);
    assertThrows(LibraryException.class, () -> m.register("x", "x", "Password123"));
    assertThrows(LibraryException.class, () -> m.update(member.getUserId(), "x", "x"));
    assertThrows(LibraryException.class, () -> m.search(""));
    assertThrows(
        LibraryException.class,
        () -> l.issueCopy(member.getUserId(), copy.getCopyId(), LocalDate.now()));
    assertThrows(LibraryException.class, () -> l.returnCopy(1, LocalDate.now()));
    assertThrows(LibraryException.class, () -> l.findOverdue(LocalDate.now()));
  }

  @Test
  void membersSeeOnlyTheirOwnHistory() {
    issue();
    User other = members.register("Other", "other", "Password123");
    LoanService own = new LoanService(repo, auth, auth.login("teddy", "Member123"));
    LoanService theirs = new LoanService(repo, auth, auth.login("other", "Password123"));
    assertEquals(1, own.visibleLoans().size());
    assertTrue(theirs.visibleLoans().isEmpty());
    assertThrows(LibraryException.class, () -> theirs.history(member.getUserId()));
    assertThrows(LibraryException.class, () -> own.history(other.getUserId()));
  }

  @Test
  void logoutRevokesSessionAndOtherAuthCannotReuseIt() {
    assertThrows(LibraryException.class, () -> new AuthenticationService(repo).require(admin));
    auth.logout(admin);
    assertThrows(LibraryException.class, () -> books.search(""));
    assertThrows(LibraryException.class, () -> auth.require(null));
  }

  @Test
  void recordsSurviveReopeningDatabase() {
    issue();
    LibraryRepository reopened = new LibraryRepository(new Database(directory.resolve("test.db")));
    assertEquals(2, reopened.users().size());
    assertEquals(1, reopened.books().size());
    assertEquals(CopyStatus.BORROWED, reopened.copies().getFirst().getStatus());
    assertEquals(1, reopened.loans().size());
  }

  @Test
  void overdueDatesAndDashboardCountsComeFromStorage() {
    repo.database()
        .transaction(
            c -> {
              execute(
                  c, "UPDATE book_copies SET status='BORROWED' WHERE copy_id=?", copy.getCopyId());
              insert(
                  c,
                  "INSERT INTO loans(member_id,copy_id,issue_date,due_date,status)"
                      + " VALUES(?,?,?,?,'ACTIVE')",
                  member.getUserId(),
                  copy.getCopyId(),
                  LocalDate.now().minusDays(7).toString(),
                  LocalDate.now().minusDays(2).toString());
              return null;
            });
    assertEquals(2, loans.findOverdue(LocalDate.now()).getFirst().overdueDays(LocalDate.now()));
    assertEquals(1L, books.summary().get("Overdue loans"));
    assertEquals(0L, books.summary().get("Available copies"));
  }

  @Test
  void failureDuringIssueRollsBackCopyUpdate() {
    repo.database()
        .transaction(
            c -> {
              execute(
                  c,
                  "CREATE TRIGGER fail_issue BEFORE INSERT ON loans BEGIN SELECT"
                      + " RAISE(ABORT,'injected failure'); END");
              return null;
            });
    assertThrows(LibraryException.class, this::issue);
    assertTrue(books.copies().getFirst().isAvailable());
    assertTrue(loans.visibleLoans().isEmpty());
  }

  @Test
  void failureDuringReturnRollsBackLoanUpdate() {
    Loan loan = issue();
    repo.database()
        .transaction(
            c -> {
              execute(
                  c,
                  "CREATE TRIGGER fail_return BEFORE UPDATE ON book_copies BEGIN SELECT"
                      + " RAISE(ABORT,'injected failure'); END");
              return null;
            });
    assertThrows(LibraryException.class, () -> loans.returnCopy(loan.getLoanId(), LocalDate.now()));
    assertTrue(loans.visibleLoans().getFirst().isActive());
    assertEquals(CopyStatus.BORROWED, books.copies().getFirst().getStatus());
  }

  @Test
  void databasePreventsDuplicateActiveLoansAndForeignKeyViolations() {
    issue();
    assertThrows(
        LibraryException.class,
        () ->
            repo.database()
                .transaction(
                    c ->
                        insert(
                            c,
                            "INSERT INTO loans(member_id,copy_id,issue_date,due_date,status)"
                                + " VALUES(?,?,?,?,'ACTIVE')",
                            member.getUserId(),
                            copy.getCopyId(),
                            LocalDate.now().toString(),
                            LocalDate.now().toString())));
    assertThrows(LibraryException.class, () -> books.registerCopy(999));
  }

  @Test
  void invalidBookAndMemberInputsAreRejected() {
    assertThrows(LibraryException.class, () -> books.addBook("", "Title", "Author", "IT"));
    assertThrows(LibraryException.class, () -> members.register("", "new", "Password123"));
    assertThrows(LibraryException.class, () -> books.updateBook(999, "i", "t", "a", "c"));
  }

  private Loan issue() {
    return loans.issueCopy(member.getUserId(), copy.getCopyId(), LocalDate.now().plusDays(14));
  }

  private long adminId() {
    return auth.require(admin).getUserId();
  }
}
