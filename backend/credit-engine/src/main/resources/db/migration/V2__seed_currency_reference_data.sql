INSERT INTO currencies (code, name, symbol) VALUES
    ('BRL', 'Real brasileiro', 'R$'),
    ('USD', 'Dollar', '$')
ON CONFLICT (code) DO NOTHING;

INSERT INTO exchange_rates (from_currency, to_currency, rate, quoted_at, source) VALUES
    ('BRL', 'USD', 5.20000000, CURRENT_TIMESTAMP, 'MANUAL'),
    ('USD', 'BRL', 0.19230769, CURRENT_TIMESTAMP, 'MANUAL')
ON CONFLICT DO NOTHING;
