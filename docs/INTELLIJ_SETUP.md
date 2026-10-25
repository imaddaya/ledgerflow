# LedgerFlow — IntelliJ IDEA setup (Windows)

**Use IntelliJ IDEA for everything. No VS Code installation is needed.**

The project is already a Maven/Spring Boot Java application with an Angular frontend. You do **not** need to convert Java files or generate a new project. The `.idea` folder is deliberately not included: IntelliJ creates local settings for your own computer, and `.gitignore` keeps those settings out of GitHub.

## 1. Install the prerequisites

- [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (free version is sufficient for the steps below).
- [JDK 21](https://adoptium.net/temurin/releases/?version=21) (install the **JDK**, not just the JRE).
- [Node.js 24 LTS](https://nodejs.org/en/download) (for the Angular frontend; use 24.15 or later).
- [Git](https://git-scm.com/downloads) (for committing and uploading to GitHub).

Maven and the Angular CLI do not require separate global installations: the repository contains a Maven wrapper and the npm lockfile/dependencies.

Restart IntelliJ after installing those tools. In IntelliJ, open **View > Tool Windows > Terminal** and check:

```powershell
java -version
javac -version
node --version
npm.cmd --version
git --version
```

If these commands fail, fix the Windows PATH/installation before moving on.

## 2. Open the complete project in IntelliJ

1. Extract the ZIP so that you have `C:\Projects\ledgerflow\` (or another location you choose).
2. Start IntelliJ IDEA > **Open** (or **File > Open**).
3. Choose the **`ledgerflow` folder**, not `frontend` or `backend` separately. Choose **Trust Project** if prompted, since these are your local files.
4. Open `backend/pom.xml`. If IntelliJ offers **Load Maven Project**, accept. Otherwise, right-click the `pom.xml` and select **Add as Maven Project** (or use the Maven tool window's add-project button).
5. Allow the Maven dependencies to download and sync. Find the Maven panel under **View > Tool Windows > Maven**.
6. In **File > Project Structure > Project**, set **SDK = Java 21**. If it is missing, select **Add SDK > JDK** and point to your JDK 21 installation. Also check **Settings > Build, Execution, Deployment > Build Tools > Maven > Runner** if Maven tries to use an older JDK.

Your full IntelliJ Project pane should contain:

```text
ledgerflow/
  backend/
    pom.xml
    src/main/java/com/ledgerflow/LedgerFlowApplication.java
  frontend/
    package.json
    src/
  docs/
  README.md
  .gitignore
```

The Java backend is imported as a Maven project; Angular is a separate Node.js application in the **same IntelliJ window**.

## 3. Run the Java backend — inside IntelliJ

**Most reliable beginner route (IntelliJ's built-in Terminal):**

```powershell
cd C:\Projects\ledgerflow\backend
.\mvnw.cmd spring-boot:run
```

This builds and starts the Spring Boot application. Leave that terminal running. Open <http://localhost:8080/actuator/health>; it should report `"status":"UP"`.

**Prefer the green Run triangle?** After Maven import, open:

`backend/src/main/java/com/ledgerflow/LedgerFlowApplication.java`

Click the green triangle beside `main()` and choose **Run**. In **Run > Edit Configurations**, set that application's **Working directory** to `C:\Projects\ledgerflow\backend` (or `$PROJECT_DIR$/backend` if `ledgerflow` is the IntelliJ project root). **This matters:** the app's local H2 database uses a relative `./data` path. The working directory should be `backend` to match the documented terminal run and keep persistent data together.

To avoid a port conflict, start the backend using **only one method at a time**. Both should use JDK 21.

## 4. Run the Angular frontend — also inside IntelliJ

Open **another Terminal tab** via **View > Tool Windows > Terminal** and the `+` button:

```powershell
cd C:\Projects\ledgerflow\frontend
npm.cmd ci
npm.cmd start
```

Open <http://localhost:4200> in your browser. The Angular server forwards API calls to the Java backend. Use `localhost` consistently.

On supported IntelliJ configurations you can also open `frontend/package.json`, click a script's run icon (or use an npm run configuration), and run the **start** script through IntelliJ's Run tool window. This is optional: IntelliJ's built-in Terminal works regardless of Angular-specific IDE features.

Demo login:

- Email: `operator@ledgerflow.demo`
- Password: `DemoPass123!`

These are synthetic local demo credentials. This project does **not** process real money or connect to a bank.

## 5. Run the tests

In IntelliJ's Terminal:

```powershell
cd C:\Projects\ledgerflow\backend
.\mvnw.cmd test
```

You can also right-click `PaymentIntegrationTest.java` in IntelliJ and click **Run** after Maven import. To test the frontend:

```powershell
cd C:\Projects\ledgerflow\frontend
npm.cmd test
npm.cmd run build
```

The optional PostgreSQL integration tests need Docker. You do not need PostgreSQL or Docker for the normal first run because the Java backend uses H2 locally.

## 6. Publish the entire project to GitHub from IntelliJ

**Important:** publish the **`ledgerflow` root** containing both backend and frontend, not the backend module alone.

**IntelliJ UI route:**

1. Open the whole `ledgerflow` folder in IntelliJ.
2. Check the root `.gitignore`; it excludes `node_modules`, `target`, `dist`, `.idea`, `.env` and local database data.
3. Go to **VCS > Share Project on GitHub** if the directory is not already a Git repository; or **Git > GitHub > Share Project on GitHub** if Git is already initialized. IntelliJ may first ask you to sign in to GitHub.
4. Set repository name to `ledgerflow`, add a description and choose Public or Private. Click **Share**.
5. If IntelliJ asks which files to add, review them before committing. Exclude any private credentials, `.env`, build outputs and local databases.
6. Open the new repository in your browser. Confirm that you see `backend/`, `frontend/`, `README.md` and `docs/`.
7. Check the **Actions** tab for automated tests. A successful push does not guarantee every test passed; inspect the results.

**Alternative using only IntelliJ's built-in Terminal:**

```powershell
cd C:\Projects\ledgerflow
git init -b main
git config user.name "Your Name"
git config user.email "YOUR_GITHUB_COMMIT_EMAIL"
git add .
git diff --cached --stat
git commit -m "Initial commit: LedgerFlow Java project"
```

Then create an **empty** repository on GitHub, copy its real HTTPS URL and run:

```powershell
git remote add origin YOUR_GITHUB_REPOSITORY_HTTPS_URL
git push -u origin main
```

Do **not** initialize the new remote with another README or license (those files exist locally). Never use `git push --force` to overcome an unexpected conflict. Do not run both publishing methods if the first one already worked.

## 7. Common IntelliJ problems

| Problem | Fix |
| --- | --- |
| Java files are red / dependencies missing | Load or re-sync `backend/pom.xml` as a Maven project. |
| `invalid target release: 21` | Set Project SDK **and** Maven JDK to 21, then reload Maven. |
| No green Run triangle | Ensure Maven import completed, open `LedgerFlowApplication.java`, or use the integrated Terminal. |
| Java starts but creates database in wrong directory | Set application's Run Configuration Working directory to `ledgerflow/backend`; or run Maven from the backend directory. |
| Angular run icon missing | Use IntelliJ's built-in Terminal with `npm.cmd start`; VS Code is not required. |
| Frontend can't contact backend | Check backend is running on 8080, frontend on 4200 and you are using `localhost`. |
| `Port 8080 already in use` | Stop the previous Java/Docker process before starting another. |
| GitHub doesn't contain frontend | Make sure Git root is `ledgerflow` (the directory containing `backend` and `frontend`). |

## How to continue learning

1. Start in `LedgerFlowApplication.java` (main entry point).
2. Read `ApiController.java` (HTTP endpoints).
3. Explore `TransferService.java` (approval and transfer rules).
4. Open `LedgerService.java` (debit/credit posting).
5. Run `PaymentIntegrationTest.java`.
6. Make one small change, run tests and commit through IntelliJ.

See [Architecture](ARCHITECTURE.md), [Learning Path](LEARNING_PATH.md), and [Demo Walkthrough](DEMO_WALKTHROUGH.md) for the full explanations.
