package br.com.srm.credit.application.batch;

import br.com.srm.credit.application.currency.ExchangeRateQueryService;
import br.com.srm.credit.domain.batch.BatchImportBusinessException;
import br.com.srm.credit.domain.batch.BatchImportMessage;
import br.com.srm.credit.domain.batch.BatchImportValidationException;
import br.com.srm.credit.domain.pricing.CreditPricingRequest;
import br.com.srm.credit.domain.pricing.PricingRuleCode;
import br.com.srm.credit.domain.pricing.ReceivablePricingService;
import br.com.srm.credit.domain.pricing.strategy.PricingStrategyResolver;
import br.com.srm.credit.domain.shared.BatchStatus;
import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import br.com.srm.credit.domain.shared.StructuredLog;
import br.com.srm.credit.infrastructure.persistence.entity.AssignorEntity;
import br.com.srm.credit.infrastructure.persistence.entity.CreditAssignmentEntity;
import br.com.srm.credit.infrastructure.persistence.entity.CreditBatchEntity;
import br.com.srm.credit.infrastructure.persistence.entity.ReceivableTypeEntity;
import br.com.srm.credit.infrastructure.persistence.repository.AssignorJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CreditAssignmentJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CreditBatchJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ReceivableTypeJpaRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

public class BatchImportApplicationService {

    private final CreditBatchJpaRepository creditBatchJpaRepository;
    private final CreditAssignmentJpaRepository creditAssignmentJpaRepository;
    private final AssignorJpaRepository assignorJpaRepository;
    private final ReceivableTypeJpaRepository receivableTypeJpaRepository;
    private final CurrencyJpaRepository currencyJpaRepository;
    private final ReceivablePricingService receivablePricingService;
    private final PricingStrategyResolver pricingStrategyResolver;
    private final ExchangeRateQueryService exchangeRateQueryService;
    private final MeterRegistry meterRegistry;
    private final BatchImportCsvParser batchImportCsvParser = new BatchImportCsvParser();

