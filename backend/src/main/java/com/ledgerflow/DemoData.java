package com.ledgerflow;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoData implements CommandLineRunner {
    private final UserRepository users; private final AccountRepository accounts; private final LedgerService ledger;
    private final TransferService service; private final TransferRepository transfers; private final AuditRepository audit;
    private final PasswordEncoder passwords;
    @Value("${app.demo-seed}") boolean enabled;
    @Value("${app.demo-password}") String demoPassword;
    DemoData(UserRepository users,AccountRepository accounts,LedgerService ledger,TransferService service,
        TransferRepository transfers,AuditRepository audit,PasswordEncoder passwords) {
        this.users=users;this.accounts=accounts;this.ledger=ledger;this.service=service;this.transfers=transfers;this.audit=audit;this.passwords=passwords;
    }
    @Override @Transactional public void run(String... args) {
        if(!enabled || users.count()>0) return;
        users.saveAll(List.of(new AppUser("operator@ledgerflow.demo","Maya · Operator",passwords.encode(demoPassword),Role.OPERATOR),
            new AppUser("approver@ledgerflow.demo","Omar · Approver",passwords.encode(demoPassword),Role.ADMIN),
            new AppUser("viewer@ledgerflow.demo","Sam · Analyst",passwords.encode(demoPassword),Role.VIEWER)));
        Account reserve=accounts.save(new Account("RESERVE","Synthetic settlement reserve",AccountType.RESERVE,Direction.DEBIT));
        List<Account> merchants=accounts.saveAll(List.of(new Account("CEDAR","Cedar Coffee",AccountType.MERCHANT,Direction.CREDIT),
            new Account("NILE","Nile Studio",AccountType.MERCHANT,Direction.CREDIT),new Account("ATLAS","Atlas Supply",AccountType.MERCHANT,Direction.CREDIT),
            new Account("OLIVE","Olive Market",AccountType.MERCHANT,Direction.CREDIT)));
        long[] balances={4_850_000,3_240_000,6_800_000,2_100_000};
        for(int i=0;i<merchants.size();i++) ledger.opening(reserve,merchants.get(i),balances[i]);
        for(int i=0;i<10;i++) {
            var result=service.create(new ApiModels.CreateTransfer(merchants.get(i%4).id,merchants.get((i+1)%4).id,
                BigDecimal.valueOf(12500L+i*3750L,2),List.of("Weekly supplier settlement","Invoice payment","Merchant service fee","Inventory purchase").get(i%4),"seed-transfer-"+i),"operator@ledgerflow.demo");
            Transfer t=transfers.findById(result.transfer().id()).orElseThrow();
            if(i<8) {
                service.approve(t.id,"approver@ledgerflow.demo");
                t.postedAt=Instant.now().minus(Duration.ofDays(6-i%7));
            }
            t.createdAt=Instant.now().minus(Duration.ofDays(7-i%7)); t.updatedAt=t.postedAt==null?t.createdAt:t.postedAt;
        }
        audit.save(new AuditEvent("system","DEMO_INITIALIZED",reserve.id,"Synthetic USD balances; no real funds or bank connection"));
    }
}
