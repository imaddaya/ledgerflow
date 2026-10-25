package com.ledgerflow;

import static com.ledgerflow.ApiModels.*;
import java.util.*;

final class Views {
    static AccountView account(Account a) { return new AccountView(a.id, a.code, a.name, a.currency, a.balanceMinor, a.type.name()); }
    static TransferView transfer(Transfer t) {
        return new TransferView(t.id, t.reference, t.fromAccount.id, t.fromAccount.name, t.toAccount.id, t.toAccount.name,
            t.amountMinor, t.currency, t.status.name(), t.description, t.createdBy.email,
            t.approvedBy == null ? null : t.approvedBy.email, t.createdAt, t.updatedAt, t.postedAt);
    }
    static AuditView audit(AuditEvent a) { return new AuditView(a.id, a.actorEmail, a.action, a.entityId, a.detail, a.createdAt); }
}
