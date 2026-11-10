# Manual desktop testing checklist

These native desktop checks are not claimed as completed by the headless automated run. Record your name, date, operating system, Java version, actual observations and Pass or Fail when you perform them on your computer.

| ID | Check | Expected result | Desktop result |
|---|---|---|---|
| M01 | Launch with `mvn clean javafx:run` on Ubuntu | Native window opens without errors | Not yet recorded |
| M02 | Create the first librarian and restart | Normal login appears; same account works | Not yet recorded |
| M03 | Use keyboard Tab, Shift Tab and Enter | Forms remain usable; Enter submits login | Not yet recorded |
| M04 | Resize to 900 by 650 and a larger window | Forms remain reachable using scrollbars; table columns can be resized | Not yet recorded |
| M05 | Enter blank book details and a duplicate ISBN | Understandable error; no extra record | Not yet recorded |
| M06 | Add two copies, issue one and return it | Counts and states change correctly | Not yet recorded |
| M07 | Select returned and unselected loans | Return button is disabled | Not yet recorded |
| M08 | Member login and catalog search | Only catalog and personal loans are available | Not yet recorded |
| M09 | Close and reopen after several edits | All saved changes remain | Not yet recorded |
| M10 | Hover over shortened table text | Full value appears in a tooltip | Not yet recorded |
| M11 | Check a past-due active loan using a separate test database | Correct dates and overdue days appear | Not yet recorded |
| M12 | Use your normal display scale and keyboard layout | Labels are readable and inputs work | Not yet recorded |

Do not edit your real database to manufacture overdue records. The automated overdue fixture and its screenshot already provide reproducible date evidence. Use only a separate test database for manual experiments.
