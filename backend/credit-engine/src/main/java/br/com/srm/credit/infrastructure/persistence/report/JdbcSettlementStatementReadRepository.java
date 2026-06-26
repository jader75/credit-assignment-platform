package br.com.srm.credit.infrastructure.persistence.report;

import br.com.srm.credit.application.settlement.SettlementStatementFilter;
import br.com.srm.credit.application.settlement.SettlementStatementItem;
import br.com.srm.credit.application.settlement.SettlementStatementPage;
import br.com.srm.credit.application.settlement.SettlementStatementReadRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class JdbcSettlementStatementReadRepository implements SettlementStatementReadRepository {

    private static final String BASE_FROM =
            """
            FROM credit_assignments ca
            JOIN credit_batches cb ON cb.id = ca.batch_id
            JOIN assignors a ON a.id = ca.assignor_id
            """;

    private static final String BASE_WHERE = """
            WHERE ca.liquidated_at IS NOT NULL
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcSettlementStatementReadRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SettlementStatementPage search(SettlementStatementFilter filter) {
        var filterParams = new MapSqlParameterSource();
        var where = buildWhere(filter, filterParams);

        var listSql =
                """
                SELECT
                    ca.operation_reference,
                    cb.batch_reference,
                    a.document_number,
                        a.name,
                        ca.payment_currency,
                        ca.face_amount,
                        ca.net_amount,
                        ca.liquidated_at,
                        ca.status
                """
                        + BASE_FROM
                        + where
                        + """

                        ORDER BY ca.liquidated_at DESC, ca.id DESC
                        LIMIT :limit OFFSET :offset
                        """;
        var params = new MapSqlParameterSource(filterParams.getValues());
        params.addValue("limit", filter.size());
        params.addValue("offset", filter.page() * filter.size());

        var countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        var countParams = new MapSqlParameterSource(filterParams.getValues());

        List<SettlementStatementItem> items = jdbcTemplate.query(listSql, params, settlementStatementRowMapper());
        Long totalElements = jdbcTemplate.queryForObject(countSql, countParams, Long.class);
        long safeTotalElements = totalElements == null ? 0L : totalElements;
        long totalPages = filter.size() == 0 ? 0L : (long) Math.ceil((double) safeTotalElements / filter.size());

        return new SettlementStatementPage(items, filter.page(), filter.size(), safeTotalElements, totalPages);
    }

    private static String buildWhere(SettlementStatementFilter filter, MapSqlParameterSource params) {
        var where = new StringBuilder(BASE_WHERE);

        if (filter.startDate() != null) {
            where.append(" AND ca.liquidated_at >= :startDate");
            params.addValue("startDate", filter.startDate());
        }
        if (filter.endDate() != null) {
            where.append(" AND ca.liquidated_at < :endExclusiveDate");
            params.addValue("endExclusiveDate", filter.endDate().plusDays(1));
        }
        if (filter.assignorDocumentNumber() != null
                && !filter.assignorDocumentNumber().isBlank()) {
            where.append(" AND a.document_number = :assignorDocumentNumber");
            params.addValue("assignorDocumentNumber", filter.assignorDocumentNumber());
        }
        if (filter.paymentCurrencyCode() != null
                && !filter.paymentCurrencyCode().isBlank()) {
            where.append(" AND ca.payment_currency = :paymentCurrencyCode");
            params.addValue("paymentCurrencyCode", filter.paymentCurrencyCode());
        }

        return where.toString();
    }

    private static RowMapper<SettlementStatementItem> settlementStatementRowMapper() {
        return (rs, rowNum) -> new SettlementStatementItem(
                rs.getString("operation_reference"),
                rs.getString("batch_reference"),
                rs.getString("document_number"),
                rs.getString("name"),
                rs.getString("payment_currency"),
                rs.getBigDecimal("face_amount"),
                rs.getBigDecimal("net_amount"),
                rs.getObject("liquidated_at", OffsetDateTime.class),
                rs.getString("status"));
    }
}
