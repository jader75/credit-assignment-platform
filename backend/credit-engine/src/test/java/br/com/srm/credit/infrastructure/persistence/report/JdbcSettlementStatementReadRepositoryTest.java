package br.com.srm.credit.infrastructure.persistence.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.srm.credit.application.settlement.SettlementStatementFilter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class JdbcSettlementStatementReadRepositoryTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private JdbcSettlementStatementReadRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        repository = new JdbcSettlementStatementReadRepository(jdbcTemplate);
    }

    @Test
    void shouldBuildQueryWithAllFiltersAndMapResultRows() throws Exception {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<?> rowMapper = invocation.getArgument(2);
                    var resultSet = mock(ResultSet.class);
                    when(resultSet.getString("operation_reference")).thenReturn("OP-001");
                    when(resultSet.getString("batch_reference")).thenReturn("BATCH-001");
                    when(resultSet.getString("document_number")).thenReturn("12345678000199");
                    when(resultSet.getString("name")).thenReturn("Cedente ABC Ltda");
                    when(resultSet.getString("payment_currency")).thenReturn("BRL");
                    when(resultSet.getBigDecimal("face_amount")).thenReturn(new BigDecimal("1000.00"));
                    when(resultSet.getBigDecimal("net_amount")).thenReturn(new BigDecimal("966.18"));
                    when(resultSet.getObject("liquidated_at", OffsetDateTime.class))
                            .thenReturn(OffsetDateTime.parse("2026-06-20T12:00:00-03:00"));
                    when(resultSet.getString("status")).thenReturn("LIQUIDATED");
                    return List.of(rowMapper.mapRow(resultSet, 0));
                });
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(Class.class)))
                .thenReturn(1L);

        var filter = new SettlementStatementFilter(
                LocalDate.parse("2026-06-20"), LocalDate.parse("2026-06-21"), "12345678000199", "BRL", 1, 10);

        var page = repository.search(filter);

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).operationReference()).isEqualTo("OP-001");
        assertThat(page.items().get(0).assignorDocumentNumber()).isEqualTo("12345678000199");
    }

    @Test
    void shouldBuildQueryWithoutOptionalFiltersAndHandleNullCount() {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(Class.class)))
                .thenReturn(null);

        var filter = new SettlementStatementFilter(null, null, null, null, 0, 20);

        var page = repository.search(filter);

        assertThat(page.totalElements()).isZero();
        assertThat(page.totalPages()).isZero();
        assertThat(page.items()).isEmpty();
    }

    @Test
    void shouldTreatBlankFiltersAsAbsentAndComputePagingForZeroSizeBranch() {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(Class.class)))
                .thenReturn(3L);

        var filter = new SettlementStatementFilter(null, null, "   ", "", 0, 0);

        var page = repository.search(filter);

        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isZero();
    }
}
