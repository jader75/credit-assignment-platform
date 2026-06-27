package br.com.srm.credit.infrastructure.persistence.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.srm.credit.application.settlement.SettlementOperationFilter;
import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class JdbcSettlementOperationReadRepositoryTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private JdbcSettlementOperationReadRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        repository = new JdbcSettlementOperationReadRepository(jdbcTemplate);
    }

    @Test
    void shouldBuildQueryWithCombinedFiltersWithoutCorruptingNamedParameters() throws Exception {
        var capturedSql = new AtomicReference<String>();

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    capturedSql.set(invocation.getArgument(0));
                    @SuppressWarnings("unchecked")
                    RowMapper<?> rowMapper = invocation.getArgument(2);
                    var resultSet = mock(ResultSet.class);
                    when(resultSet.getString("operation_reference")).thenReturn("OP-001");
                    when(resultSet.getString("batch_reference")).thenReturn("BATCH-002");
                    when(resultSet.getString("document_number")).thenReturn("12345678000199");
                    when(resultSet.getString("name")).thenReturn("Cedente ABC Ltda");
                    when(resultSet.getString("receivable_type_code")).thenReturn("TRADE_RECEIVABLE");
                    when(resultSet.getString("face_currency")).thenReturn("BRL");
                    when(resultSet.getString("payment_currency")).thenReturn("BRL");
                    when(resultSet.getBigDecimal("face_amount")).thenReturn(new BigDecimal("1000.00"));
                    when(resultSet.getBigDecimal("net_amount")).thenReturn(new BigDecimal("966.18"));
                    when(resultSet.getObject("due_date", LocalDate.class)).thenReturn(LocalDate.parse("2026-07-20"));
                    when(resultSet.getObject("pricing_at", OffsetDateTime.class))
                            .thenReturn(OffsetDateTime.parse("2026-06-20T10:00:00-03:00"));
                    when(resultSet.getObject("liquidated_at", OffsetDateTime.class))
                            .thenReturn(OffsetDateTime.parse("2026-06-20T12:00:00-03:00"));
                    when(resultSet.getString("status")).thenReturn(CreditAssignmentStatus.LIQUIDATED.name());
                    return List.of(rowMapper.mapRow(resultSet, 0));
                });
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(Class.class)))
                .thenReturn(1L);

        var page = repository.search(new SettlementOperationFilter(
                List.of(CreditAssignmentStatus.LIQUIDATED), "BATCH-002", "12345678000199", 0, 20));

        assertThat(capturedSql.get()).contains("cb.batch_reference = :batchReference");
        assertThat(capturedSql.get()).contains("ORDER BY ca.pricing_at DESC, ca.id DESC");
        assertThat(capturedSql.get()).doesNotContain("batchReferenceORDER");
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).batchReference()).isEqualTo("BATCH-002");
        assertThat(page.items().get(0).status()).isEqualTo(CreditAssignmentStatus.LIQUIDATED);
    }
}
