package com.ledgerflow;

import static com.ledgerflow.ApiModels.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api")
public class ApiController {
    private final TransferService service; private final TransferRepository transfers;
    private final UserRepository users; private final AccountRepository accounts;
    private final LedgerService ledger; private final JournalEntryRepository entries;
    private final AuditRepository audit; private final ReconciliationService reconciliation;
    private final StatementRowRepository statementRows;
    ApiController(TransferService service, TransferRepository transfers, UserRepository users, AccountRepository accounts,
        LedgerService ledger, JournalEntryRepository entries, AuditRepository audit,
        ReconciliationService reconciliation, StatementRowRepository statementRows) {
        this.service=service; this.transfers=transfers; this.users=users; this.accounts=accounts;
        this.ledger=ledger; this.entries=entries; this.audit=audit; this.reconciliation=reconciliation; this.statementRows=statementRows;
    }
    @GetMapping("/auth/csrf") Map<String,String> csrf(CsrfToken token) { return Map.of("headerName",token.getHeaderName()); }
    @GetMapping("/auth/me") UserView me(Principal principal) {
        if (principal==null) return new UserView(false,null,null,null,null);
        AppUser u=users.findByEmail(principal.getName()).orElseThrow();
        return new UserView(true,u.id,u.email,u.displayName,u.role.name());
    }
    @GetMapping("/accounts") List<AccountView> accounts() { return accounts.findByTypeOrderByName(AccountType.MERCHANT).stream().map(Views::account).toList(); }
    @PostMapping("/accounts") @PreAuthorize("hasRole('ADMIN')") @Transactional
    ResponseEntity<AccountView> createAccount(@Valid @RequestBody CreateAccount request, Principal principal) {
        if (accounts.findByCode(request.code()).isPresent()) throw DomainException.conflict("That account code already exists");
        Account a=accounts.save(new Account(request.code(),request.name().trim(),AccountType.MERCHANT,Direction.CREDIT));
        audit.save(new AuditEvent(principal.getName(),"ACCOUNT_CREATED",a.id,a.code));
        return ResponseEntity.status(201).body(Views.account(a));
    }
    @GetMapping("/transfers") PageView<TransferView> transfers(@RequestParam(required=false) TransferStatus status,
        @RequestParam(defaultValue="") String q,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="12") int size) {
        if(q.length()>140) throw new DomainException(400,"Search text is too long");
        return service.search(status,q,paging(page,size));
    }
    @GetMapping("/transfers/{id}") TransferDetail transfer(@PathVariable UUID id) { return service.detail(id); }
    @PostMapping("/transfers") @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    ResponseEntity<CreatedTransfer> create(@Valid @RequestBody CreateTransfer request,Principal principal) {
        CreatedTransfer created=service.create(request,principal.getName());
        return ResponseEntity.status(created.replayed()?200:201).body(created);
    }
    @PostMapping("/transfers/{id}/approve") @PreAuthorize("hasRole('ADMIN')")
    TransferView approve(@PathVariable UUID id,Principal principal) { return service.approve(id,principal.getName()); }
    @PostMapping("/transfers/{id}/reject") @PreAuthorize("hasRole('ADMIN')")
    TransferView reject(@PathVariable UUID id,@Valid @RequestBody Reason reason,Principal principal) { return service.reject(id,principal.getName(),reason.reason()); }
    @PostMapping("/transfers/{id}/refund") @PreAuthorize("hasRole('ADMIN')")
    TransferView refund(@PathVariable UUID id,@Valid @RequestBody Reason reason,Principal principal) { return service.refund(id,principal.getName(),reason.reason()); }
    @GetMapping("/journal") @Transactional(readOnly=true)
    PageView<JournalView> journal(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="12") int size) {
        Page<JournalEntry> result=entries.findAllByOrderByCreatedAtDesc(paging(page,size));
        return new PageView<>(ledger.views(result.getContent()),result.getTotalElements(),result.getTotalPages(),page,result.getSize());
    }
    @GetMapping("/dashboard") @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    DashboardView dashboard() {
        List<Transfer> posted=transfers.findAll().stream().filter(t->t.status==TransferStatus.POSTED).toList();
        LocalDate today=LocalDate.now(ZoneOffset.UTC); List<DailyVolume> daily=new ArrayList<>();
        for(int i=6;i>=0;i--) { LocalDate day=today.minusDays(i);
            daily.add(new DailyVolume(day.toString(),posted.stream().filter(t->t.postedAt!=null && t.postedAt.atZone(ZoneOffset.UTC).toLocalDate().equals(day)).mapToLong(t->t.amountMinor).sum()));
        }
        return new DashboardView(accounts.findByTypeOrderByName(AccountType.MERCHANT).stream().mapToLong(a->a.balanceMinor).sum(),
            transfers.volume(TransferStatus.POSTED),transfers.countByStatus(TransferStatus.PENDING),
            statementRows.countByStatusNot(ReconciliationStatus.MATCHED),transfers.countByStatus(TransferStatus.POSTED),ledger.integrity(),daily,
            transfers.findTop6ByOrderByCreatedAtDesc().stream().map(Views::transfer).toList());
    }
    @GetMapping("/audit") @PreAuthorize("hasRole('ADMIN')")
    PageView<AuditView> audit(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="15") int size) {
        return PageView.from(audit.findAllByOrderByCreatedAtDesc(paging(page,size)).map(Views::audit));
    }
    @PostMapping(value="/reconciliation/imports",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')") @ResponseStatus(HttpStatus.CREATED)
    ImportDetail upload(@RequestPart("file") MultipartFile file,Principal principal) { return reconciliation.upload(file,principal.getName()); }
    @GetMapping("/reconciliation/imports") PageView<ImportView> imports(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="12") int size) { return reconciliation.list(paging(page,size)); }
    @GetMapping("/reconciliation/imports/{id}") ImportDetail imported(@PathVariable UUID id) { return reconciliation.detail(id); }
    @GetMapping(value="/reconciliation/sample",produces="text/csv") @Transactional(readOnly=true)
    ResponseEntity<String> sample() {
        Transfer t=transfers.findAll().stream().filter(v->v.status==TransferStatus.POSTED).findFirst().orElse(null);
        String row=t==null?"LF-UNKNOWN,125.00,USD\n":t.reference+","+Money.decimal(t.amountMinor)+",USD\n"+t.reference+","+Money.decimal(t.amountMinor)+",USD\n";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=ledgerflow-statement.csv")
            .body("reference,amount,currency\n"+row+"BANK-UNRECOGNIZED,49.00,USD\n");
    }
    static Pageable paging(int page,int size) {
        if(page<0||size<1||size>100) throw new DomainException(400,"Page must be nonnegative; size must be between 1 and 100");
        return PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt"));
    }
}
