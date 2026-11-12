# Setup guide

## Requirements

Use JDK 17 and a graphical desktop. The included Maven Wrapper supplies Maven 3.9.9; a separate Maven installation is optional. Check `java -version`, `javac -version` and `mvn -version`. Maven must report Java 17. On Ubuntu, install a JDK rather than only a JRE. Install your distribution's GTK 3 runtime if JavaFX reports a missing GTK library.

## Obtain and launch

Open the project folder containing `pom.xml`. On Windows PowerShell, use the bundled Maven Wrapper:

```powershell
java -version
javac -version
.\mvnw.cmd -version
.\mvnw.cmd clean javafx:run
```

Both Java commands should report 17, and the wrapper version output must report Java 17. If `JAVA_HOME` points to a different JDK, set it to the installed JDK 17 folder, then restart VS Code. The wrapper solves the earlier “mvn is not recognized” error without a separate Maven installation. It needs internet access on first use to download Maven and dependencies.

For Linux or macOS:

```bash
git clone https://github.com/teddynak/librayminisystem.git
cd librayminisystem
./mvnw clean javafx:run
```

An existing Maven installation can still use `mvn clean javafx:run`. Authenticate with your GitHub account when cloning this private repository. In VS Code open the project folder and select JDK 17 for the project. In IntelliJ open `pom.xml` as a Maven project and select JDK 17.

The application itself makes no network requests. The first launch asks for a full name, username and a password of 8 to 128 characters to create the initial librarian. Later launches show the normal login form. Keep your existing database; this compatibility update does not change its schema.

JavaFX 21.0.5 supports Java 17 and remains unchanged. The headless test-only Monocle dependency uses version 17.0.10. The original development master prompt records the earlier Java 21 plan; the current Maven configuration and this guide describe the supported Java 17 build.

## Data location

The default database is `data/library.db`, relative to the directory from which the application is launched. It is created automatically without replacing existing records. Always run from the same project directory to use the same database.

For another location:

```bash
mvn javafx:run -Dlibrary.db=/absolute/path/library.db
```

Close the application before copying the database as a backup. Restore by copying that backup into the same location while the application is closed. Do not commit real user records or passwords to GitHub. Losing a local password is not handled by an email reset feature; keep the demonstration account credentials securely.

## Build and test

```bash
./mvnw clean test
./mvnw -Pui-tests clean verify
./mvnw clean javafx:run
```

On Windows replace `./mvnw` with `.\mvnw.cmd`. The first command runs the 27 normal tests. The UI profile adds three JavaFX test scenarios and produces screenshots. A build success does not replace checking the native window on your own computer.

## Troubleshooting

- Java release errors: set the editor JDK and `JAVA_HOME` to a complete JDK 17.
- Maven command not recognized: use `.\mvnw.cmd` on Windows or `./mvnw` on Linux instead.
- Dependency resolution errors: check internet access and Maven proxy configuration, then retry.
- Missing display: launch from your desktop session; use the UI test profile for headless tests.
- Storage errors: check write permissions and avoid running multiple instances against the same database.
- Duplicate record error: choose a unique username or ISBN.
