package com.ledgerflow;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Page;

public final class ApiModels {
    private ApiModels() {}
    public record CreateTransfer(
        @NotNull UUID fromAccountId, @NotNull UUID toAccountId,
        @NotNull @DecimalMin("0.01") @DecimalMax("1000000.00") @Digits(integer = 7, fraction = 2) BigDecimal amount,
        @NotBlank @Size(max = 140) String description,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{8,80}") String idempotencyKey) {}
    public record Reason(@NotBlank @Size(max = 140) String reason) {}
    public record CreateAccount(@NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_-]{2,15}") String code, @NotBlank @Size(max = 80) String name) {}
    public record UserView(boolean authenticated, UUID id, String email, String displayName, String role) {}
    public record AccountView(UUID id, String code, String name, String currency, long balanceMinor, String type) {}
    public record TransferView(UUID id, String reference, UUID fromAccountId, String fromAccount, UUID toAccountId, String toAccount,
        long amountMinor, String currency, String status, String description, String createdBy, String approvedBy,
        Instant createdAt, Instant updatedAt, Instant postedAt) {}
    public record CreatedTransfer(TransferView transfer, boolean replayed) {}
    public record JournalLineView(String accountCode, String accountName, String direction, long amountMinor) {}
    public record JournalView(UUID id, String reference, String type, String description, Instant createdAt, String postedBy, List<JournalLineView> lines) {}
    public record TransferDetail(TransferView transfer, List<JournalView> journal) {}
    public record IntegrityView(long debitMinor, long creditMinor, boolean balanced, boolean accountBalancesMatch) {}
    public record DailyVolume(String day, long amountMinor) {}
    public record DashboardView(long totalBalanceMinor, long postedVolumeMinor, long pendingCount, long exceptionCount,
        long postedCount, IntegrityView ledger, List<DailyVolume> dailyVolume, List<TransferView> recentTransfers) {}
    public record AuditView(UUID id, String actorEmail, String action, UUID entityId, String detail, Instant createdAt) {}
    public record ReconciliationRow(int rowNumber, String reference, long amountMinor, String currency, String status, String note) {}
    public record ImportView(UUID id, String fileName, String uploadedBy, Instant createdAt, long totalRows, long matchedRows, long exceptions) {}
    public record ImportDetail(ImportView summary, List<ReconciliationRow> rows) {}
    public record PageView<T>(List<T> content, long totalElements, int totalPages, int page, int size) {
        static <T> PageView<T> from(Page<T> page) { return new PageView<>(page.getContent(), page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize()); }
    }
}
