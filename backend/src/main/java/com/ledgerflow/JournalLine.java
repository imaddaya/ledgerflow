package com.ledgerflow;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name = "journal_lines")
public class JournalLine {
    @Id UUID id = UUID.randomUUID();
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "entry_id") JournalEntry entry;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "account_id") Account account;
    @Enumerated(EnumType.STRING) @Column(nullable = false) Direction direction;
    @Column(nullable = false) long amountMinor;
    protected JournalLine() {}
    JournalLine(JournalEntry entry, Account account, Direction direction, long amount) {
        this.entry = entry; this.account = account; this.direction = direction; this.amountMinor = amount;
    }
}
