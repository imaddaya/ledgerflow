package com.ledgerflow;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name = "app_users")
public class AppUser {
    @Id UUID id = UUID.randomUUID();
    @Column(nullable = false, unique = true) String email;
    @Column(nullable = false) String displayName;
    @Column(nullable = false) String passwordHash;
    @Enumerated(EnumType.STRING) @Column(name = "user_role", nullable = false) Role role;
    @Version long version;
    protected AppUser() {}
    AppUser(String email, String name, String passwordHash, Role role) {
        this.email = email; this.displayName = name; this.passwordHash = passwordHash; this.role = role;
    }
}
