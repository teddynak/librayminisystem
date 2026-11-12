# Test cases and actual results

## Current Java 17 verification — 12 November 2026

Compiled and tested using OpenJDK **17.0.19**, Maven 3.9.9 and the bundled Maven Wrapper on Linux amd64. JavaFX remains 21.0.5 (compatible with JDK 17); headless UI tests use Monocle 17.0.10.

Command: `./mvnw -B -Pui-tests clean verify` with temporary environment-specific proxy settings. Completed at 12:53 EAT. **BUILD SUCCESS: 30 tests, zero failures, zero errors, zero skipped** (7 model, 20 integration, 3 JavaFX workflow tests). The same cases listed below were rerun. OopDemo also completed successfully.

A separate native JavaFX launch under virtual X11 opened the first-run setup form and created a temporary SQLite database; the smoke-test harness then closed the process. Windows execution has not been tested in this Linux environment. On Windows use `.\mvnw.cmd clean javafx:run`; no separate Maven installation is required. The first wrapper run requires internet access.

Evidence: [build log](evidence/java17/maven-verify.log), [suite summary](evidence/java17/test-summary.json), [wrapper version](evidence/java17/wrapper-version.txt), [OOP output](evidence/java17/oop-demo.txt), [native launch screenshot](evidence/java17/08-native-first-run.png), [native launch log](evidence/java17/native-launch.log). The Java 17 evidence directory also includes XML reports and eight screenshots captured by the UI tests.

## Previous Java 21 verification (historical)

Verification completed on 10 November 2026 at 19:48 EAT. The source was compiled and tested with OpenJDK 21.0.13 and Maven 3.9.9 on Linux amd64. JavaFX 21.0.5 used Monocle headless software rendering for the UI scenarios.

Command executed: `mvn -B -Pui-tests clean verify` (the environment used a temporary Maven proxy configuration to obtain dependencies).

**Actual outcome: BUILD SUCCESS. 30 tests, zero failures, zero errors, zero skipped.** The application JAR was produced. The separate OopDemo also ran successfully. This is an executed result, not a proposed test plan.

| Suite | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| JavaFxWorkflowTest | 3 | 0 | 0 | 0 |
| LibraryIntegrationTest | 20 | 0 | 0 | 0 |
| ModelTest | 7 | 0 | 0 | 0 |

## Cases

All cases create their own objects or temporary databases. Preconditions and exact inputs are visible in the named test method under `src/test/java/com/library/`; examples use fictional accounts, Password123/Member123 and temporary catalog records. Database failures are deliberately injected inside isolated tests. Test data is never inserted into the normal application database.

