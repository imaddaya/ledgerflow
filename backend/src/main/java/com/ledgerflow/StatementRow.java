package com.ledgerflow;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name = "statement_rows")
public class StatementRow {
    @Id UUID id = UUID.randomUUID();
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "import_id") StatementImport statementImport;
    @Column(nullable = false) int rowNumber;
    @Column(nullable = false) String reference;
    @Column(nullable = false) long amountMinor;
    @Column(nullable = false) String currency;
    @Enumerated(EnumType.STRING) @Column(name = "row_status", nullable = false) ReconciliationStatus status;
    @Column(nullable = false) String note;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "transfer_id") Transfer transfer;
    protected StatementRow() {}
}
