package br.com.srm.credit.application.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class BatchImportValueObjectTest {

    @Test
    void shouldExposeBatchImportRecords() {
        var processedAt = OffsetDateTime.parse("2026-06-27T10:00:00Z");
        var row = new BatchImportRow(
                "OP-1",
                "12345678000199",
                "Cedente ABC Ltda",
                "A",
                "TRADE_RECEIVABLE",
                "Duplicata Mercantil",
                "BRL",
                new java.math.BigDecimal("1000.00"),
                java.time.LocalDate.parse("2026-07-20"),
                new java.math.BigDecimal("0.0200"),
                30,
                "BRL");
        var result = new BatchImportResult("BATCH-001", "PROCESSED", 10, 1, 1, processedAt);

        assertThat(row.operationReference()).isEqualTo("OP-1");
        assertThat(row.assignorName()).isEqualTo("Cedente ABC Ltda");
        assertThat(result.batchReference()).isEqualTo("BATCH-001");
        assertThat(result.assignmentsImported()).isEqualTo(10);
    }
}
