package com.ledgerflow;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpSession;
import jakarta.servlet.http.Cookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @ActiveProfiles("test") @AutoConfigureMockMvc
class PaymentIntegrationTest {
    @Autowired TransferService service; @Autowired AccountRepository accounts;
    @Autowired TransferRepository transfers; @Autowired LedgerService ledger;
    @Autowired JournalEntryRepository journal; @Autowired JournalLineRepository lines;
    @Autowired AuditRepository audit; @Autowired ReconciliationService reconciliation;
    @Autowired StatementImportRepository imports; @Autowired UserRepository users; @Autowired MockMvc mvc;
    static final String MAKER="operator@ledgerflow.demo", APPROVER="approver@ledgerflow.demo";
    UUID from, to;
    @BeforeEach void setup() { from=accounts.findByCode("CEDAR").orElseThrow().id; to=accounts.findByCode("NILE").orElseThrow().id; }
    ApiModels.CreateTransfer request(String amount,String key){return new ApiModels.CreateTransfer(from,to,new BigDecimal(amount),"Integration test",key);}
    UUID create(String amount){return service.create(request(amount,UUID.randomUUID().toString()),MAKER).transfer().id();}
    long balance(UUID id){return accounts.findById(id).orElseThrow().balanceMinor;}

    @Test void approvalPostsExactlyOneBalancedEntryAndRefundReversesIt() {
        long a=balance(from),b=balance(to);UUID id=create("15.25");
        assertThat(balance(from)).isEqualTo(a);service.approve(id,APPROVER);
        assertThat(balance(from)).isEqualTo(a-1525);assertThat(balance(to)).isEqualTo(b+1525);
        assertThat(service.detail(id).journal()).hasSize(1);
        assertThatThrownBy(()->service.approve(id,APPROVER)).isInstanceOf(DomainException.class);
        service.refund(id,APPROVER,"Duplicate invoice");
        assertThat(balance(from)).isEqualTo(a);assertThat(balance(to)).isEqualTo(b);
        assertThat(service.detail(id).journal()).hasSize(2);
        assertThatThrownBy(()->service.refund(id,APPROVER,"Again")).isInstanceOf(DomainException.class);
        assertThat(ledger.integrity().balanced()).isTrue();assertThat(ledger.integrity().accountBalancesMatch()).isTrue();
    }
    @Test void insufficientFundsRollBackJournalBalanceAndAudit() {
        UUID id=create("1000000.00");long a=balance(from),b=balance(to),j=journal.count(),events=audit.count();
        assertThatThrownBy(()->service.approve(id,APPROVER)).isInstanceOf(DomainException.class).hasMessageContaining("Insufficient");
        assertThat(balance(from)).isEqualTo(a);assertThat(balance(to)).isEqualTo(b);
        assertThat(journal.count()).isEqualTo(j);assertThat(audit.count()).isEqualTo(events);
        assertThat(service.detail(id).transfer().status()).isEqualTo("PENDING");
    }
    @Test void idempotencyReplaysTheSameRequestAndRejectsChangedPayload() {
        String key=UUID.randomUUID().toString();var first=service.create(request("12.00",key),MAKER);
        var replay=service.create(request("12.00",key),MAKER);
        assertThat(replay.replayed()).isTrue();assertThat(replay.transfer().id()).isEqualTo(first.transfer().id());
        assertThatThrownBy(()->service.create(request("13.00",key),MAKER)).hasMessageContaining("different request");
    }
    @Test void makerCannotApproveTheirOwnPayment() {
        UUID id=service.create(request("3.00",UUID.randomUUID().toString()),APPROVER).transfer().id();
        assertThatThrownBy(()->service.approve(id,APPROVER)).hasMessageContaining("different user");
        assertThat(service.detail(id).transfer().status()).isEqualTo("PENDING");
    }
    @Test void rejectedPaymentsNeverPost() {
        UUID id=create("4.00");long a=balance(from);service.reject(id,APPROVER,"Invoice not approved");
        assertThat(balance(from)).isEqualTo(a);assertThat(service.detail(id).journal()).isEmpty();
        assertThatThrownBy(()->service.approve(id,APPROVER)).isInstanceOf(DomainException.class);
    }
    @Test void concurrentApprovalCannotDoubleSpend() throws Exception {
        UUID id=create("7.00");long a=balance(from);CountDownLatch start=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)) {
            Callable<Boolean> approve=()->{start.await();try{service.approve(id,APPROVER);return true;}catch(DomainException expected){return false;}};
            Future<Boolean> x=pool.submit(approve),y=pool.submit(approve);start.countDown();
            assertThat(List.of(x.get(10,TimeUnit.SECONDS),y.get(10,TimeUnit.SECONDS))).containsExactlyInAnyOrder(true,false);
        }
        assertThat(balance(from)).isEqualTo(a-700);assertThat(service.detail(id).journal()).hasSize(1);
    }
    @Test void reconciliationClassifiesMatchesDuplicatesAndExceptions() {
        UUID id=create("18.20");service.approve(id,APPROVER);String ref=service.detail(id).transfer().reference();
        String csv="reference,amount,currency\n"+ref+",18.20,USD\n"+ref+",18.20,USD\nUNKNOWN,12.00,USD\n";
        var result=reconciliation.upload(file(csv),MAKER);
        assertThat(result.rows()).extracting(ApiModels.ReconciliationRow::status).containsExactly("MATCHED","DUPLICATE","UNKNOWN_REFERENCE");
        assertThat(result.summary().matchedRows()).isEqualTo(1);assertThat(result.summary().exceptions()).isEqualTo(2);
        var mismatch=reconciliation.upload(file("reference,amount,currency\n"+ref+",18.21,USD\n"),MAKER);
        assertThat(mismatch.rows().getFirst().status()).isEqualTo("AMOUNT_MISMATCH");
    }
    @Test void concurrentRequestsWithOneKeyCreateOnePayment() throws Exception {
        String key=UUID.randomUUID().toString();CountDownLatch start=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)) {
            Callable<UUID> create=()->{start.await();return service.create(request("6.00",key),MAKER).transfer().id();};
            Future<UUID> a=pool.submit(create),b=pool.submit(create);start.countDown();
            assertThat(a.get(10,TimeUnit.SECONDS)).isEqualTo(b.get(10,TimeUnit.SECONDS));
        }
    }
    @Test void competingPaymentsCannotOverdrawAnAccount() throws Exception {
        long before=balance(from);String amount=Money.decimal(before);
        UUID one=create(amount),two=create(amount);CountDownLatch start=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)) {
            Callable<UUID> first=()->{start.await();try{service.approve(one,APPROVER);return one;}catch(DomainException expected){return null;}};
            Callable<UUID> second=()->{start.await();try{service.approve(two,APPROVER);return two;}catch(DomainException expected){return null;}};
            Future<UUID> x=pool.submit(first),y=pool.submit(second);start.countDown();UUID a=x.get(10,TimeUnit.SECONDS),b=y.get(10,TimeUnit.SECONDS);
            assertThat((a==null) != (b==null)).isTrue();assertThat(balance(from)).isZero();
            service.refund(a==null?b:a,APPROVER,"Restore test balance");assertThat(balance(from)).isEqualTo(before);
        }
        assertThat(ledger.integrity().accountBalancesMatch()).isTrue();
    }
    @Test void malformedCsvIsAtomicAndMoneyNeverRoundsFractionalCents() {
        long count=imports.count();
        assertThatThrownBy(()->reconciliation.upload(file("reference,amount,currency\nUNKNOWN,12.00,USD\nBROKEN,nope,USD\n"),MAKER)).isInstanceOf(DomainException.class);
        assertThat(imports.count()).isEqualTo(count);
        assertThatThrownBy(()->Money.minor(new BigDecimal("1.001"))).isInstanceOf(DomainException.class);
        assertThat(Money.minor(new BigDecimal("0.10"))).isEqualTo(10);
        assertThat(reconciliation.upload(file("\uFEFFreference,amount,currency\nUNKNOWN,2.00,USD\n"),MAKER).rows()).hasSize(1);
    }
    @Test void authenticationCsrfAndRoleBoundariesAreEnforced() throws Exception {
        mvc.perform(get("/api/accounts")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me")).andExpect(status().isOk()).andExpect(jsonPath("$.authenticated").value(false));
        mvc.perform(post("/api/transfers").with(user(MAKER).roles("OPERATOR")).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
        String body="{\"fromAccountId\":\""+from+"\",\"toAccountId\":\""+to+"\",\"amount\":1,\"description\":\"Test\",\"idempotencyKey\":\"api-test-"+UUID.randomUUID()+"\"}";
        mvc.perform(post("/api/transfers").with(csrf()).with(user("viewer@ledgerflow.demo").roles("VIEWER"))
            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/transfers").with(csrf()).with(user(MAKER).roles("OPERATOR"))
            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mvc.perform(get("/api/audit").with(user(MAKER).roles("OPERATOR"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/audit").with(user(APPROVER).roles("ADMIN"))).andExpect(status().isOk());
    }
    @Test void loginUsesPasswordHashAndReturnsNoPasswordFields() throws Exception {
        assertThat(users.findByEmail(MAKER).orElseThrow().passwordHash).startsWith("$2a$");
        mvc.perform(post("/api/auth/login").with(csrf()).param("username",MAKER).param("password","wrong")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(csrf()).param("username",MAKER).param("password","DemoPass123!"))
            .andExpect(status().isOk()).andExpect(content().json("{\"message\":\"Signed in\"}"));
        mvc.perform(get("/api/auth/me").with(user(MAKER).roles("OPERATOR"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("OPERATOR")).andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
    @Test void browserStyleCsrfCookieAndSessionLoginWorkTogether() throws Exception {
        var csrfResponse=mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn().getResponse();
        Cookie token=Objects.requireNonNull(csrfResponse.getCookie("XSRF-TOKEN"));
        var login=mvc.perform(post("/api/auth/login").cookie(token).header("X-XSRF-TOKEN",token.getValue())
            .param("username",MAKER).param("password","DemoPass123!")).andExpect(status().isOk()).andReturn();
        MockHttpSession session=(MockHttpSession)login.getRequest().getSession(false);
        assertThat(session).isNotNull();
        var fresh=mvc.perform(get("/api/auth/csrf").session(session)).andExpect(status().isOk()).andReturn().getResponse();
        Cookie freshToken=Objects.requireNonNull(fresh.getCookie("XSRF-TOKEN"));
        mvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.email").value(MAKER));
        mvc.perform(post("/api/auth/logout").session(session).cookie(freshToken).header("X-XSRF-TOKEN",freshToken.getValue())).andExpect(status().isOk());
        assertThat(session.isInvalid()).isTrue();
    }
    static MockMultipartFile file(String csv){return new MockMultipartFile("file","bank.csv","text/csv",csv.getBytes(StandardCharsets.UTF_8));}
}
