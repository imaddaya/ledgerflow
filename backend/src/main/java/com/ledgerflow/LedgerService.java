package com.ledgerflow;

import static com.ledgerflow.ApiModels.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {
    private final JournalEntryRepository entries;
    private final JournalLineRepository lines;
    private final AccountRepository accounts;
    LedgerService(JournalEntryRepository entries, JournalLineRepository lines, AccountRepository accounts) {
        this.entries = entries; this.lines = lines; this.accounts = accounts;
    }
    // Called inside the transfer transaction after both merchant accounts are locked.
    JournalEntry post(Transfer transfer, Account sender, Account receiver, AppUser actor, EntryType type) {
        if (sender.balanceMinor < transfer.amountMinor) throw DomainException.conflict("Insufficient available balance");
        sender.balanceMinor = Math.subtractExact(sender.balanceMinor, transfer.amountMinor);
        receiver.balanceMinor = Math.addExact(receiver.balanceMinor, transfer.amountMinor);
        JournalEntry entry = new JournalEntry(); entry.transfer = transfer; entry.entryType = type;
        entry.description = (type == EntryType.REFUND ? "Reversal: " : "Payment: ") + transfer.reference;
        entry.postedBy = actor; entries.save(entry);
        // Merchant balances are credit-normal liabilities: debit decreases the sender.
        lines.saveAll(List.of(new JournalLine(entry, sender, Direction.DEBIT, transfer.amountMinor),
            new JournalLine(entry, receiver, Direction.CREDIT, transfer.amountMinor)));
        return entry;
    }
    void opening(Account reserve, Account merchant, long amount) {
        merchant.balanceMinor = amount; reserve.balanceMinor += amount;
        JournalEntry entry = new JournalEntry(); entry.entryType = EntryType.OPENING;
        entry.description = "Synthetic opening balance: " + merchant.code; entries.save(entry);
        lines.saveAll(List.of(new JournalLine(entry, reserve, Direction.DEBIT, amount),
            new JournalLine(entry, merchant, Direction.CREDIT, amount)));
    }
    @Transactional(readOnly = true)
    public IntegrityView integrity() {
        LedgerTotals totals = lines.totals(Direction.DEBIT, Direction.CREDIT);
        Map<UUID, Long> computed = lines.accountBalances().stream()
            .collect(Collectors.toMap(AccountLedgerBalance::getAccountId, AccountLedgerBalance::getBalanceMinor));
        boolean match = accounts.findAll().stream().allMatch(a -> a.balanceMinor == computed.getOrDefault(a.id, 0L));
        return new IntegrityView(totals.getDebitMinor(), totals.getCreditMinor(),
            totals.getDebitMinor().equals(totals.getCreditMinor()), match);
    }
    @Transactional(readOnly = true)
    public List<JournalView> views(List<JournalEntry> journal) {
        if (journal.isEmpty()) return List.of();
        Map<UUID, List<JournalLine>> grouped = lines.findByEntryIdIn(journal.stream().map(e -> e.id).toList())
            .stream().collect(Collectors.groupingBy(l -> l.entry.id));
        return journal.stream().map(e -> new JournalView(e.id, e.transfer == null ? "OPENING" : e.transfer.reference,
            e.entryType.name(), e.description, e.createdAt, e.postedBy == null ? "Demo initializer" : e.postedBy.email,
            grouped.getOrDefault(e.id, List.of()).stream().map(l -> new JournalLineView(l.account.code,
                l.account.name, l.direction.name(), l.amountMinor)).toList())).toList();
    }
}
