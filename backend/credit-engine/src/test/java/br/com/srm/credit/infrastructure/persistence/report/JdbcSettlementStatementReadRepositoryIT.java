package br.com.srm.credit.infrastructure.persistence.report;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.srm.credit.application.settlement.SettlementStatementFilter;
import br.com.srm.credit.application.settlement.SettlementStatementReadRepository;
import br.com.srm.credit.bootstrap.CreditEngineApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
        classes = CreditEngineApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "credit.exchange-rate.redis.refresh-enabled=false")
class JdbcSettlementStatementReadRepositoryIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SettlementStatementReadRepository repository;

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("credit_assignment_db")
            .withUsername("platform_user")
            .withPassword("platform_password");

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute(
                "TRUNCATE TABLE credit_assignments, credit_batches, assignors, receivable_types, currencies RESTART IDENTITY CASCADE");
        seedDatabase();
    }

    @Test
    void shouldReturnOnlyLiquidatedAssignmentsMatchingFilters() {
        var filter = new SettlementStatementFilter(
                java.time.LocalDate.parse("2026-06-20"),
                java.time.LocalDate.parse("2026-06-21"),
                "12345678000199",
                "BRL",
                0,
                10);

        var page = repository.search(filter);

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).operationReference()).isEqualTo("OP-001");
        assertThat(page.items().get(0).assignorDocumentNumber()).isEqualTo("12345678000199");
    }

    private void seedDatabase() {
        jdbcTemplate.update("INSERT INTO currencies(code, name, symbol) VALUES ('BRL', 'Real', 'R$')");
        jdbcTemplate.update("INSERT INTO currencies(code, name, symbol) VALUES ('USD', 'Dollar', '$')");
        jdbcTemplate.update(
                "INSERT INTO receivable_types(code, name, pricing_rule_code, base_spread, is_active) VALUES ('TRADE_RECEIVABLE', 'Duplicata Mercantil', 'TRADE_RECEIVABLE', 0.0150, true)");
        jdbcTemplate.update(
                "INSERT INTO assignors(document_number, name, risk_rating) VALUES ('12345678000199', 'Cedente ABC Ltda', 'AA')");
        jdbcTemplate.update(
                "INSERT INTO assignors(document_number, name, risk_rating) VALUES ('99887766000155', 'Cedente XYZ Ltda', 'A')");
        jdbcTemplate.update("INSERT INTO credit_batches(batch_reference, status) VALUES ('BATCH-001', 'PROCESSED')");
        jdbcTemplate.update("INSERT INTO credit_batches(batch_reference, status) VALUES ('BATCH-002', 'PROCESSED')");

        var batch1Id = jdbcTemplate.queryForObject(
                "SELECT id FROM credit_batches WHERE batch_reference = 'BATCH-001'", Long.class);
        var batch2Id = jdbcTemplate.queryForObject(
                "SELECT id FROM credit_batches WHERE batch_reference = 'BATCH-002'", Long.class);
        var assignor1Id = jdbcTemplate.queryForObject(
                "SELECT id FROM assignors WHERE document_number = '12345678000199'", Long.class);
        var assignor2Id = jdbcTemplate.queryForObject(
                "SELECT id FROM assignors WHERE document_number = '99887766000155'", Long.class);

        jdbcTemplate.update(
                """
                INSERT INTO credit_assignments(
                    batch_id, assignor_id, receivable_type_code, operation_reference, face_currency,
                    face_amount, due_date, base_tax_rate, applied_spread, term_days, payment_currency,
                    exchange_rate, net_amount, pricing_at, liquidated_at, status
                ) VALUES (?, ?, 'TRADE_RECEIVABLE', 'OP-001', 'BRL', 1000.00, DATE '2026-07-20',
                         0.0200, 0.0150, 30, 'BRL', 1.00000000, 966.18,
                         TIMESTAMPTZ '2026-06-20 10:00:00-03:00', TIMESTAMPTZ '2026-06-20 12:00:00-03:00', 'LIQUIDATED')
                """,
                batch1Id,
                assignor1Id);

        jdbcTemplate.update(
                """
                INSERT INTO credit_assignments(
                    batch_id, assignor_id, receivable_type_code, operation_reference, face_currency,
                    face_amount, due_date, base_tax_rate, applied_spread, term_days, payment_currency,
                    exchange_rate, net_amount, pricing_at, liquidated_at, status
                ) VALUES (?, ?, 'TRADE_RECEIVABLE', 'OP-002', 'USD', 2000.00, DATE '2026-07-25',
                         0.0200, 0.0150, 45, 'USD', 1.00000000, 1920.00,
                         TIMESTAMPTZ '2026-06-21 10:00:00-03:00', TIMESTAMPTZ '2026-06-21 12:00:00-03:00', 'LIQUIDATED')
                """,
                batch2Id,
                assignor2Id);
    }
}
