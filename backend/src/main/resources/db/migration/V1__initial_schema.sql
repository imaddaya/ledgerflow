CREATE TABLE app_users (
    id UUID PRIMARY KEY, email VARCHAR(200) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL, password_hash VARCHAR(100) NOT NULL,
    user_role VARCHAR(20) NOT NULL, version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE accounts (
    id UUID PRIMARY KEY, code VARCHAR(20) NOT NULL UNIQUE, name VARCHAR(100) NOT NULL,
    account_type VARCHAR(20) NOT NULL, normal_balance VARCHAR(6) NOT NULL,
    currency VARCHAR(3) NOT NULL, balance_minor BIGINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT nonnegative_account_balance CHECK (balance_minor >= 0)
);
CREATE TABLE transfers (
    id UUID PRIMARY KEY, reference VARCHAR(40) NOT NULL UNIQUE,
    from_account_id UUID NOT NULL REFERENCES accounts(id), to_account_id UUID NOT NULL REFERENCES accounts(id),
    amount_minor BIGINT NOT NULL, currency VARCHAR(3) NOT NULL,
    transfer_status VARCHAR(24) NOT NULL, description VARCHAR(140) NOT NULL,
    created_by UUID NOT NULL REFERENCES app_users(id), approved_by UUID REFERENCES app_users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    posted_at TIMESTAMP WITH TIME ZONE, idempotency_key VARCHAR(80) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT positive_transfer_amount CHECK (amount_minor > 0),
    CONSTRAINT different_transfer_accounts CHECK (from_account_id <> to_account_id),
    CONSTRAINT unique_actor_idempotency UNIQUE (created_by, idempotency_key)
);
CREATE INDEX transfers_status_created_idx ON transfers(transfer_status, created_at);
CREATE TABLE journal_entries (
    id UUID PRIMARY KEY, transfer_id UUID REFERENCES transfers(id), entry_type VARCHAR(20) NOT NULL,
    description VARCHAR(200) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    posted_by UUID REFERENCES app_users(id)
);
CREATE TABLE journal_lines (
    id UUID PRIMARY KEY, entry_id UUID NOT NULL REFERENCES journal_entries(id),
    account_id UUID NOT NULL REFERENCES accounts(id), direction VARCHAR(6) NOT NULL,
    amount_minor BIGINT NOT NULL, CONSTRAINT positive_line_amount CHECK (amount_minor > 0)
);
CREATE INDEX journal_lines_entry_idx ON journal_lines(entry_id);
CREATE INDEX journal_lines_account_idx ON journal_lines(account_id);
CREATE TABLE audit_events (
    id UUID PRIMARY KEY, actor_email VARCHAR(200) NOT NULL, action VARCHAR(40) NOT NULL,
    entity_id UUID, detail VARCHAR(1000) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX audit_events_created_idx ON audit_events(created_at);
CREATE TABLE statement_imports (
    id UUID PRIMARY KEY, file_name VARCHAR(120) NOT NULL,
    uploaded_by VARCHAR(200) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE statement_rows (
    id UUID PRIMARY KEY, import_id UUID NOT NULL REFERENCES statement_imports(id),
    row_number INTEGER NOT NULL, reference VARCHAR(80) NOT NULL,
    amount_minor BIGINT NOT NULL, currency VARCHAR(3) NOT NULL,
    row_status VARCHAR(30) NOT NULL, note VARCHAR(300) NOT NULL,
    transfer_id UUID REFERENCES transfers(id)
);
CREATE INDEX statement_rows_import_idx ON statement_rows(import_id);
