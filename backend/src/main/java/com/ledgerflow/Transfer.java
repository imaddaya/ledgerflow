package com.ledgerflow;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "transfers")
public class Transfer {
    @Id UUID id = UUID.randomUUID();
    @Column(nullable = false, unique = true) String reference;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "from_account_id") Account fromAccount;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "to_account_id") Account toAccount;
    @Column(nullable = false) long amountMinor;
    @Column(nullable = false) String currency = "USD";
    @Enumerated(EnumType.STRING) @Column(name = "transfer_status", nullable = false) TransferStatus status = TransferStatus.PENDING;
    @Column(nullable = false) String description;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "created_by") AppUser createdBy;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "approved_by") AppUser approvedBy;
    @Column(nullable = false) Instant createdAt = Instant.now();
    @Column(nullable = false) Instant updatedAt = Instant.now();
    Instant postedAt;
    @Column(nullable = false) String idempotencyKey;
    @Version long version;
    protected Transfer() {}
}