    public BatchImportApplicationService(
            CreditBatchJpaRepository creditBatchJpaRepository,
            CreditAssignmentJpaRepository creditAssignmentJpaRepository,
            AssignorJpaRepository assignorJpaRepository,
            ReceivableTypeJpaRepository receivableTypeJpaRepository,
            CurrencyJpaRepository currencyJpaRepository,
            ReceivablePricingService receivablePricingService,
            PricingStrategyResolver pricingStrategyResolver,
            ExchangeRateQueryService exchangeRateQueryService,
            MeterRegistry meterRegistry) {
        this.creditBatchJpaRepository = creditBatchJpaRepository;
        this.creditAssignmentJpaRepository = creditAssignmentJpaRepository;
        this.assignorJpaRepository = assignorJpaRepository;
        this.receivableTypeJpaRepository = receivableTypeJpaRepository;
        this.currencyJpaRepository = currencyJpaRepository;
        this.receivablePricingService = receivablePricingService;
        this.pricingStrategyResolver = pricingStrategyResolver;
        this.exchangeRateQueryService = exchangeRateQueryService;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public BatchImportResult importBatch(String batchReference, byte[] fileContent) {
        var sample = Timer.start(meterRegistry);
        var batchReferenceLabel = safeBatchReference(batchReference);
        try {
            var normalizedBatchReference = normalizeBatchReference(batchReference);
            var csvContent = new String(fileContent, StandardCharsets.UTF_8);
            var rows = batchImportCsvParser.parse(csvContent);
            if (rows.isEmpty()) {
                throw new BatchImportValidationException(BatchImportMessage.BATCH_FILE_EMPTY);
            }
            if (creditBatchJpaRepository.existsByBatchReference(normalizedBatchReference)) {
                throw new BatchImportBusinessException(BatchImportMessage.BATCH_ALREADY_EXISTS);
            }

            StructuredLog.info()
                    .step("start")
                    .append("batchReference", normalizedBatchReference)
                    .append("items", rows.size())
                    .log();

            var batch = creditBatchJpaRepository.save(
                    new CreditBatchEntity(normalizedBatchReference, BatchStatus.PROCESSING));
            var assignorCache = new HashMap<String, AssignorEntity>();
            var receivableTypeCache = new HashMap<String, ReceivableTypeEntity>();
            var seenOperations = new HashSet<String>();
            var assignorsCreated = 0;
            var receivableTypesCreated = 0;
            var importedAssignments = 0;
            var now = OffsetDateTime.now();

            for (var row : rows) {
                var operationReference = normalizeReference(row.operationReference());
                if (!seenOperations.add(operationReference)
                        || creditAssignmentJpaRepository.existsByOperationReference(operationReference)) {
                    throw new BatchImportBusinessException(BatchImportMessage.BATCH_OPERATION_REFERENCE_ALREADY_EXISTS);
                }

                var assignorResolution = resolveAssignor(row, assignorCache);
                if (assignorResolution.created()) {
                    assignorsCreated++;
                }

                var receivableTypeResolution = resolveReceivableType(row, receivableTypeCache);
                if (receivableTypeResolution.created()) {
                    receivableTypesCreated++;
                }
                var receivableType = receivableTypeResolution.entity();
                var assignor = assignorResolution.entity();

                validateCurrency(row.faceCurrencyCode());
                validateCurrency(row.paymentCurrencyCode());

                var pricingRequest = CreditPricingRequest.of(
                        operationReference,
                        receivableType.getPricingRuleCode(),
                        row.faceCurrencyCode(),
                        row.paymentCurrencyCode(),
                        row.faceAmount(),
                        row.baseTaxRate(),
                        row.termDays(),
                        exchangeRateQueryService.resolve(row.faceCurrencyCode(), row.paymentCurrencyCode()));
                var pricingResult = receivablePricingService.price(pricingRequest);

                creditAssignmentJpaRepository.save(new CreditAssignmentEntity(
                        batch,
                        assignor,
                        receivableType,
                        operationReference,
                        row.faceCurrencyCode(),
                        pricingResult.faceAmount(),
                        row.dueDate(),
                        pricingResult.baseTaxRate(),
                        pricingResult.appliedSpread(),
                        pricingResult.termDays(),
                        row.paymentCurrencyCode(),
                        pricingResult.exchangeRate(),
                        pricingResult.netAmount(),
                        now,
                        CreditAssignmentStatus.PRICED));
                importedAssignments++;
            }

            batch.markProcessed(OffsetDateTime.now());
            creditBatchJpaRepository.save(batch);

            var result = new BatchImportResult(
                    normalizedBatchReference,
                    batch.getStatus().name(),
                    importedAssignments,
                    assignorsCreated,
                    receivableTypesCreated,
                    batch.getProcessedAt());

            meterRegistry
                    .counter("credit.batch.import.requests", "outcome", "success")
                    .increment();
            StructuredLog.info()
                    .step("end")
                    .append(result, "batchReference", "status", "assignmentsImported")
                    .log();
            return result;
        } catch (RuntimeException exception) {
            meterRegistry
                    .counter("credit.batch.import.requests", "outcome", "failure")
                    .increment();
            StructuredLog.warn()
                    .step("error")
                    .append("batchReference", batchReferenceLabel)
                    .append("reason", exception.getMessage())
                    .log();
            throw exception;
        } finally {
            sample.stop(meterRegistry.timer("credit.batch.import.duration"));
        }
    }

    private EntityResolution<AssignorEntity> resolveAssignor(BatchImportRow row, Map<String, AssignorEntity> cache) {
        var documentNumber = normalizeDocumentNumber(row.assignorDocumentNumber());
        var cached = cache.get(documentNumber);
        if (cached != null) {
            if (!cached.getName().equals(row.assignorName().trim())
                    || !cached.getRiskRating().equalsIgnoreCase(row.riskRating().trim())) {
                throw new BatchImportBusinessException(BatchImportMessage.BATCH_NAME_MISMATCH);
            }
            return new EntityResolution<>(cached, false);
        }

        var existing = assignorJpaRepository.findByDocumentNumber(documentNumber);
        if (existing.isPresent()) {
            var assignor = existing.get();
            if (!assignor.getName().equals(row.assignorName().trim())
                    || !assignor.getRiskRating()
                            .equalsIgnoreCase(row.riskRating().trim())) {
                throw new BatchImportBusinessException(BatchImportMessage.BATCH_NAME_MISMATCH);
            }
            cache.put(documentNumber, assignor);
            return new EntityResolution<>(assignor, false);
        }

        var saved = assignorJpaRepository.save(new AssignorEntity(
                documentNumber,
                row.assignorName().trim(),
                row.riskRating().trim().toUpperCase(Locale.ROOT)));
        cache.put(documentNumber, saved);
        return new EntityResolution<>(saved, true);
    }

    private EntityResolution<ReceivableTypeEntity> resolveReceivableType(
            BatchImportRow row, Map<String, ReceivableTypeEntity> cache) {
        var code = normalizeReference(row.receivableTypeCode());
        var strategy = pricingStrategyResolver.resolve(PricingRuleCode.fromCode(code));
        var cached = cache.get(code);
        if (cached != null) {
            if (!cached.getName().equals(row.receivableTypeName().trim())
                    || !cached.getPricingRuleCode()
                            .equalsIgnoreCase(strategy.ruleCode().code())
                    || cached.getBaseSpread().compareTo(strategy.baseSpread()) != 0) {
                throw new BatchImportBusinessException(BatchImportMessage.BATCH_NAME_MISMATCH);
            }
            return new EntityResolution<>(cached, false);
        }

        var existing = receivableTypeJpaRepository.findById(code);
        if (existing.isPresent()) {
            var receivableType = existing.get();
            if (!receivableType.getName().equals(row.receivableTypeName().trim())
                    || !receivableType
                            .getPricingRuleCode()
                            .equalsIgnoreCase(strategy.ruleCode().code())
                    || receivableType.getBaseSpread().compareTo(strategy.baseSpread()) != 0) {
                throw new BatchImportBusinessException(BatchImportMessage.BATCH_NAME_MISMATCH);
            }
            receivableType = receivableTypeJpaRepository.save(new ReceivableTypeEntity(
                    code, row.receivableTypeName().trim(), strategy.ruleCode().code(), strategy.baseSpread(), true));
            cache.put(code, receivableType);
            return new EntityResolution<>(receivableType, false);
        }

        var saved = receivableTypeJpaRepository.save(new ReceivableTypeEntity(
                code, row.receivableTypeName().trim(), strategy.ruleCode().code(), strategy.baseSpread(), true));
        cache.put(code, saved);
        return new EntityResolution<>(saved, true);
    }

    private void validateCurrency(String currencyCode) {
        var normalized = normalizeReference(currencyCode);
        if (!currencyJpaRepository.existsById(normalized)) {
            throw new BatchImportBusinessException(BatchImportMessage.BATCH_CURRENCY_INVALID);
        }
    }

    private static String normalizeBatchReference(String batchReference) {
        if (batchReference == null || batchReference.isBlank()) {
            throw new BatchImportValidationException(BatchImportMessage.BATCH_REFERENCE_INVALID);
        }
        return batchReference.trim().toUpperCase(Locale.ROOT);
    }

    private static String safeBatchReference(String batchReference) {
        if (batchReference == null || batchReference.isBlank()) {
            return "<invalid>";
        }
        return batchReference.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeReference(String value) {
        if (value == null || value.isBlank()) {
            throw new BatchImportValidationException(BatchImportMessage.BATCH_ROW_INVALID);
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeDocumentNumber(String value) {
        var digits = normalizeReference(value).replaceAll("\\D", "");
        if (digits.isBlank()) {
            throw new BatchImportValidationException(BatchImportMessage.BATCH_ASSIGNOR_INVALID);
        }
        return digits;
    }

    private record EntityResolution<T>(T entity, boolean created) {}
}
