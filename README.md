# Library Mini System

A small Java 21 and JavaFX desktop assignment for one library. SQLite stores accounts, book titles, individual copies and loan history locally. No external server or paid service is required.

## Run

Install JDK 21 and Maven 3.9 or newer, open a terminal in this project, then run:

```bash
mvn clean javafx:run.
```

On the first launch, create your librarian account. There is no permanent default password. Register members from the Members tab. Both roles log in through the same screen.

Dependencies download on the first build; subsequent launches work offline once all required Maven artifacts have been cached. On Ubuntu a graphical desktop and GTK 3 are required. See [setup guide](docs/setup-guide.md).

## Features

- Librarian and member login with salted PBKDF2 password hashes and service-level role checks.
- Book registration, editing, field-specific searching and physical-copy registration.
- Member registration, searching, editing and borrowing history.
- Transactional issuing and returns, prevention of duplicate issues and returns.
- Overdue loans, days overdue and stored-data dashboard counts.
- Members can search the catalog and view only their own loans.
- Inactive copies preserve historical records.

## Verify

```bash
mvn clean test                     # 27 model and database/service tests
mvn -Pui-tests clean verify         # also runs 3 headless JavaFX scenarios
```

The UI profile uses Monocle for real JavaFX controls without a desktop display. It generates screenshots under `target/ui-evidence/`. Tests use temporary databases, never `data/library.db`.

The packaged JAR from `mvn package` contains the application classes and resources. It is not a standalone installer; use the Maven JavaFX launch command so JavaFX and SQLite dependencies are supplied.

## Study and submission

- User manual: [Markdown](docs/user-manual.md) | [Word](docs/user-manual.docx)
- Technical documentation: [Markdown](docs/technical-documentation.md) | [Word](docs/technical-documentation.docx)
- [OOP explanations and defense examples](docs/oop-explanations.md)
- Implemented UML and design changes: [Markdown](docs/system-design.md) | [Word](docs/system-design.docx)
- [Test cases and actual results](docs/test-results.md)
- [Manual testing checklist](docs/manual-test-checklist.md)
- Final project report: [Markdown](docs/final-project-report.md) | [Word](docs/final-project-report.docx)
- [Presentation and recording guide](docs/presentation-guide.md)
- [Submission checklist](docs/submission-checklist.md)
- [Development stages](docs/development-stages.md)

Original assignment and planning documents are preserved under `docs/reference/`. The implemented design document describes changes from the original UML.

This is a local academic application, not a deployed enterprise service. Your personal video explanation, Google Drive link and oral defense remain your responsibility.
