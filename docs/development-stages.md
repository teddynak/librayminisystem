# Development stages

## Stage 1 Proposal and requirements

Reviewed the original assignment, proposal, requirements, UML diagrams and master prompt. The remote repository contained only a README. Retained Java 21, JavaFX, SQLite, Maven and JUnit, and limited scope to one library. Resolved INACTIVE versus WITHDRAWN in favor of the supplied UML's INACTIVE.

## Stage 2 Classes objects and constructors

Created Maven configuration and User, Librarian, Member, Book, BookCopy and Loan models. Added OopDemo. Initial compilation attempts were blocked by environment dependency access and missing JDK compiler support files. Those failures were not treated as successful checks; a complete local compiler environment was assembled for later verification.

## Stage 3 Encapsulation and validation

Used private fields, immutable identifiers and controlled updates. Added required-field, identity, copy-state and date validation. ModelTest verifies invalid and valid behavior; Book validates all updates before changing existing fields.

## Stage 4 Inheritance and polymorphism

Member and Librarian inherit from abstract User and override getRole. OopDemo uses parent references for runtime dispatch. Book has overloaded constructors, and the catalog calls BookService's overloaded search methods. Model and integration tests verify these behaviors.

## Stage 5 Abstraction interfaces and collections

Implemented abstract User, Searchable<T>, ArrayList row mapping, list-based results, filtering and table collection updates. Kept a single concrete repository instead of multiple repository interfaces. Search and update behavior is covered by integration tests.

## Stage 6 Exceptions and persistence

Added LibraryException, SQLite schema initialization, prepared statements, resource handling, session checks, salted password hashes and transactional circulation. The first completed normal run passed 27 tests, including injected-failure rollback tests and reopening the database.

## Stage 7 Interface and system testing

Implemented JavaFX screens with no placeholder actions. Added a headless JavaFX test profile and a workflow exercising setup, book and member changes, issuing, returning, member history and deactivation. Added entry-point and overdue-screen scenarios. Screenshots capture actual rendered scenes.

## Stage 8 Final verification and presentation preparation

Updated implemented UML, technical documentation, user manual, OOP explanations, test evidence, final report and presentation guide. Verified the Maven JavaFX launch on virtual X11 and captured the native window. Exact executed counts and artifacts are listed in test-results.md. Native desktop checks, the student's recording and oral defense remain separate activities; no personal recording or defense is claimed as completed.

## Java 17 compatibility update

The application now targets Java 17 to match the student workstation. List.getFirst calls were replaced with get(0), the Maven release and GitHub workflow target Java 17, and the headless test platform uses a Java 17 compatible Monocle build. JavaFX 21.0.5 remains in use because it supports Java 17. Fresh Java 17 verification results are recorded in test-results.md. Original assignment and proposal documents remain historical references.
