CREATE TABLE currencies (
    code CHAR(3) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(5) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_currencies_code_upper CHECK (code = UPPER(code))
);

CREATE TABLE exchange_rates (
    id BIGSERIAL PRIMARY KEY,
    from_currency CHAR(3) NOT NULL REFERENCES currencies(code),
    to_currency CHAR(3) NOT NULL REFERENCES currencies(code),
    rate NUMERIC(18, 8) NOT NULL,
    quoted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    source VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_exchange_rates_positive CHECK (rate > 0)
);

CREATE TABLE receivable_types (
    code VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    pricing_rule_code VARCHAR(50) NOT NULL,
    base_spread NUMERIC(8, 4) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_receivable_types_base_spread CHECK (base_spread >= 0)
);

CREATE TABLE assignors (
    id BIGSERIAL PRIMARY KEY,
    document_number VARCHAR(14) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    risk_rating VARCHAR(5) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE credit_batches (
    id BIGSERIAL PRIMARY KEY,
    batch_reference VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_credit_batches_status CHECK (status IN ('RECEIVED', 'PROCESSING', 'PROCESSED', 'REJECTED'))
);

CREATE TABLE credit_assignments (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL REFERENCES credit_batches(id),
    assignor_id BIGINT NOT NULL REFERENCES assignors(id),
    receivable_type_code VARCHAR(30) NOT NULL REFERENCES receivable_types(code),
    operation_reference VARCHAR(50) NOT NULL UNIQUE,
    face_currency CHAR(3) NOT NULL REFERENCES currencies(code),
    face_amount NUMERIC(18, 2) NOT NULL,
    due_date DATE NOT NULL,
    base_tax_rate NUMERIC(8, 4) NOT NULL,
    applied_spread NUMERIC(8, 4) NOT NULL,
    term_days INT NOT NULL,
    payment_currency CHAR(3) NOT NULL REFERENCES currencies(code),
    exchange_rate NUMERIC(18, 8) NOT NULL DEFAULT 1,
    net_amount NUMERIC(18, 2) NOT NULL,
    pricing_at TIMESTAMP WITH TIME ZONE NOT NULL,
    liquidated_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_credit_assignments_positive CHECK (
        face_amount > 0
            AND base_tax_rate >= 0
            AND applied_spread >= 0
            AND term_days >= 0
            AND exchange_rate > 0
            AND net_amount >= 0
    ),
    CONSTRAINT ck_credit_assignments_status CHECK (status IN ('PENDING', 'PRICED', 'LIQUIDATED', 'REJECTED'))
);

CREATE INDEX idx_exchange_rates_latest
    ON exchange_rates (from_currency, to_currency, quoted_at DESC);

CREATE INDEX idx_credit_assignments_report
    ON credit_assignments (created_at, assignor_id, payment_currency);

CREATE INDEX idx_credit_assignments_batch
    ON credit_assignments (batch_id, status);

CREATE INDEX idx_credit_assignments_liquidated_at
    ON credit_assignments (liquidated_at);
