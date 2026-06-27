package br.com.srm.credit.infrastructure.web.batch;

import br.com.srm.credit.application.batch.BatchImportResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record BatchImportResponse(
        @Schema(description = "Referencia do lote importado.", example = "BATCH-001") String batchReference,
        @Schema(description = "Status final do lote.", example = "PROCESSED") String status,
        @Schema(description = "Quantidade de recebiveis importados.", example = "10") int assignmentsImported,
        @Schema(description = "Quantidade de cedentes criados durante a importacao.", example = "1")
                int assignorsCreated,
        @Schema(description = "Quantidade de tipos de recebivel criados durante a importacao.", example = "1")
                int receivableTypesCreated,
        @Schema(description = "Momento de processamento do lote.") OffsetDateTime processedAt) {

    public static BatchImportResponse from(BatchImportResult result) {
        return new BatchImportResponse(
                result.batchReference(),
                result.status(),
                result.assignmentsImported(),
                result.assignorsCreated(),
                result.receivableTypesCreated(),
                result.processedAt());
    }
}
