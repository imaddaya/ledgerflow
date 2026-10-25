package com.ledgerflow;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "statement_imports")
public class StatementImport {
    @Id UUID id = UUID.randomUUID();
    @Column(nullable = false) String fileName;
    @Column(nullable = false) String uploadedBy;
    @Column(nullable = false) Instant createdAt = Instant.now();
    protected StatementImport() {}
}
