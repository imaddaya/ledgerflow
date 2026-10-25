import { Component, HostListener, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Account, Audit, Dashboard, Journal, Page, Statement, StatementDetail, Transfer, TransferDetail, User, transferError } from './models';

type Section = 'Overview' | 'Transfers' | 'Accounts' | 'Journal' | 'Reconciliation' | 'Audit log';
@Component({ selector: 'app-root', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './app.html' })
export class App {
  private http = inject(HttpClient);
  user = signal<User | null>(null); booting = signal(true); busy = signal(false); loading = signal(false);
  error = signal(''); notice = signal(''); section = signal<Section>('Overview');
  data = signal<Dashboard | null>(null); accounts = signal<Account[]>([]);
  transfers = signal<Page<Transfer> | null>(null); journal = signal<Page<Journal> | null>(null);
  statements = signal<Page<Statement> | null>(null); audits = signal<Page<Audit> | null>(null);
  detail = signal<TransferDetail | null>(null); statement = signal<StatementDetail | null>(null);
  modal = signal<'transfer' | 'account' | null>(null); decision = signal<'reject' | 'refund' | null>(null);
  readonly sections: Section[] = ['Overview', 'Transfers', 'Accounts', 'Journal', 'Reconciliation', 'Audit log'];
  readonly symbols: Record<Section,string> = {Overview:'◫',Transfers:'⇄',Accounts:'▤',Journal:'≡',Reconciliation:'✓','Audit log':'◷'};
  email = 'operator@ledgerflow.demo'; password = 'DemoPass123!'; status = ''; search = ''; page = 0;
  from = ''; to = ''; amount = ''; description = ''; requestKey = ''; code = ''; accountName = ''; reason = '';
  constructor() { void this.initialize(); }
  get isAdmin() { return this.user()?.role === 'ADMIN'; }
  get canWrite() { return this.user()?.role !== 'VIEWER'; }
  get availableSections() { return this.sections.filter(s => s !== 'Audit log' || this.isAdmin); }
  get titleText() { return ({Overview:'Your operations, in balance.',Transfers:'Move money with confidence.',Accounts:'Every merchant, accounted for.',Journal:'The record behind every movement.',Reconciliation:'Close the gap.', 'Audit log':'A clear trail of decisions.'})[this.section()]; }
  get subtitle() { return ({Overview:'A live view of merchant balances, approvals and payment activity.',Transfers:'Create, review and trace payments from request to ledger.',Accounts:'Synthetic USD balances backed by a double-entry journal.',Journal:'Immutable postings. Equal debits and credits. Complete traceability.',Reconciliation:'Compare statement records with your internal payments.', 'Audit log':'Review who changed what, and when.'})[this.section()]; }
  private get<T>(url: string) { return firstValueFrom(this.http.get<T>('/api' + url)); }
  private post<T>(url: string, body: unknown) { return firstValueFrom(this.http.post<T>('/api' + url, body)); }
  private fail(e: unknown) {
    this.error.set(e instanceof HttpErrorResponse ? (e.error?.message || (e.status === 0 ? 'Cannot reach the API. Check that the backend is running on port 8080.' : 'The request failed. Please try again.')) : 'Something went wrong. Please try again.');
    if(e instanceof HttpErrorResponse && e.status===401 && this.user()?.authenticated) { this.user.set(null); }
  }
  async initialize() { try { await this.get('/auth/csrf'); const u=await this.get<User>('/auth/me'); this.user.set(u.authenticated?u:null); if(u.authenticated) await this.load(); } catch(e) { this.fail(e); } finally { this.booting.set(false); } }
  chooseRole(role: string) { this.email = role + '@ledgerflow.demo'; }
  async login() {
    this.busy.set(true); this.error.set('');
    try { await this.get('/auth/csrf'); await this.post('/auth/login', new HttpParams().set('username',this.email).set('password',this.password)); await this.get('/auth/csrf'); this.user.set(await this.get<User>('/auth/me')); this.password=''; await this.load(); }
    catch(e) { this.fail(e); } finally { this.busy.set(false); }
  }
  async logout() { this.busy.set(true); try { await this.post('/auth/logout',{}); this.user.set(null); this.section.set('Overview'); this.detail.set(null); this.statement.set(null); this.password='DemoPass123!'; await this.get('/auth/csrf'); } catch(e) { this.fail(e); } finally {this.busy.set(false);} }
  async navigate(section: Section) { this.section.set(section); this.page=0; this.detail.set(null); this.statement.set(null); this.error.set(''); this.notice.set(''); await this.load(); }
  async load() {
    this.loading.set(true);
    try {
      const accounts = await this.get<Account[]>('/accounts'); this.accounts.set(accounts);
      switch(this.section()) {
        case 'Overview': this.data.set(await this.get<Dashboard>('/dashboard')); break;
        case 'Transfers': this.transfers.set(await this.get<Page<Transfer>>('/transfers?page='+this.page+'&q='+encodeURIComponent(this.search)+(this.status?'&status='+this.status:''))); break;
        case 'Journal': this.journal.set(await this.get<Page<Journal>>('/journal?page='+this.page)); break;
        case 'Reconciliation': this.statements.set(await this.get<Page<Statement>>('/reconciliation/imports?page='+this.page)); break;
        case 'Audit log': this.audits.set(await this.get<Page<Audit>>('/audit?page='+this.page)); break;
      }
    } catch(e) { this.fail(e); } finally { this.loading.set(false); }
  }
  async filter() { this.page=0; await this.load(); }
  async paginate(delta: number) { this.page+=delta; await this.load(); }
  money(minor: number, currency='USD') { return new Intl.NumberFormat('en-US',{style:'currency',currency}).format(minor/100); }
  date(value: string) { return new Intl.DateTimeFormat('en-GB',{day:'2-digit',month:'short',hour:'2-digit',minute:'2-digit'}).format(new Date(value)); }
  day(value: string) { return new Intl.DateTimeFormat('en-US',{weekday:'short',timeZone:'UTC'}).format(new Date(value+'T00:00:00Z')); }
  label(value: string) { return value.replaceAll('_',' ').toLowerCase(); }
  bar(value: number) { const max=Math.max(...(this.data()?.dailyVolume.map(d=>d.amountMinor)??[1]),1); return Math.max(0,value/max*100); }
  newTransfer() { this.from=this.accounts()[0]?.id??''; this.to=this.accounts()[1]?.id??''; this.amount='';this.description='';this.requestKey=crypto.randomUUID();this.error.set('');this.modal.set('transfer'); }
  async saveTransfer() {
    const error=transferError(this.from,this.to,this.amount,this.description); if(error) {this.error.set(error);return;}
    this.busy.set(true);this.error.set('');
    try { const result=await this.post<{transfer:Transfer;replayed:boolean}>('/transfers',{fromAccountId:this.from,toAccountId:this.to,amount:this.amount,description:this.description,idempotencyKey:this.requestKey});
      this.modal.set(null);this.notice.set(result.transfer.reference+' is awaiting approval. Sign in as the approver to post it.'); await this.load(); }
    catch(e) {this.fail(e);} finally {this.busy.set(false);}
  }
  async openTransfer(t: Transfer) { this.error.set(''); try {this.detail.set(await this.get<TransferDetail>('/transfers/'+t.id));this.decision.set(null);} catch(e){this.fail(e);} }
  async act(action: 'approve' | 'reject' | 'refund') {
    const t=this.detail()?.transfer; if(!t) return;
    if(action!=='approve' && !this.reason.trim()) {this.error.set('Add a reason before confirming.');return;}
    this.busy.set(true);this.error.set('');
    try {await this.post('/transfers/'+t.id+'/'+action, action==='approve'?{}:{reason:this.reason}); this.detail.set(await this.get<TransferDetail>('/transfers/'+t.id));this.decision.set(null);this.notice.set('Transfer updated. The ledger and audit trail are in sync.');await this.load();}
    catch(e){this.fail(e);}finally{this.busy.set(false);}
  }
  startDecision(action: 'reject' | 'refund') {this.reason='';this.decision.set(action);}
  newAccount(){this.code='';this.accountName='';this.error.set('');this.modal.set('account');}
  async saveAccount(){
    this.busy.set(true);this.error.set('');
    try {await this.post('/accounts',{code:this.code,name:this.accountName});this.modal.set(null);this.notice.set('Merchant account created with a zero opening balance.');await this.load();}
    catch(e){this.fail(e);}finally{this.busy.set(false);}
  }
  async upload(event: Event) {
    const input=event.target as HTMLInputElement; const file=input.files?.[0]; if(!file)return;
    if(file.size>1_048_576){this.error.set('Choose a CSV smaller than 1 MB.');input.value='';return;}
    const body=new FormData();body.append('file',file);this.busy.set(true);this.error.set('');
    try{this.statement.set(await this.post<StatementDetail>('/reconciliation/imports',body));this.notice.set('Statement checked against the internal ledger.');await this.load();}
    catch(e){this.fail(e);}finally{this.busy.set(false);input.value='';}
  }
  async openStatement(s:Statement){try{this.statement.set(await this.get<StatementDetail>('/reconciliation/imports/'+s.id));}catch(e){this.fail(e);}}
  close(){if(!this.busy()){this.modal.set(null);this.detail.set(null);this.decision.set(null);this.error.set('');}}
  @HostListener('document:keydown.escape') escape(){this.close();}
}
