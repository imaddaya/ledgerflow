# Start from a clean Windows 11 laptop

Follow this guide in order. The easiest first run uses Java, Node.js and the included H2 database. Docker and PostgreSQL come afterwards.

## 1. Download and extract the project

1. Download `LedgerFlow_Java_Fintech_Project.zip` from the accompanying message.
2. Right-click it, choose **Extract All**, and put the `ledgerflow` folder in `C:\Projects`.
3. Confirm that `C:\Projects\ledgerflow` contains `README.md`, `backend`, `frontend`, `compose.yml` and `docs`. If there is a second nested `ledgerflow` folder, move the inner project folder to this location.
4. Use a local folder rather than a ZIP preview. The commands below assume the path is exactly `C:\Projects\ledgerflow`; substitute your actual path if different.

You need internet access for the initial tool and dependency downloads. Allow several gigabytes of free space. Docker requires additional memory, disk space and virtualization support; it is optional for the first run.

## 2. Install the JDK, Node.js, Git and an editor

| Download | Choose | Installer settings / purpose |
|---|---|---|
| [Eclipse Temurin](https://adoptium.net/temurin/releases/?version=21&os=windows) | Java **21 LTS**, Windows **JDK**, MSI; x64 for ordinary Intel/AMD laptops, ARM64 for ARM laptops | Include **Add to PATH** and **Set JAVA_HOME**. A JRE alone cannot compile Java. |
| [Node.js](https://nodejs.org/en/download) | **24 LTS**, Windows installer | Keep npm and PATH enabled. Use 24.15 or later in the 24.x line for Angular 22. |
| [Git for Windows](https://git-scm.com/install/windows) | Current Windows installer | Keep the recommended option allowing Git from the command line and third-party software. Git Credential Manager supports browser authentication. |
| [Visual Studio Code](https://code.visualstudio.com/download) | Windows installer | Enable **Add to PATH** if offered. This is the code editor. |

Use the official installers. Maven is supplied by the project wrapper, and Angular CLI is installed as a project dependency. PostgreSQL, Oracle and global Angular are not required for the first run.

Close and reopen PowerShell after installing. In VS Code, open **File → Open Folder → C:\Projects\ledgerflow**, then **Terminal → New Terminal**.

Optional editor extensions: **Extension Pack for Java** (Microsoft), **Spring Boot Extension Pack** (VMware) and **Angular Language Service** (Angular). The command-line build works without these extensions.

## 3. Verify the installations

Run each line in PowerShell:

```powershell
java -version
javac -version
node --version
npm.cmd --version
git --version
$env:JAVA_HOME
```

Expected: Java and javac show **21.x**, Node shows **v24.x** at least 24.15, and npm/Git show version numbers. JAVA_HOME should point to the JDK installation folder, not its `bin` subfolder.

If Java or javac is missing, rerun the JDK MSI, select its PATH/JAVA_HOME features and open a new terminal. If another Java version takes priority, check `Get-Command java` and your Windows environment variables. Do not delete system PATH entries.

Commands use `npm.cmd` to avoid the PowerShell script execution-policy error that can occur with `npm.ps1`. You do not need to weaken your system execution policy.

## 4. Start the Java backend

In the first terminal:

```powershell
cd C:\Projects\ledgerflow\backend
.\mvnw.cmd -version
.\mvnw.cmd spring-boot:run
```

The first command downloads Maven. The second downloads dependencies, applies the Flyway migration, validates the Hibernate mapping, creates synthetic demo records and starts the API on port **8080**. Wait for `Started LedgerFlowApplication`.

Leave this terminal running. Open **http://localhost:8080/actuator/health**: it should return JSON containing `"status":"UP"`. Opening `/api/accounts` before signing in should return 401; that is the intended security boundary.

The database is created automatically in `C:\Projects\ledgerflow\backend\data`. Stopping Java does not erase it. Demo data seeds only when the users table is empty. Editing the default demo password after the first run does not reset existing password hashes.

## 5. Start the Angular frontend

Open a **second terminal** in VS Code using the plus button:

```powershell
cd C:\Projects\ledgerflow\frontend
npm.cmd ci
npm.cmd start
```

`npm ci` installs the exact versions in the lockfile. `npm start` runs the local Angular development server. Wait until it reports the application is ready.

Open **http://localhost:4200**. Use this hostname consistently rather than mixing it with `127.0.0.1`; the frontend proxy forwards API requests and the session/CSRF cookies must agree on a hostname.

Sign in with **operator@ledgerflow.demo** and **DemoPass123!**. The interface lets you choose one of three demo roles. See [DEMO_WALKTHROUGH.md](DEMO_WALKTHROUGH.md) for what to click next.

## 6. Stop and restart

- Press **Ctrl+C** in each running terminal to stop the processes.
- On the next visit, run the backend and frontend start commands again. You only need `npm ci` again when dependencies or the lockfile change or `node_modules` is removed.
- Do not close the terminals while expecting the application to keep running.

## 7. Run checks and make a release build

In the backend directory:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

`package` reruns the tests and builds `target\ledgerflow-1.0.0.jar`. You can start that file instead of the Maven development command:

```powershell
java -jar target\ledgerflow-1.0.0.jar
```

Run it from the `backend` folder to keep the H2 database in the same place. Do not run both backend commands at once.

In the frontend directory:

```powershell
npm.cmd test
npm.cmd run build
```

The browser assets appear under `frontend\dist\ledgerflow`. Opening `index.html` directly through `file://` is not a deployment: it needs a web server with the API proxy. Docker Compose provides that route.

## 8. Add Docker Desktop and PostgreSQL

After the easy route works, install [Docker Desktop for Windows](https://docs.docker.com/desktop/setup/install/windows-install/). Follow its current Windows and WSL 2 requirements. It may require administrator access, enabling hardware virtualization and restarting Windows. On a managed laptop, follow its administrator's setup process.

Start Docker Desktop and wait until the engine is running. Verify:

```powershell
docker --version
docker compose version
```

Stop the local backend first, then run from the project root:

```powershell
cd C:\Projects\ledgerflow
Copy-Item .env.example .env
docker compose up --build
```

Open **http://localhost:8088**. The three services are PostgreSQL on 5432, Java on 8080 and Nginx/Angular on 8088. They bind to your laptop's loopback interface. This route has a separate database from H2, so its initial demo balances start fresh.

Useful commands, from the root:

```powershell
docker compose ps
docker compose logs backend
docker compose down
```

`down` stops containers and keeps the database volume. **Only if you want to erase all Docker demo data**, use `docker compose down --volumes`. Back up any demo records you want to keep first.

You can also run just PostgreSQL in Docker and keep Java/Angular in development mode:

```powershell
cd C:\Projects\ledgerflow
docker compose up -d database
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Default local PostgreSQL credentials are `ledgerflow` / `ledgerflow_local_only`, database `ledgerflow`. If you changed `.env`, pass the same password in the Java terminal with `$env:DB_PASSWORD='your-local-value'` before starting Java. Never commit that `.env` file.

With Docker running, execute the PostgreSQL integration test:

```powershell
cd C:\Projects\ledgerflow\backend
.\mvnw.cmd -Ppostgres-tests verify
```

## 9. Common fixes

| Symptom | What to do |
|---|---|
| Command not recognized | Reopen the terminal, check the relevant installer PATH option, then rerun the version check. |
| `invalid target release: 21` | Maven is using an older JDK. Check `.\mvnw.cmd -version` and JAVA_HOME. |
| Port 8080 already in use | Stop the previous Java process or Docker backend; use one backend route at a time. |
| UI cannot reach API | Check Java's terminal, health URL, port 8080 and `frontend/proxy.conf.json`. |
| 403 after login / session expiry | Refresh, sign in again, use `localhost` everywhere and allow first-party cookies. |
| Failed dependency download | Check internet/proxy configuration and retry. Do not disable TLS certificate verification. |
| PostgreSQL password failure | Match Java's DB_PASSWORD to the password used when the existing database volume was initialized. Changing `.env` alone does not change that existing database user's password. |
| CSV rejected | Use headers exactly `reference,amount,currency`, positive amounts with at most two decimal places, a three-letter currency, UTF-8 encoding and no more than 500 rows. UTF-8 BOM from Excel is accepted. |
| Wrong demo password after a change | Existing users retain their original hashed password. Use the original password or deliberately reset your demo database. |

To reset H2 only: stop Java and rename `backend\data` to a backup folder. Restart Java and it will create a new demo database. Never do this while the process is running.

## 10. Continue to GitHub

Once the app and tests work on your laptop, follow [GITHUB_UPLOAD.md](GITHUB_UPLOAD.md). Then work through [LEARNING_PATH.md](LEARNING_PATH.md), make your own changes and add screenshots from your actual running application to the repository.