| ID | Test method | Expected behavior | Actual result |
|---|---|---|---|
| T01 | `JavaFxWorkflowTest.overdueScreenShowsStoredDatesAndDays` | Overdue table displays a stored loan three days past due. | PASS; assertions completed without failure. |
| T02 | `JavaFxWorkflowTest.applicationEntryPointShowsFirstRunSetup` | Main.start opens a stage with the first-run account form. | PASS; assertions completed without failure. |
| T03 | `JavaFxWorkflowTest.fullLibrarianAndMemberWorkflow` | Actual controls complete setup, catalog/member edits, issue/return, member history and deactivation. | PASS; assertions completed without failure. |
| T04 | `LibraryIntegrationTest.invalidBorrowerAndDueDateDoNotChangeCopy` | Missing/non-member borrowers and past due dates fail without changing stock. | PASS; assertions completed without failure. |
| T05 | `LibraryIntegrationTest.inactiveCopyCannotBeIssuedAndHistoryIsKept` | Borrowed copies cannot be deactivated; inactive copies cannot be issued; history remains. | PASS; assertions completed without failure. |
| T06 | `LibraryIntegrationTest.failureDuringIssueRollsBackCopyUpdate` | Injected insert failure leaves no loan and keeps the copy available. | PASS; assertions completed without failure. |
| T07 | `LibraryIntegrationTest.memberCannotCallLibrarianServices` | Direct member calls to protected service operations are rejected. | PASS; assertions completed without failure. |
| T08 | `LibraryIntegrationTest.logoutRevokesSessionAndOtherAuthCannotReuseIt` | Logged-out, null and foreign-service sessions cannot access services. | PASS; assertions completed without failure. |
| T09 | `LibraryIntegrationTest.duplicateUsernameIsRejectedIgnoringCase` | A duplicate username with different case is rejected. | PASS; assertions completed without failure. |
| T10 | `LibraryIntegrationTest.overdueDatesAndDashboardCountsComeFromStorage` | A two-day overdue fixture appears with correct overdue and availability counts. | PASS; assertions completed without failure. |
| T11 | `LibraryIntegrationTest.searchOverloadsAndUpdates` | All-field and field-specific searches work; edits affect results. | PASS; assertions completed without failure. |
| T12 | `LibraryIntegrationTest.duplicateIsbnIsRejected` | Duplicate ISBN registration fails. | PASS; assertions completed without failure. |
| T13 | `LibraryIntegrationTest.invalidBookAndMemberInputsAreRejected` | Missing fields and nonexistent update targets are rejected. | PASS; assertions completed without failure. |
| T14 | `LibraryIntegrationTest.saltedPasswordHashesAndVerification` | Same password has different salted hashes; only the correct password verifies. | PASS; assertions completed without failure. |
| T15 | `LibraryIntegrationTest.successfulIssueAndReturnChangesBothRecords` | Issue borrows the copy; return records the date and restores availability. | PASS; assertions completed without failure. |
| T16 | `LibraryIntegrationTest.duplicateReturnIsRejected` | The same loan cannot be returned twice. | PASS; assertions completed without failure. |
| T17 | `LibraryIntegrationTest.secondIssueIsRejected` | The same copy cannot have a second active loan. | PASS; assertions completed without failure. |
| T18 | `LibraryIntegrationTest.memberSearchAndUpdate` | Updated name is searchable and updated username can log in. | PASS; assertions completed without failure. |
| T19 | `LibraryIntegrationTest.membersSeeOnlyTheirOwnHistory` | Members see their own loans and cannot request another member history. | PASS; assertions completed without failure. |
| T20 | `LibraryIntegrationTest.databasePreventsDuplicateActiveLoansAndForeignKeyViolations` | Database rejects a second active loan and a nonexistent book reference. | PASS; assertions completed without failure. |
| T21 | `LibraryIntegrationTest.credentialsAndFirstRunSetup` | Valid case-insensitive username works; bad credentials and repeated setup fail. | PASS; assertions completed without failure. |
| T22 | `LibraryIntegrationTest.failureDuringReturnRollsBackLoanUpdate` | Injected copy-update failure leaves the loan active and copy borrowed. | PASS; assertions completed without failure. |
| T23 | `LibraryIntegrationTest.recordsSurviveReopeningDatabase` | New repository instance reloads accounts, books, copy state and loans. | PASS; assertions completed without failure. |
| T24 | `ModelTest.requiredFieldsAreRejected` | A blank title is rejected. | PASS; assertions completed without failure. |
| T25 | `ModelTest.invalidIdentityIsRejected` | A zero record ID is rejected. | PASS; assertions completed without failure. |
| T26 | `ModelTest.polymorphismUsesConcreteRole` | Parent references return the concrete account roles; invalid names are rejected. | PASS; assertions completed without failure. |
| T27 | `ModelTest.overdueBoundaryAndReturnedLoan` | Due today is not overdue; one day late is overdue; a return closes the loan. | PASS; assertions completed without failure. |
| T28 | `ModelTest.bookConstructorsAndValidatedUpdates` | Default category and valid edits work; an invalid edit preserves earlier details. | PASS; assertions completed without failure. |
| T29 | `ModelTest.invalidLoanDatesAreRejected` | Invalid due, past return and future return dates are rejected. | PASS; assertions completed without failure. |
| T30 | `ModelTest.copyStateTransitionsAreControlled` | Borrowed/inactive copies reject invalid transitions. | PASS; assertions completed without failure. |

## Evidence files

- [Full final Maven output](evidence/maven-verify.txt)
- [Machine-readable results](evidence/test-summary.json)
- [Model suite report](evidence/com.library.ModelTest.txt)
- [Database and service suite report](evidence/com.library.LibraryIntegrationTest.txt)
- [JavaFX suite report](evidence/com.library.JavaFxWorkflowTest.txt)
- [Object demonstration output](evidence/oop-demo.txt)

The first eight PNGs in `evidence/` were captured from the rendered JavaFX scenes during the successful test execution: setup, summary, catalog, members, issued loan, returned loan, member history and overdue loans. The overdue scene uses a deliberate historical test fixture, not an altered production record.

## Reproduce

Run `mvn clean test` for 27 model/integration tests, or `mvn -Pui-tests clean verify` for all 30 tests and fresh screenshots under `target/ui-evidence/`. The repository's preserved `docs/evidence` files are a snapshot of this execution; rerunning tests does not silently overwrite that historical evidence.

A JavaFX unnamed-module warning appeared during headless testing. It did not fail the tests or packaging. The normal Maven JavaFX launch command is documented separately.

## Scope and remaining checks

Main.start was executed and displayed a real JavaFX stage on the headless platform. The Maven JavaFX launch command was also executed successfully on virtual X11 using the native GTK platform; see [launch check](evidence/launch-check.txt), [launch log](evidence/maven-launch-x11.txt), [window tree](evidence/x11-window-tree.txt) and [launch screenshot](evidence/08-maven-launch-x11.png). The smoke-test harness intentionally closed the running application after the capture. Keyboard behavior, display scaling and accessibility were not manually verified on the student's Ubuntu machine. These must be recorded truthfully in [the manual checklist](manual-test-checklist.md). No performance benchmark, independent user acceptance test, completed oral defense or personal explanation video is claimed.
