package com.ledgerflow;

import static com.ledgerflow.ApiModels.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.apache.commons.csv.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReconciliationService {
    private final StatementImportRepository imports;
    private final StatementRowRepository rows;
    private final TransferRepository transfers;
    private final AuditRepository audit;
    ReconciliationService(StatementImportRepository imports, StatementRowRepository rows, TransferRepository transfers, AuditRepository audit) {
        this.imports = imports; this.rows = rows; this.transfers = transfers; this.audit = audit;
    }
    @Transactional
    public ImportDetail upload(MultipartFile file, String email) {
        if (file.isEmpty() || file.getSize() > 1_048_576) throw new DomainException(400, "Choose a nonempty CSV file smaller than 1 MB");
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("statement.csv").replace('\\', '/');
        String name = original.substring(original.lastIndexOf('/') + 1).replaceAll("[\\r\\n]", "");
        if (!name.toLowerCase().endsWith(".csv")) throw new DomainException(400, "The statement must be a .csv file");
        StatementImport batch = new StatementImport(); batch.fileName = name.substring(0, Math.min(name.length(), 120)); batch.uploadedBy = email;
        List<StatementRow> parsed = new ArrayList<>(); Set<String> seen = new HashSet<>();
        try (Reader reader = new StringReader(new String(file.getBytes(), StandardCharsets.UTF_8).replaceFirst("^\\uFEFF", ""));
             CSVParser csv = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).get().parse(reader)) {
            if (!csv.getHeaderNames().equals(List.of("reference", "amount", "currency")))
                throw new DomainException(400, "CSV headers must be exactly: reference,amount,currency");
            for (CSVRecord record : csv) {
                if (parsed.size() == 500) throw new DomainException(400, "A statement can contain at most 500 rows");
                if (record.size() != 3) throw new DomainException(400, "Each CSV row must have exactly three columns");
                StatementRow row = new StatementRow(); row.statementImport = batch; row.rowNumber = parsed.size() + 2;
                row.reference = record.get("reference"); row.currency = record.get("currency").toUpperCase(Locale.ROOT);
                if (row.reference.isBlank() || row.reference.length() > 80 || !row.currency.matches("[A-Z]{3}"))
                    throw new DomainException(400, "Invalid reference or currency at row " + row.rowNumber);
                try { row.amountMinor = Money.minor(new BigDecimal(record.get("amount"))); }
                catch (RuntimeException e) { throw new DomainException(400, "Invalid amount at row " + row.rowNumber + "; use a positive decimal with at most two decimal places"); }
                row.transfer = transfers.findByReference(row.reference).orElse(null);
                if (!seen.add(row.reference)) status(row, ReconciliationStatus.DUPLICATE, "Repeated reference in this file");
                else if (row.transfer == null) status(row, ReconciliationStatus.UNKNOWN_REFERENCE, "No matching internal transfer");
                else if (row.transfer.status != TransferStatus.POSTED) status(row, ReconciliationStatus.NOT_POSTED, "Internal transfer is " + row.transfer.status);
                else if (!row.currency.equals(row.transfer.currency)) status(row, ReconciliationStatus.CURRENCY_MISMATCH, "Expected " + row.transfer.currency);
                else if (row.amountMinor != row.transfer.amountMinor) status(row, ReconciliationStatus.AMOUNT_MISMATCH, "Expected " + Money.decimal(row.transfer.amountMinor));
                else status(row, ReconciliationStatus.MATCHED, "Reference, amount and currency agree");
                parsed.add(row);
            }
        } catch (IOException | IllegalArgumentException e) { throw new DomainException(400, "Cannot parse the CSV; check its UTF-8 encoding and column structure"); }
        if (parsed.isEmpty()) throw new DomainException(400, "The CSV has no statement rows");
        imports.save(batch); rows.saveAll(parsed);
        audit.save(new AuditEvent(email, "STATEMENT_IMPORTED", batch.id, parsed.size() + " statement rows"));
        return detail(batch, parsed);
    }
    private void status(StatementRow row, ReconciliationStatus status, String note) { row.status = status; row.note = note; }
    private ImportView summary(StatementImport batch, List<StatementRow> list) {
        long matched = list.stream().filter(r -> r.status == ReconciliationStatus.MATCHED).count();
        return new ImportView(batch.id, batch.fileName, batch.uploadedBy, batch.createdAt, list.size(), matched, list.size() - matched);
    }
    private ImportDetail detail(StatementImport batch, List<StatementRow> list) {
        return new ImportDetail(summary(batch, list), list.stream().map(r -> new ReconciliationRow(r.rowNumber,
            r.reference, r.amountMinor, r.currency, r.status.name(), r.note)).toList());
    }
    @Transactional(readOnly = true)
    public ImportDetail detail(UUID id) {
        StatementImport batch = imports.findById(id).orElseThrow(() -> DomainException.missing("Import not found"));
        return detail(batch, rows.findByStatementImportIdOrderByRowNumber(id));
    }
    @Transactional(readOnly = true)
    public PageView<ImportView> list(Pageable pageable) {
        return PageView.from(imports.findAllByOrderByCreatedAtDesc(pageable)
            .map(b -> summary(b, rows.findByStatementImportIdOrderByRowNumber(b.id))));
    }
}
