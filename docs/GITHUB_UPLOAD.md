# Upload LedgerFlow to your GitHub, step by step

The project has been prepared locally. It has **not** been published to your account. These steps create your own repository and publish the source code.

## 1. Check the project before publishing

Run the backend tests and frontend build using [WINDOWS_SETUP.md](WINDOWS_SETUP.md). Open the app and complete a payment/approval/refund demo. Read [ARCHITECTURE.md](ARCHITECTURE.md) so you understand what you are presenting.

The supplied `.gitignore` excludes `node_modules`, compiled output, local databases and `.env`. The visible demo password in README and demo configuration is intentional synthetic demo data. Do not substitute a personal password, a real bank key or a private customer record.

Your CV, phone numbers and private job tracker are separate deliverables and are not included in this repository ZIP.

## 2. Create an empty repository on the GitHub website

1. Sign in at [github.com](https://github.com).
2. Select **+ → New repository**.
3. Choose your own account as owner, expected to be `imaddaya` if that remains your username.
4. Repository name: **ledgerflow**. If that name already exists in your account, choose another name and use that exact name in the commands below.
5. Description: `Java and Angular merchant payments sandbox with double-entry accounting, approval controls and CSV reconciliation.`
6. Choose **Public** if you want recruiters to view the source, or **Private** while learning.
7. Leave **Add a README**, **Add .gitignore** and **Choose a license** unchecked. Those files are already supplied locally.
8. Click **Create repository**. Copy the HTTPS repository URL shown on the next page.

## 3. Initialize Git in the extracted root folder

Open PowerShell in `C:\Projects\ledgerflow`. The commands operate on the root containing both `backend` and `frontend`.

```powershell
cd C:\Projects\ledgerflow
git init -b main
git config user.name "Imadeddine Dayeh"
```

Set the commit email to the email you want associated with commits. For privacy, copy the exact noreply address from **GitHub → Settings → Emails**:

```powershell
git config user.email "PASTE_YOUR_GITHUB_COMMIT_EMAIL_HERE"
```

Replace that placeholder before running it. These settings are local to this repository.

## 4. Inspect and commit

```powershell
git status
git add .
git diff --cached --stat
git status
```

Check that source, documentation, Maven wrapper, npm lockfile and CI workflow are staged. You should **not** see `.env`, `backend/data`, `backend/target`, `frontend/node_modules` or `frontend/dist`.

Then create the first commit:

```powershell
git commit -m "Add LedgerFlow merchant payments learning project"
```

If Git reports nothing to commit, check whether you already created the commit. Use `git log --oneline -3`; do not repeatedly initialize or force-push.

## 5. Connect the repository and push

If your GitHub username is still `imaddaya` and you created `ledgerflow`, use:

```powershell
git remote add origin https://github.com/imaddaya/ledgerflow.git
git remote -v
git push -u origin main
```

Otherwise replace the URL with the exact HTTPS URL copied from your new GitHub repository. On the first push, Git Credential Manager may open a browser. Sign in there and complete GitHub's authentication flow. A normal GitHub account password is not accepted as a Git HTTPS password; use the supported browser flow rather than putting a password or token into a command or URL.

If `origin already exists`, inspect `git remote -v` first. Only if it is the wrong URL for this local project, change it with `git remote set-url origin YOUR_CORRECT_HTTPS_URL`. Do not delete a remote blindly.

If a push is rejected because the remote already contains a README or other commit, stop and inspect it. The cleanest beginner route is a different empty repository. Do not use `--force` to overwrite existing work.

## 6. Verify what GitHub received

1. Refresh the repository page. Check that README is visible and both source folders exist.
2. Open **Actions → Verify LedgerFlow**. The included workflow runs Java tests, the PostgreSQL Testcontainers test, Angular tests and the Angular production build on GitHub's runner.
3. Wait for the jobs to finish. A green check is evidence of that run; the included YAML file alone is not evidence that CI passed.
4. If a job fails, open its log, reproduce the failing command locally, fix the issue, commit and push again.

This delivery did not execute Docker or GitHub Actions. The first real PostgreSQL/CI run is therefore an explicit validation step after upload.

## 7. Make it a convincing portfolio project

- Add repository topics: `java`, `spring-boot`, `angular`, `hibernate`, `postgresql`, `fintech`, `double-entry-accounting`.
- Run your real application and capture screenshots of the dashboard, a transfer's journal and a reconciliation result. Save them under `docs/images/` and embed them in README. Do not use a mock screenshot as proof of the running app.
- Record a short demo: operator creates → approver posts → ledger balances → CSV identifies an exception → refund reverses the entry.
- Explain that you used AI assistance, what you verified and which changes you implemented yourself. Keep the learning-project label until you can explain the code confidently.
- Pin the repository on your GitHub profile and add the **actual live repository URL** to your CV once it exists.

The CV provided with this delivery links to your existing GitHub profile rather than an unpublished repository URL.

## 8. Publish your own later changes

After modifying source or documentation:

```powershell
git status
git add .
git diff --cached --stat
git commit -m "Explain the specific change you made"
git push
```

Use clear commit messages such as `Add reconciliation currency mismatch test`. Make real incremental improvements; do not manufacture historical commits or experience dates.

Official reference: [GitHub — Adding locally hosted code](https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github?platform=windows).
