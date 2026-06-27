package br.com.srm.credit.infrastructure.persistence.report;

import br.com.srm.credit.application.settlement.SettlementOperationFilter;
import br.com.srm.credit.application.settlement.SettlementOperationItem;
import br.com.srm.credit.application.settlement.SettlementOperationPage;
import br.com.srm.credit.application.settlement.SettlementOperationReadRepository;
import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class JdbcSettlementOperationReadRepository implements SettlementOperationReadRepository {

    private static final String BASE_FROM =
            """
            FROM credit_assignments ca
            JOIN credit_batches cb ON cb.id = ca.batch_id
            JOIN assignors a ON a.id = ca.assignor_id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcSettlementOperationReadRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SettlementOperationPage search(SettlementOperationFilter filter) {
        var params = new MapSqlParameterSource();
        var where = new StringBuilder(" WHERE 1 = 1 ");

        if (filter.statuses() != null && !filter.statuses().isEmpty()) {
            where.append(" AND ca.status IN (:statuses)");
            params.addValue(
                    "statuses",
                    filter.statuses().stream().map(CreditAssignmentStatus::name).toList());
        }
        if (filter.batchReference() != null && !filter.batchReference().isBlank()) {
            where.append(" AND cb.batch_reference = :batchReference");
            params.addValue("batchReference", filter.batchReference().trim().toUpperCase());
        }
        if (filter.assignorDocumentNumber() != null
                && !filter.assignorDocumentNumber().isBlank()) {
            where.append(" AND a.document_number = :assignorDocumentNumber");
            params.addValue(
                    "assignorDocumentNumber", filter.assignorDocumentNumber().trim());
        }

        var listSql = new StringBuilder(
                        """
                        SELECT
                            ca.operation_reference,
                            cb.batch_reference,
                            a.document_number,
                            a.name,
                            ca.receivable_type_code,
                            ca.face_currency,
                            ca.payment_currency,
                            ca.face_amount,
                            ca.net_amount,
                            ca.due_date,
                            ca.pricing_at,
                            ca.liquidated_at,
                            ca.status
                """)
                .append(BASE_FROM)
                .append(where)
                .append(System.lineSeparator())
                .append("ORDER BY ca.pricing_at DESC, ca.id DESC")
                .append(System.lineSeparator())
                .append("LIMIT :limit OFFSET :offset")
                .toString();
        var listParams = new MapSqlParameterSource(params.getValues());
        listParams.addValue("limit", filter.size());
        listParams.addValue("offset", filter.page() * filter.size());

        var countSql = new StringBuilder("SELECT COUNT(*) ")
                .append(BASE_FROM)
                .append(where)
                .toString();
        var countParams = new MapSqlParameterSource(params.getValues());

        List<SettlementOperationItem> items = jdbcTemplate.query(listSql, listParams, rowMapper());
        Long totalElements = jdbcTemplate.queryForObject(countSql, countParams, Long.class);
        long safeTotalElements = totalElements == null ? 0L : totalElements;
        long totalPages = filter.size() == 0 ? 0L : (long) Math.ceil((double) safeTotalElements / filter.size());

        return new SettlementOperationPage(items, filter.page(), filter.size(), safeTotalElements, totalPages);
    }

    private static RowMapper<SettlementOperationItem> rowMapper() {
        return (rs, rowNum) -> new SettlementOperationItem(
                rs.getString("operation_reference"),
                rs.getString("batch_reference"),
                rs.getString("document_number"),
                rs.getString("name"),
                rs.getString("receivable_type_code"),
                rs.getString("face_currency"),
                rs.getString("payment_currency"),
                rs.getBigDecimal("face_amount"),
                rs.getBigDecimal("net_amount"),
                rs.getObject("due_date", java.time.LocalDate.class),
                rs.getObject("pricing_at", java.time.OffsetDateTime.class),
                rs.getObject("liquidated_at", java.time.OffsetDateTime.class),
                CreditAssignmentStatus.valueOf(rs.getString("status")));
    }
}
