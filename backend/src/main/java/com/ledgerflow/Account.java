package com.ledgerflow;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name = "accounts")
public class Account {
    @Id UUID id = UUID.randomUUID();
    @Column(nullable = false, unique = true) String code;
    @Column(nullable = false) String name;
    @Enumerated(EnumType.STRING) @Column(name = "account_type", nullable = false) AccountType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false) Direction normalBalance;
    @Column(nullable = false) String currency = "USD";
    @Column(nullable = false) long balanceMinor;
    @Version long version;
    protected Account() {}
    Account(String code, String name, AccountType type, Direction normalBalance) {
        this.code = code; this.name = name; this.type = type; this.normalBalance = normalBalance;
    }
}
