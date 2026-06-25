CREATE TABLE currencies (
    code VARCHAR(3) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(5) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE currency_rates (
    id BIGSERIAL PRIMARY KEY,
    from_currency VARCHAR(3) NOT NULL REFERENCES currencies(code),
    to_currency VARCHAR(3) NOT NULL REFERENCES currencies(code),
    rate NUMERIC(14, 6) NOT NULL,
    rate_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_types (
    code VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    base_spread NUMERIC(6, 4) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE assignors (
    id BIGSERIAL PRIMARY KEY,
    document_number VARCHAR(14) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    risk_rating VARCHAR(5) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE credit_assignments (
    id BIGSERIAL PRIMARY KEY,
    assignor_id BIGINT NOT NULL REFERENCES assignors(id),
    product_type_code VARCHAR(30) NOT NULL REFERENCES product_types(code),
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    face_currency VARCHAR(3) NOT NULL REFERENCES currencies(code),
    face_amount NUMERIC(18, 2) NOT NULL,
    due_date DATE NOT NULL,
    base_tax_rate NUMERIC(6, 4) NOT NULL,
    applied_spread NUMERIC(6, 4) NOT NULL,
    term_days INT NOT NULL,
    payment_currency VARCHAR(3) NOT NULL REFERENCES currencies(code),
    exchange_rate NUMERIC(14, 6) DEFAULT 1.000000,
    net_amount NUMERIC(18, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    liquidated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_assignments_report ON credit_assignments (created_at, assignor_id, payment_currency);
CREATE INDEX idx_currency_rates_latest ON currency_rates (from_currency, to_currency, rate_date DESC);
