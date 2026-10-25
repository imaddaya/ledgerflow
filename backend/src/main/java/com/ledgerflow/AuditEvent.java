package com.ledgerflow;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "audit_events")
public class AuditEvent {
    @Id UUID id = UUID.randomUUID();
    @Column(nullable = false) String actorEmail;
    @Column(nullable = false) String action;
    UUID entityId;
    @Column(nullable = false) String detail;
    @Column(nullable = false) Instant createdAt = Instant.now();
    protected AuditEvent() {}
    AuditEvent(String actor, String action, UUID entityId, String detail) {
        this.actorEmail = actor; this.action = action; this.entityId = entityId; this.detail = detail;
    }
}
