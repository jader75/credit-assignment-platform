-- Tabela mestre para registro de moedas homologadas pela plataforma
CREATE TABLE IF NOT EXISTS currencies (
                                          code VARCHAR(3) PRIMARY KEY, -- Ex: 'BRL', 'USD'
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(5) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- Tabela de histórico cambial para auditoria de cotações cross-currency
CREATE TABLE IF NOT EXISTS currency_rates (
                                              id BIGSERIAL PRIMARY KEY,
                                              from_currency VARCHAR(3) NOT NULL REFERENCES currencies(code),
    to_currency VARCHAR(3) NOT NULL REFERENCES currencies(code),
    rate NUMERIC(14, 6) NOT NULL, -- Precisao ate a 6ª casa decimal (Padrao Spot)
    rate_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- Cadastro dos tipos de ativos e suas respectivas politicas base de spreads de risco
CREATE TABLE IF NOT EXISTS product_types (
                                             code VARCHAR(30) PRIMARY KEY, -- Ex: 'DUPLICATA_MERCANTIL', 'CHEQUE_PRE'
    name VARCHAR(100) NOT NULL,
    base_spread NUMERIC(6, 4) NOT NULL, -- Armazenamento em Basis Points (Ex: 0.0150 para 1.5%)
    is_active BOOLEAN DEFAULT TRUE
    );

-- Cadastro de cedentes detentores de direitos creditorios
CREATE TABLE IF NOT EXISTS assignors (
                                         id BIGSERIAL PRIMARY KEY,
                                         document_number VARCHAR(14) NOT NULL UNIQUE, -- CPF ou CNPJ sem formatacao
    name VARCHAR(150) NOT NULL,
    risk_rating VARCHAR(5) NOT NULL, -- Ex: 'AAA', 'AA', 'A'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- Registro centralizador de operacoes de cessao e liquidacao transacional
CREATE TABLE IF NOT EXISTS credit_assignments (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  assignor_id BIGINT NOT NULL REFERENCES assignors(id),
    product_type_code VARCHAR(30) NOT NULL REFERENCES product_types(code),
    transaction_id VARCHAR(50) NOT NULL UNIQUE, -- CHAVE DE IDEMPOTÊNCIA: Bloqueia duplicidade via UUID do Front
    face_currency VARCHAR(3) NOT NULL REFERENCES currencies(code),
    face_amount NUMERIC(18, 2) NOT NULL, -- Valor nominal do titulo
    due_date DATE NOT NULL, -- Data de vencimento do direito creditorio
    base_tax_rate NUMERIC(6, 4) NOT NULL, -- Taxa base flutuante do mercado (Ex: CDI/Selic)
    applied_spread NUMERIC(6, 4) NOT NULL, -- Juros de risco calculados via Strategy Pattern
    term_days INT NOT NULL, -- Prazo corrido em dias ate o vencimento
    payment_currency VARCHAR(3) NOT NULL REFERENCES currencies(code),
    exchange_rate NUMERIC(14, 6) DEFAULT 1.000000, -- Taxa de conversao cambial aplicada no fechamento
    net_amount NUMERIC(18, 2) NOT NULL, -- Valor presente / liquido final calculado a pagar
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, LIQUIDATED, REJECTED
    version BIGINT NOT NULL DEFAULT 0, -- CONCORRÊNCIA: Habilita Optimistic Locking nativo no Hibernate
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    liquidated_at TIMESTAMP WITH TIME ZONE
                                );

-- INDICES ANALÍTICOS: Otimizacao estrita para a rota de relatorios de duas camadas (Padronece CQRS)
CREATE INDEX IF NOT EXISTS idx_assignments_report ON credit_assignments (created_at, assignor_id, payment_currency);
CREATE INDEX IF NOT EXISTS idx_currency_rates_latest ON currency_rates (from_currency, to_currency, rate_date DESC);
