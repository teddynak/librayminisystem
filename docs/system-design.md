# Implemented system design

## Domain classes

```mermaid
classDiagram
    class User {
      <<abstract>>
      -long userId
      -String fullName
      -String username
      -String passwordHash
      -boolean active
      +getRole() String
      +updateName(String) void
    }
    User <|-- Librarian
    User <|-- Member
    class Book {
      -long bookId
      -String isbn
      -String title
      -String author
      -String category
      +updateDetails(String,String,String,String) void
    }
    class BookCopy {
      -long copyId
      -long bookId
      -CopyStatus status
      +markBorrowed() void
      +markAvailable() void
      +deactivate() void
    }
    class Loan {
      -long loanId
      -long memberId
      -long copyId
      -LocalDate issueDate
      -LocalDate dueDate
      -LocalDate returnDate
      +isActive() boolean
      +isOverdue(LocalDate) boolean
      +recordReturn(LocalDate) void
    }
    Book "1" -- "0..*" BookCopy
    Member "1" -- "0..*" Loan
    BookCopy "1" -- "0..*" Loan
```

Associations are implemented through IDs and database foreign keys rather than duplicated in-memory lists on each model. Every loan belongs to one member and one copy. Historical loans can reference a copy repeatedly, but only one may be active. `LoanStatus` is derived from `returnDate`; there is no independently mutable status field in the Java model. The database stores status with a check constraint to keep it consistent.

## Application components

```mermaid
classDiagram
    Searchable~Book~ <|.. BookService
    class Searchable~Book~ {
      <<interface>>
      +search(String) List
    }
    LibraryController --> AuthenticationService
    LibraryController --> BookService
    LibraryController --> MemberService
    LibraryController --> LoanService
    AuthenticationService --> LibraryRepository
    BookService --> LibraryRepository
    MemberService --> LibraryRepository
    LoanService --> LibraryRepository
    LibraryRepository --> Database
```

`BookService.search(String)` delegates to overloaded `search(String, Field)`. `AuthenticationService` creates session objects; book, member and loan services require a valid session. One shared repository performs the small project's JDBC mapping.

## Changes from the supplied design

- The original service diagram proposed `BookRepository`, `BookCopyRepository` and `LoanRepository` interfaces. The implementation uses one concrete `LibraryRepository` to reduce repetitive files. `Searchable<T>` remains a purposeful interface.
- `Loan` explicitly includes member and copy IDs. It derives Java loan status from the return date to avoid conflicting model state.
- `BookCopy` holds `bookId`, making the persisted association explicit.
- `CopyStatus.INACTIVE` is used consistently, matching the original design document rather than the prompt's alternative example `WITHDRAWN`.
- Authentication, member management, UI and database classes are included in the component diagram; the supplied diagrams focused on domain, book and loan classes.

The original planning documents remain in `reference/` for comparison. These changes preserve their functional requirements.
