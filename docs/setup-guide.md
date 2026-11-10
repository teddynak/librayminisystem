# Setup guide

## Requirements

Use JDK 21, Maven 3.9 or newer and a graphical desktop. Check `java -version`, `javac -version` and `mvn -version`. Maven must report Java 21. On Ubuntu, install a JDK rather than only a JRE. Install your distribution's GTK 3 runtime if JavaFX reports a missing GTK library.

## Obtain and launch

```bash
git clone https://github.com/teddynak/librayminisystem.git
cd librayminisystem
mvn clean javafx:run
```

Authenticate to GitHub with your own account when cloning this private repository. In VS Code install Java support and open the project folder. In IntelliJ open `pom.xml` as a Maven project and select JDK 21. The terminal commands work in either editor.

Internet access is needed for the initial Maven dependency download. The application itself makes no network requests. Wait for the JavaFX window, then enter a full name, username and password of 8 to 128 characters to create the initial librarian. Later launches show the normal login form.

## Data location

The default database is `data/library.db`, relative to the directory from which the application is launched. It is created automatically without replacing existing records. Always run from the same project directory to use the same database.

For another location:

```bash
mvn javafx:run -Dlibrary.db=/absolute/path/library.db
```

Close the application before copying the database as a backup. Restore by copying that backup into the same location while the application is closed. Do not commit real user records or passwords to GitHub. Losing a local password is not handled by an email reset feature; keep the demonstration account credentials securely.

## Build and test

```bash
mvn clean test
mvn -Pui-tests clean verify
mvn clean javafx:run
```

The first command runs the 27 normal tests. The UI profile adds three JavaFX test scenarios and produces screenshots. A build success does not replace checking the native window on your own computer.

## Troubleshooting

- Java release errors: set the editor JDK and `JAVA_HOME` to a complete JDK 21.
- Dependency resolution errors: check internet access and Maven proxy configuration, then retry.
- Missing display: launch from your desktop session; use the UI test profile for headless tests.
- Storage errors: check write permissions and avoid running multiple instances against the same database.
- Duplicate record error: choose a unique username or ISBN.
