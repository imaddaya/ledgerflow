package com.ledgerflow;

import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

/** Run with ./mvnw -Ppostgres-tests verify when Docker is running. */
@SpringBootTest @ActiveProfiles("postgres") @Testcontainers
class PostgresIT {
    @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17-alpine");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);
    }
    @Autowired TransferService transfers;@Autowired AccountRepository accounts;@Autowired LedgerService ledger;
    @Test void flywaySchemaAndDoubleEntryWorkOnPostgres() {
        var from=accounts.findByCode("CEDAR").orElseThrow();var to=accounts.findByCode("NILE").orElseThrow();
        var result=transfers.create(new ApiModels.CreateTransfer(from.id,to.id,new BigDecimal("5.25"),"Postgres integration",UUID.randomUUID().toString()),"operator@ledgerflow.demo");
        transfers.approve(result.transfer().id(),"approver@ledgerflow.demo");
        assertThat(ledger.integrity().balanced()).isTrue();assertThat(ledger.integrity().accountBalancesMatch()).isTrue();
        assertThat(transfers.detail(result.transfer().id()).journal()).hasSize(1);
    }
}
