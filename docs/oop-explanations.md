# OOP explanations for the project defense

Each example below refers to implemented source code. Matching `// OOP:` comments are placed beside important declarations and operations.

## Classes and Objects

A class defines a kind of information; an object is one instance.

Implementation: `Book, BookCopy, Loan and OopDemo.main`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Create a title and copy in the interface, then show where the repository constructs Book objects.

## Encapsulation

Private fields and validated operations protect object state.

Implementation: `Book.updateDetails, User.updateName and BookCopy.markBorrowed`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Try a blank title and show why a borrowed copy cannot be deactivated.

## Constructors and Methods

Constructors initialize valid objects; methods perform operations.

Implementation: `Book constructors and Loan constructor`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Show the two Book constructors and explain the default General category.

## Inheritance

A subclass reuses common state and behavior from its parent.

Implementation: `Member extends User and Librarian extends User`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Show inherited account fields and updateName without duplicating them in either subclass.

## Polymorphism

The same method call can use different implementations; overloads accept different parameters.

Implementation: `Member.getRole, Librarian.getRole, BookService.search and OopDemo.main`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Show User references returning different roles, then select a search field to exercise the overloaded search method.

## Abstraction

An abstract parent describes shared behavior while leaving a required operation to subclasses.

Implementation: `abstract User and abstract getRole`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Explain why User cannot be directly instantiated and why both roles must implement getRole.

## Interfaces

An interface defines a contract implemented by a class.

Implementation: `Searchable<T> and BookService.search(String)`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Show implements Searchable<Book>, its @Override method and its use by the catalog.

## Collections and Data Structures

Collections hold groups of typed objects for searching and display.

Implementation: `LibraryRepository.query ArrayList, BookService.search streams, JavaFX table items`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Show adding mapped rows to the list, filtering search results and replacing or clearing table items after edits and copy deactivation.

## Exception Handling

Failures are reported and handled without accepting invalid operations.

Implementation: `LibraryException, Database.transaction and LibraryController.action`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Attempt a duplicate issue or blank title; show the message and the unchanged records.

## Data Persistence

Saved records remain available after the program exits.

Implementation: `Database.initialize, LibraryRepository.query and LoanService transactions`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Create a record, restart the application and find it again; explain rollback on a failed save.

## User Interface

Controls collect input and display results while services perform the work.

Implementation: `Main.start and LibraryController.catalog / loanPage`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Follow Issue copy from the button event into LoanService.issueCopy.

## Testing

Tests compare actual behavior with expected outcomes.

Implementation: `ModelTest, LibraryIntegrationTest and JavaFxWorkflowTest`. This supports the library operations while keeping each class responsible for its own data or work.

Defense demonstration: Run Maven tests and explain one rollback test and one UI scenario using their actual assertions.

## Run the separate object demonstration

After `mvn compile`, run `java -cp target/classes com.library.OopDemo`. It creates objects, changes a copy state and demonstrates runtime role dispatch without changing the library database.
