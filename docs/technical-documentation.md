# Technical documentation

## Structure

The project uses Java 21, JavaFX 21.0.5, SQLite JDBC 3.46.1.3, Maven and JUnit 5.11.3. `Main` starts the desktop application. `LibraryController` builds JavaFX controls in Java, without FXML, and handles navigation and input presentation. JavaFX CSS is in `src/main/resources/styles/library.css`.

The six domain classes represent users, titles, copies and loans. Four services hold account, book, member and circulation rules. One small `LibraryRepository` maps JDBC rows to objects. `Database` owns connection configuration, schema creation and transaction handling. This keeps SQL and borrowing rules out of the JavaFX controller without a separate repository interface for every table.

## Database

| Table | Key fields | Rules |
|---|---|---|
| users | user_id, full_name, username, password_hash, role, active | Unique case-insensitive username; role MEMBER or LIBRARIAN |
| books | book_id, isbn, title, author, category | Unique case-insensitive ISBN |
| book_copies | copy_id, book_id, status | Foreign key to books; AVAILABLE, BORROWED or INACTIVE |
| loans | loan_id, member_id, copy_id, issue_date, due_date, return_date, status | Foreign keys; valid date order; ACTIVE or RETURNED |

Dates use ISO `YYYY-MM-DD` text and Java `LocalDate`. The unique partial index `one_active_loan_per_copy` rejects two active loans for the same copy. A trigger rejects new loans for accounts that are not active members. Foreign-key enforcement is enabled on every connection. Related records are preserved; the application offers copy deactivation instead of destructive deletion.

`Database.initialize()` creates missing tables and indexes with `IF NOT EXISTS`. It does not seed users or overwrite data. This first-version schema has no migration framework.

## Circulation transactions

`LoanService.issueCopy()` checks the session, borrower, date and copy state. It updates an available copy conditionally and inserts the loan in one transaction. If either operation fails, rollback restores the earlier state.

`returnCopy()` loads the loan, validates the return, updates the loan and conditionally restores the borrowed copy. A failed update rolls back the whole return. JUnit tests inject failing SQLite triggers to verify both rollback paths.

## Accounts and authorization

`Passwords` uses PBKDF2-HMAC-SHA256 with 210,000 iterations, a random 16-byte salt and a 256-bit result. Each stored value contains iterations, salt and derived hash. Verification compares derived bytes using `MessageDigest.isEqual`.

The first account is created through a setup form only while the user table is empty. `AuthenticationService.Session` has a private constructor, and each authentication service tracks its issued sessions by identity. Service methods validate sessions before accessing records. A manually constructed `Librarian` object does not grant service access. Logout removes the session. Member loan access is restricted in `LoanService`, not only in the UI.

This is a local desktop assignment. Anyone with direct operating-system access to the database can inspect or modify the file; database encryption, remote identity providers and password recovery are outside scope.

## Error handling and resources

Domain validation throws `LibraryException`. SQL failures are wrapped with a user-facing message and retain their cause for debugging. JDBC connections, statements and results use try-with-resources. The controller displays recoverable validation and storage failures in its bottom message area. Database operations are synchronous, which keeps the small assignment understandable but can pause the UI during a slow storage operation.

## Testing

`ModelTest` checks validation, constructors, inheritance, copy transitions and date boundaries. `LibraryIntegrationTest` checks authentication, services, SQLite constraints, persistence and failure rollback. `JavaFxWorkflowTest` exercises real controls on the JavaFX thread using the headless Monocle platform. Its snapshots are screenshots of rendered JavaFX scenes.

Normal tests exclude the `ui` tag. `-Pui-tests` includes all tests. Every database test uses a JUnit temporary directory. Evidence is described in `test-results.md`; native desktop usability checks remain in `manual-test-checklist.md`.
