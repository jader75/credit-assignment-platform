package br.com.srm.credit.application.batch;

import java.time.OffsetDateTime;

public record BatchImportResult(
        String batchReference,
        String status,
        int assignmentsImported,
        int assignorsCreated,
        int receivableTypesCreated,
        OffsetDateTime processedAt) {}
