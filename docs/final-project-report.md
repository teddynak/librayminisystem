# Design and implementation of a library management system using object oriented programming principles in Java

Course: Object-Oriented Programming with Java  
Prepared for: Nakyejwe Teddy  
Registration number: 24/BSE/BU/R/0017

## Introduction

This project implements a small desktop system for a single library. It brings together book titles, physical copies, registered members and borrowing records. The application was developed to demonstrate the Java OOP concepts required in the course while providing a complete set of manageable library operations.

A library using separate registers can struggle to identify which copy is available, who borrowed it and when it should be returned. The implemented system links these records and checks borrowing and return rules before saving changes. Its local database keeps information available between sessions.

## Objectives and scope

The main objective was to build and test a Java library application demonstrating classes and objects, constructors, encapsulation, inheritance, polymorphism, abstraction, interfaces, collections, exception handling and persistence.

The application supports librarian and member accounts, catalog searching, book and copy registration, member registration, editing, issuing, returns, overdue identification and borrowing history. It is intended for one local library. Online payments, fines, SMS, electronic books, multiple branches, mobile applications and cloud infrastructure were excluded.

## Requirements and users

Librarians manage titles, copies, members and circulation. Members search the catalog and view only their own loans. Each physical copy has its own ID. A title may have multiple copies, and a copy may have multiple historical loans but only one active loan.

Issue and due dates are required. A due date cannot precede the issue date. Returning a copy records today's return date and restores availability. Invalid operations must leave the earlier records unchanged. Records used by borrowing history are retained; copies leaving circulation become inactive.

Quality requirements include understandable forms, useful validation messages, protected password storage, role restrictions, consistent circulation data and persistent local records. The original requirements specification is preserved in `reference/requirements-specification.docx`.

## Design

The main domain classes are abstract User, Librarian, Member, Book, BookCopy and Loan. User stores common account information. Librarian and Member override getRole. Book describes a title, while BookCopy describes one physical copy. Loan connects a member and copy using identifiers and records issue, due and return dates.

The JavaFX controller calls four service classes: AuthenticationService, BookService, MemberService and LoanService. These enforce rules and permissions. LibraryRepository maps JDBC results to objects, and Database opens connections and handles schema creation and transactions. A single repository was chosen to keep the assignment smaller than the original design's proposed repository interfaces. Searchable<T> remains an implemented interface.

The implemented class diagrams and the changes from the original UML are in `system-design.md`. The database contains users, books, book_copies and loans, connected with foreign keys. A partial unique index prevents multiple active loans for one copy.

## Implementation

The project uses Java 21, JavaFX, SQLite through JDBC, Maven and JUnit 5. JavaFX screens are built in Java and styled using CSS. The application creates its local database and displays a first-run librarian setup form. It never creates a permanent hardcoded administrator password or random library records during normal startup.

Authentication uses salted PBKDF2 password hashes. A private session type is created only after a successful login. Services validate the session and role before performing work. Members cannot obtain another member's loan history through the service methods.

BookService supports adding and updating titles, searching through overloaded methods and managing copies. MemberService supports registration, searching and updating names and usernames. LoanService implements transactional issuing and returns. When saving a related record fails, Database rolls back the transaction.

The screens show database-backed information rather than fixed sample values. Buttons requiring a selected record are disabled until a record is selected. Tables display copy availability, dates and loan status. Shortened table text has tooltips.

## Application of OOP

| Concept | Implemented example |
|---|---|
| Classes and objects | Book, BookCopy, Loan and repository-created instances |
| Encapsulation | Private model fields, validated updateDetails and controlled copy transitions |
| Constructors | Required initialization and overloaded Book constructors |
| Inheritance | Member and Librarian extend User |
| Overriding | Role-specific getRole implementations |
| Overloading | BookService.search(String) and search(String, Field) |
| Runtime polymorphism | User references to Member and Librarian; account mapping and OopDemo |
| Abstraction | Abstract User and getRole |
| Interfaces | Searchable<T> implemented by BookService |
| Collections | ArrayList row mapping, list filtering and JavaFX table items |
| Exceptions | LibraryException, validation and transactional rollback handling |
| Persistence | Prepared JDBC statements and SQLite transactions |

Short `// OOP:` comments identify these concepts in the source. `oop-explanations.md` provides code locations and defense demonstrations. OopDemo is a separate executable illustration and does not modify the normal database.

## Testing and results

The completed verification command was `mvn -B -Pui-tests clean verify`. It executed 30 tests: seven model tests, twenty database/service tests and three JavaFX scenarios. There were zero failures, zero errors and zero skipped tests. Maven also successfully built the application JAR. Detailed per-test cases and preserved execution output are in `test-results.md` and `evidence/`.

Testing covered constructors, invalid fields, account roles, salted password verification, duplicate usernames and ISBNs, searching and updates, successful issues and returns, rejected duplicate circulation operations, member privacy, revoked sessions, overdue dates, database constraints and reopening saved records.

Two tests injected database failures using temporary triggers. A failed loan insertion did not leave the copy borrowed, and a failed copy update during a return did not leave the loan closed. These tests checked the saved state after rollback.

The JavaFX scenarios used real JavaFX controls on a headless platform. They exercised first-run setup and Main.start, librarian and member workflows, and an overdue fixture with a loan three days late. Screenshots are genuine captures of those rendered test scenes. Test accounts and records were fictional and stored in temporary databases.

The documented Maven launch was also verified on a virtual X11 display with a native GTK JavaFX window and a separately configured database path. Its screenshot and window-tree evidence are preserved. This does not establish the student's native Ubuntu display compatibility, keyboard usability or performance under a large workload. The manual desktop checklist remains available for the student to complete on their computer. No manual result or personal recording has been invented.

## Results against the objectives

The implementation provides the planned core functions for a single library and represents the required OOP concepts in working code. Borrowing rules are enforced in services and supported by database constraints. Persistence is verified by opening the saved database through a new repository instance. The member interface and service restrictions limit personal borrowing records to the correct account.

Supporting outputs include setup instructions, user and technical documentation, implemented UML, OOP explanations, genuine test evidence, a manual checklist and a presentation guide. The source project can be built and launched using Maven.

## Limitations

The application is intended for small local use. Database operations run synchronously, so a slow operation can pause the UI. The database file is not encrypted. There is no password recovery, configurable borrowing policy, report export, fine processing or multi-branch management. ISBNs are required and unique but are not checksum-validated. The Maven-built JAR is not a standalone installer.

Native desktop verification, the student's own explanation video, Google Drive sharing and oral defense are still required before the overall academic submission is complete.

## Conclusion

The project demonstrates how Java OOP can organize a working library system into understandable classes, services and persistent records. The executed tests support the core circulation and access rules. The remaining submission activities are to verify the desktop experience, understand and explain the code, record the demonstration and submit the required materials in the lecturer's preferred format.

## Supporting material

The original assignment and planning documents are in `reference/`. The implemented design is in `system-design.md`, the user instructions are in `user-manual.md`, and technical details are in `technical-documentation.md`. Test source is in `src/test/java/com/library` relative to the repository root. `presentation-guide.md` and `submission-checklist.md` cover the remaining academic activities.
