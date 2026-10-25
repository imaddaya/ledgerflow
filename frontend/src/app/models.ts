export interface User { authenticated: boolean; id: string; email: string; displayName: string; role: 'ADMIN' | 'OPERATOR' | 'VIEWER'; }
export interface Account { id: string; code: string; name: string; currency: string; balanceMinor: number; type: string; }
export interface Transfer { id: string; reference: string; fromAccountId: string; fromAccount: string; toAccountId: string; toAccount: string; amountMinor: number; currency: string; status: string; description: string; createdBy: string; approvedBy: string | null; createdAt: string; updatedAt: string; postedAt: string | null; }
export interface Page<T> { content: T[]; totalElements: number; totalPages: number; page: number; size: number; }
export interface Integrity { debitMinor: number; creditMinor: number; balanced: boolean; accountBalancesMatch: boolean; }
export interface Dashboard { totalBalanceMinor: number; postedVolumeMinor: number; pendingCount: number; exceptionCount: number; postedCount: number; ledger: Integrity; dailyVolume: {day: string; amountMinor: number}[]; recentTransfers: Transfer[]; }
export interface Journal { id: string; reference: string; type: string; description: string; createdAt: string; postedBy: string; lines: {accountCode: string; accountName: string; direction: string; amountMinor: number}[]; }
export interface TransferDetail { transfer: Transfer; journal: Journal[]; }
export interface Statement { id: string; fileName: string; uploadedBy: string; createdAt: string; totalRows: number; matchedRows: number; exceptions: number; }
export interface StatementDetail { summary: Statement; rows: {rowNumber: number; reference: string; amountMinor: number; currency: string; status: string; note: string}[]; }
export interface Audit { id: string; actorEmail: string; action: string; entityId: string; detail: string; createdAt: string; }
export function validAmount(value: string): boolean { return /^\d{1,7}(\.\d{1,2})?$/.test(value) && Number(value) > 0 && Number(value) <= 1_000_000; }
export function transferError(from: string, to: string, amount: string, description: string): string | null {
  if (!from || !to) return 'Choose a sending and receiving account.';
  if (from === to) return 'Choose two different accounts.';
  if (!validAmount(amount)) return 'Enter an amount from 0.01 to 1,000,000 with at most two decimal places.';
  if (!description.trim() || description.length > 140) return 'Add a description of up to 140 characters.';
  return null;
}
