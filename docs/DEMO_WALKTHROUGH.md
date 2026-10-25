# Demonstrate the project in five minutes

Start both applications using [WINDOWS_SETUP.md](WINDOWS_SETUP.md), then open http://localhost:4200.

1. **Sign in as operator.** Use `operator@ledgerflow.demo` / `DemoPass123!`. Review the dashboard's actual balances and ledger-health check.
2. **Create a transfer.** Select Cedar Coffee → Nile Studio, enter `125.50` and description `Invoice LF-DEMO-01`. Confirm. Notice that the payment is pending and balances have not changed.
3. **Switch to approver.** Use the sign-out icon at the bottom of the sidebar. Sign in as `approver@ledgerflow.demo` with the same demo password.
4. **Approve.** Go to Transfers, select Pending, apply filters, open your new request and click **Approve & post**. The payment becomes posted.
5. **Trace the accounting.** In the transfer details, inspect the equal debit and credit lines. Open Accounts: Cedar decreased by $125.50 and Nile increased by the same amount. The dashboard ledger checks should still agree.
6. **Reconcile.** Open Reconciliation, download the current sample CSV and upload it. Expect one match, one repeated-reference exception and one unknown-reference exception, provided a posted demo transfer exists. Inspect the result rows and import history.
7. **Refund.** Reopen your posted payment, select full refund and enter `Demo reversal`. The original journal remains and a second reversing entry appears. Both merchant balances return to their prior values.
8. **Inspect controls.** Audit log records create/approve/refund/import actions. Sign in as the viewer: write buttons disappear, and the API independently denies write requests.

Try the unhappy paths as well: same sending/receiving account, `1.001` amount, insufficient funds, malformed CSV and repeated approval. Describe the resulting validation or conflict rather than promising every request succeeds.

Demo records persist on your laptop. A new session uses the existing records rather than resetting the balances. The default credentials are synthetic and intended for this local demonstration.
