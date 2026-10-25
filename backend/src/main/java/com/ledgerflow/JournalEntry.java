package com.ledgerflow;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "journal_entries")
public class JournalEntry {
    @Id UUID id = UUID.randomUUID();
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "transfer_id") Transfer transfer;
    @Enumerated(EnumType.STRING) @Column(nullable = false) EntryType entryType;
    @Column(nullable = false) String description;
    @Column(nullable = false) Instant createdAt = Instant.now();
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "posted_by") AppUser postedBy;
    protected JournalEntry() {}
}
