package com.ledgerflow;

import static com.ledgerflow.ApiModels.*;
import java.time.Instant;
import java.util.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    private final TransferRepository transfers;
    private final AccountRepository accounts;
    private final UserRepository users;
    private final LedgerService ledger;
    private final AuditRepository audits;
    private final JournalEntryRepository entries;
    private final EntityManager entityManager;
    TransferService(TransferRepository transfers, AccountRepository accounts, UserRepository users,
        LedgerService ledger, AuditRepository audits, JournalEntryRepository entries, EntityManager entityManager) {
        this.transfers = transfers; this.accounts = accounts; this.users = users;
        this.ledger = ledger; this.audits = audits; this.entries = entries;
        this.entityManager = entityManager;
    }
    AppUser actor(String email) { return users.findByEmail(email).orElseThrow(() -> new DomainException(401, "Unknown user")); }
    @Transactional
    public CreatedTransfer create(CreateTransfer request, String email) {
        AppUser user = actor(email);
        // Serialize requests from this maker so two identical idempotency keys cannot race.
        users.lockById(user.id).orElseThrow();
        long amount = Money.minor(request.amount());
        Optional<Transfer> previous = transfers.findByCreatedByIdAndIdempotencyKey(user.id, request.idempotencyKey());
        if (previous.isPresent()) {
            Transfer p = previous.get();
            if (!p.fromAccount.id.equals(request.fromAccountId()) || !p.toAccount.id.equals(request.toAccountId()) ||
                p.amountMinor != amount || !p.description.equals(request.description().trim()))
                throw DomainException.conflict("This idempotency key already belongs to a different request");
            return new CreatedTransfer(Views.transfer(p), true);
        }
        if (request.fromAccountId().equals(request.toAccountId())) throw new DomainException(400, "Choose two different merchant accounts");
        Account from = merchant(request.fromAccountId()); Account to = merchant(request.toAccountId());
        Transfer t = new Transfer(); t.reference = "LF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        t.fromAccount = from; t.toAccount = to; t.amountMinor = amount;
        t.description = request.description().trim(); t.createdBy = user; t.idempotencyKey = request.idempotencyKey();
        transfers.save(t); audit(email, "TRANSFER_CREATED", t.id, t.reference);
        return new CreatedTransfer(Views.transfer(t), false);
    }
    private Account merchant(UUID id) {
        Account a = accounts.findById(id).orElseThrow(() -> DomainException.missing("Account not found"));
        if (a.type != AccountType.MERCHANT) throw new DomainException(400, "Only merchant accounts can send or receive transfers");
        return a;
    }
    private Transfer locked(UUID id) { return transfers.lockById(id).orElseThrow(() -> DomainException.missing("Transfer not found")); }
    private void pending(Transfer t, AppUser approver) {
        if (t.status != TransferStatus.PENDING) throw DomainException.conflict("Only pending transfers can be decided");
        if (t.createdBy.id.equals(approver.id)) throw DomainException.conflict("A different user must approve or reject this transfer");
    }
    private Map<UUID, Account> lockAccounts(Transfer t) {
        Map<UUID, Account> locked = new HashMap<>();
        List.of(t.fromAccount.id, t.toAccount.id).stream().sorted().forEach(id -> {
            Account account = accounts.findById(id).orElseThrow();
            // Refresh and lock together: reusing a previously loaded version can raise a stale-version error.
            entityManager.refresh(account, LockModeType.PESSIMISTIC_WRITE); locked.put(id, account);
        });
        return locked;
    }
    @Transactional
    public TransferView approve(UUID id, String email) {
        Transfer t = locked(id); AppUser approver = actor(email); pending(t, approver);
        Map<UUID, Account> locked = lockAccounts(t);
        ledger.post(t, locked.get(t.fromAccount.id), locked.get(t.toAccount.id), approver, EntryType.TRANSFER);
        t.status = TransferStatus.POSTED; t.approvedBy = approver; t.postedAt = Instant.now(); t.updatedAt = t.postedAt;
        audit(email, "TRANSFER_APPROVED", id, t.reference); return Views.transfer(t);
    }
    @Transactional
    public TransferView reject(UUID id, String email, String reason) {
        Transfer t = locked(id); AppUser approver = actor(email); pending(t, approver);
        t.status = TransferStatus.REJECTED; t.approvedBy = approver; t.updatedAt = Instant.now();
        audit(email, "TRANSFER_REJECTED", id, reason); return Views.transfer(t);
    }
    @Transactional
    public TransferView refund(UUID id, String email, String reason) {
        Transfer t = locked(id); if (t.status != TransferStatus.POSTED) throw DomainException.conflict("Only posted transfers can be refunded once");
        Map<UUID, Account> locked = lockAccounts(t);
        ledger.post(t, locked.get(t.toAccount.id), locked.get(t.fromAccount.id), actor(email), EntryType.REFUND);
        t.status = TransferStatus.REFUNDED; t.updatedAt = Instant.now();
        audit(email, "TRANSFER_REFUNDED", id, reason); return Views.transfer(t);
    }
    @Transactional(readOnly = true)
    public PageView<TransferView> search(TransferStatus status, String q, Pageable pageable) {
        return PageView.from(transfers.search(status, q.trim(), pageable).map(Views::transfer));
    }
    @Transactional(readOnly = true)
    public TransferDetail detail(UUID id) {
        Transfer t = transfers.findById(id).orElseThrow(() -> DomainException.missing("Transfer not found"));
        return new TransferDetail(Views.transfer(t), ledger.views(entries.findByTransferIdOrderByCreatedAt(id)));
    }
    void audit(String email, String action, UUID id, String detail) { audits.save(new AuditEvent(email, action, id, detail)); }
}
