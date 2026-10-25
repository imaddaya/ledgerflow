package com.ledgerflow;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

interface UserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select u from AppUser u where u.id = :id")
    Optional<AppUser> lockById(@Param("id") UUID id);
}
interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByTypeOrderByName(AccountType type);
    Optional<Account> findByCode(String code);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from Account a where a.id = :id")
    Optional<Account> lockById(@Param("id") UUID id);
}
interface TransferRepository extends JpaRepository<Transfer, UUID> {
    Optional<Transfer> findByCreatedByIdAndIdempotencyKey(UUID actor, String key);
    Optional<Transfer> findByReference(String reference);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from Transfer t where t.id = :id")
    Optional<Transfer> lockById(@Param("id") UUID id);
    @EntityGraph(attributePaths = {"fromAccount", "toAccount", "createdBy", "approvedBy"})
    @Query("select t from Transfer t where (:status is null or t.status = :status) and (:q = '' or lower(t.reference) like lower(concat('%', :q, '%')) or lower(t.description) like lower(concat('%', :q, '%')))")
    Page<Transfer> search(@Param("status") TransferStatus status, @Param("q") String q, Pageable pageable);
    long countByStatus(TransferStatus status);
    @EntityGraph(attributePaths = {"fromAccount", "toAccount", "createdBy", "approvedBy"})
    List<Transfer> findTop6ByOrderByCreatedAtDesc();
    @Query("select coalesce(sum(t.amountMinor), 0) from Transfer t where t.status = :status")
    long volume(@Param("status") TransferStatus status);
}
interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    @EntityGraph(attributePaths = {"transfer", "postedBy"})
    Page<JournalEntry> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<JournalEntry> findByTransferIdOrderByCreatedAt(UUID transferId);
}
interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {
    @EntityGraph(attributePaths = {"account"})
    List<JournalLine> findByEntryIdIn(Collection<UUID> entryIds);
    @Query("select l.account.id as accountId, sum(case when l.direction = l.account.normalBalance then l.amountMinor else -l.amountMinor end) as balanceMinor from JournalLine l group by l.account.id")
    List<AccountLedgerBalance> accountBalances();
    @Query("select coalesce(sum(case when l.direction = :debit then l.amountMinor else 0 end),0) as debitMinor, coalesce(sum(case when l.direction = :credit then l.amountMinor else 0 end),0) as creditMinor from JournalLine l")
    LedgerTotals totals(@Param("debit") Direction debit, @Param("credit") Direction credit);
}
interface AccountLedgerBalance { UUID getAccountId(); Long getBalanceMinor(); }
interface LedgerTotals { Long getDebitMinor(); Long getCreditMinor(); }
interface AuditRepository extends JpaRepository<AuditEvent, UUID> {
    Page<AuditEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
interface StatementImportRepository extends JpaRepository<StatementImport, UUID> {
    Page<StatementImport> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
interface StatementRowRepository extends JpaRepository<StatementRow, UUID> {
    List<StatementRow> findByStatementImportIdOrderByRowNumber(UUID importId);
    long countByStatusNot(ReconciliationStatus status);
}
