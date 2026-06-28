package br.com.srm.credit.application.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.srm.credit.application.currency.ExchangeRateQueryService;
import br.com.srm.credit.domain.batch.BatchImportBusinessException;
import br.com.srm.credit.domain.batch.BatchImportMessage;
import br.com.srm.credit.domain.batch.BatchImportValidationException;
import br.com.srm.credit.domain.pricing.CreditPricingResponse;
import br.com.srm.credit.domain.pricing.ReceivablePricingService;
import br.com.srm.credit.domain.pricing.strategy.CommercialReceivablePricingStrategy;
import br.com.srm.credit.domain.pricing.strategy.PostDatedCheckPricingStrategy;
import br.com.srm.credit.domain.pricing.strategy.PricingStrategyResolver;
import br.com.srm.credit.infrastructure.persistence.entity.AssignorEntity;
import br.com.srm.credit.infrastructure.persistence.entity.CreditAssignmentEntity;
import br.com.srm.credit.infrastructure.persistence.entity.CreditBatchEntity;
import br.com.srm.credit.infrastructure.persistence.entity.ReceivableTypeEntity;
import br.com.srm.credit.infrastructure.persistence.repository.AssignorJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CreditAssignmentJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CreditBatchJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ReceivableTypeJpaRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

class BatchImportApplicationServiceTest {

    private CreditBatchJpaRepository creditBatchJpaRepository;
    private CreditAssignmentJpaRepository creditAssignmentJpaRepository;
    private AssignorJpaRepository assignorJpaRepository;
    private ReceivableTypeJpaRepository receivableTypeJpaRepository;
    private CurrencyJpaRepository currencyJpaRepository;
    private ReceivablePricingService receivablePricingService;
    private ExchangeRateQueryService exchangeRateQueryService;
    private BatchImportApplicationService applicationService;

    @BeforeEach
    void setUp() {
        creditBatchJpaRepository = mock(CreditBatchJpaRepository.class, Answers.RETURNS_DEEP_STUBS);
        creditAssignmentJpaRepository = mock(CreditAssignmentJpaRepository.class);
        assignorJpaRepository = mock(AssignorJpaRepository.class);
        receivableTypeJpaRepository = mock(ReceivableTypeJpaRepository.class);
        currencyJpaRepository = mock(CurrencyJpaRepository.class);
        receivablePricingService = mock(ReceivablePricingService.class);
        exchangeRateQueryService = mock(ExchangeRateQueryService.class);

        applicationService = new BatchImportApplicationService(
                creditBatchJpaRepository,
                creditAssignmentJpaRepository,
                assignorJpaRepository,
                receivableTypeJpaRepository,
                currencyJpaRepository,
                receivablePricingService,
                new PricingStrategyResolver(
                        List.of(new CommercialReceivablePricingStrategy(), new PostDatedCheckPricingStrategy())),
                exchangeRateQueryService,
                new SimpleMeterRegistry());

        when(creditBatchJpaRepository.existsByBatchReference(anyString())).thenReturn(false);
        when(creditBatchJpaRepository.save(any(CreditBatchEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(creditAssignmentJpaRepository.existsByOperationReference(anyString()))
                .thenReturn(false);
        when(assignorJpaRepository.findByDocumentNumber(anyString())).thenReturn(Optional.empty());
        when(assignorJpaRepository.save(any(AssignorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(receivableTypeJpaRepository.findById(anyString())).thenReturn(Optional.empty());
        when(receivableTypeJpaRepository.save(any(ReceivableTypeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(currencyJpaRepository.existsById("BRL")).thenReturn(true);
        when(exchangeRateQueryService.resolve(anyString(), anyString())).thenReturn(new BigDecimal("1.00000000"));
        when(receivablePricingService.price(any()))
                .thenReturn(new CreditPricingResponse(
                        "OP-001",
                        "TRADE_RECEIVABLE",
                        new BigDecimal("1000.00"),
                        new BigDecimal("0.0200"),
                        new BigDecimal("0.0150"),
                        30,
                        new BigDecimal("966.18"),
                        new BigDecimal("1.00000000"),
                        new BigDecimal("966.18"),
                        false));
    }

    @Test
    void shouldImportBatchFromCsvFile() {
        var result = applicationService.importBatch(
                "batch-001",
                """
                        operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                        OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                        """
                        .getBytes());

        assertThat(result.batchReference()).isEqualTo("BATCH-001");
        assertThat(result.status()).isEqualTo("PROCESSED");
        assertThat(result.assignmentsImported()).isEqualTo(1);
        assertThat(result.assignorsCreated()).isEqualTo(1);
        assertThat(result.receivableTypesCreated()).isEqualTo(1);
        assertThat(result.processedAt()).isNotNull();
    }

    @Test
    void shouldRejectDuplicateBatchReferenceAndEmptyFile() {
        when(creditBatchJpaRepository.existsByBatchReference("BATCH-001")).thenReturn(true);

        assertThatThrownBy(() -> applicationService.importBatch(
                        "batch-001",
                        """
                                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                                OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                                """
                                .getBytes()))
                .isInstanceOf(BatchImportBusinessException.class)
                .hasMessage(BatchImportMessage.BATCH_ALREADY_EXISTS.message());

        assertThatThrownBy(() -> applicationService.importBatch("batch-002", " ".getBytes()))
                .isInstanceOf(BatchImportValidationException.class)
                .hasMessage(BatchImportMessage.BATCH_FILE_EMPTY.message());
    }

    @Test
    void shouldRejectDuplicatedOperationReferenceInsideTheSameBatch() {
        when(creditBatchJpaRepository.existsByBatchReference("BATCH-001")).thenReturn(false);
        when(creditAssignmentJpaRepository.existsByOperationReference("OP-001")).thenReturn(false);
        when(creditAssignmentJpaRepository.save(any(CreditAssignmentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> applicationService.importBatch(
                        "batch-001",
                        """
                                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                                OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                                OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                                """
                                .getBytes()))
                .isInstanceOf(BatchImportBusinessException.class)
                .hasMessage(BatchImportMessage.BATCH_OPERATION_REFERENCE_ALREADY_EXISTS.message());
    }

    @Test
    void shouldRejectAssignorMismatchAfterCache() {
        when(creditBatchJpaRepository.existsByBatchReference("BATCH-001")).thenReturn(false);
        when(creditAssignmentJpaRepository.existsByOperationReference("OP-001")).thenReturn(false);
        when(assignorJpaRepository.findByDocumentNumber("12345678901"))
                .thenReturn(Optional.of(new AssignorEntity("12345678901", "Acme LTDA", "A")));
        when(receivableTypeJpaRepository.findById("TRADE_RECEIVABLE")).thenReturn(Optional.empty());
        when(receivableTypeJpaRepository.save(any(ReceivableTypeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(currencyJpaRepository.existsById("USD")).thenReturn(false);

        assertThatThrownBy(() -> applicationService.importBatch(
                        "batch-001",
                        """
                                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                                OP-001,12345678901,Acme LTDA,AA,TRADE_RECEIVABLE,Duplicata Mercantil,USD,1000.00,2026-07-20,0.0200,30,BRL
                                """
                                .getBytes()))
                .isInstanceOf(BatchImportBusinessException.class)
                .hasMessage(BatchImportMessage.BATCH_NAME_MISMATCH.message());

        verify(currencyJpaRepository, never()).existsById("BRL");
    }

    @Test
    void shouldRejectAssignorMismatchFromCacheOnSecondRow() {
        when(creditBatchJpaRepository.existsByBatchReference("BATCH-001")).thenReturn(false);
        when(creditAssignmentJpaRepository.existsByOperationReference("OP-001")).thenReturn(false);
        when(creditAssignmentJpaRepository.existsByOperationReference("OP-002")).thenReturn(false);
        when(receivableTypeJpaRepository.findById("TRADE_RECEIVABLE")).thenReturn(Optional.empty());
        when(receivableTypeJpaRepository.save(any(ReceivableTypeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> applicationService.importBatch(
                        "batch-001",
                        """
                                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                                OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                                OP-002,12345678901,Acme SA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,2000.00,2026-07-21,0.0200,30,BRL
                                """
                                .getBytes()))
                .isInstanceOf(BatchImportBusinessException.class)
                .hasMessage(BatchImportMessage.BATCH_NAME_MISMATCH.message());
    }

    @Test
    void shouldRejectInvalidCurrencyBeforePricing() {
        when(creditBatchJpaRepository.existsByBatchReference("BATCH-001")).thenReturn(false);
        when(creditAssignmentJpaRepository.existsByOperationReference("OP-001")).thenReturn(false);
        when(assignorJpaRepository.findByDocumentNumber("12345678901"))
                .thenReturn(Optional.of(new AssignorEntity("12345678901", "Acme LTDA", "A")));
        when(receivableTypeJpaRepository.findById("TRADE_RECEIVABLE")).thenReturn(Optional.empty());
        when(receivableTypeJpaRepository.save(any(ReceivableTypeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(currencyJpaRepository.existsById("USD")).thenReturn(false);

        assertThatThrownBy(() -> applicationService.importBatch(
                        "batch-001",
                        """
                                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                                OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,USD,1000.00,2026-07-20,0.0200,30,BRL
                                """
                                .getBytes()))
                .isInstanceOf(BatchImportBusinessException.class)
                .hasMessage(BatchImportMessage.BATCH_CURRENCY_INVALID.message());
    }

    @Test
    void shouldRejectReceivableTypeMismatchFromCacheOrRepository() {
        when(creditBatchJpaRepository.existsByBatchReference("BATCH-001")).thenReturn(false);
        when(creditAssignmentJpaRepository.existsByOperationReference("OP-001")).thenReturn(false);
        when(receivableTypeJpaRepository.findById("TRADE_RECEIVABLE"))
                .thenReturn(Optional.of(new ReceivableTypeEntity(
                        "TRADE_RECEIVABLE",
                        "Duplicata de Servico",
                        "TRADE_RECEIVABLE",
                        new BigDecimal("0.0200"),
                        true)));

        assertThatThrownBy(() -> applicationService.importBatch(
                        "batch-001",
                        """
                                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                                OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                                """
                                .getBytes()))
                .isInstanceOf(BatchImportBusinessException.class)
                .hasMessage(BatchImportMessage.BATCH_NAME_MISMATCH.message());
    }

    @Test
    void shouldReuseCachedAssignorAndReceivableTypeAcrossRows() {
        when(creditBatchJpaRepository.existsByBatchReference("BATCH-001")).thenReturn(false);
        when(assignorJpaRepository.findByDocumentNumber("12345678901"))
                .thenReturn(Optional.of(new AssignorEntity("12345678901", "Acme LTDA", "A")));
        when(receivableTypeJpaRepository.findById("TRADE_RECEIVABLE"))
                .thenReturn(Optional.of(new ReceivableTypeEntity(
                        "TRADE_RECEIVABLE",
                        "Duplicata Mercantil",
                        "TRADE_RECEIVABLE",
                        new BigDecimal("0.0150"),
                        true)));
        when(creditAssignmentJpaRepository.existsByOperationReference("OP-001")).thenReturn(false);
        when(creditAssignmentJpaRepository.existsByOperationReference("OP-002")).thenReturn(false);

        var result = applicationService.importBatch(
                "batch-001",
                """
                        operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                        OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                        OP-002,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,2000.00,2026-07-21,0.0200,30,BRL
                        """
                        .getBytes());

        assertThat(result.assignmentsImported()).isEqualTo(2);
        assertThat(result.assignorsCreated()).isEqualTo(0);
        assertThat(result.receivableTypesCreated()).isEqualTo(0);
    }

    @Test
    void shouldRejectBlankBatchReferenceAndInvalidAssignorDocument() {
        assertThatThrownBy(() -> applicationService.importBatch(
                        " ",
                        """
                                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                                OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                                """
                                .getBytes()))
                .isInstanceOf(BatchImportValidationException.class)
                .hasMessage(BatchImportMessage.BATCH_REFERENCE_INVALID.message());

        assertThatThrownBy(() -> applicationService.importBatch(
                        "batch-001",
                        """
                                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                                OP-001,ABC,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                                """
                                .getBytes()))
                .isInstanceOf(BatchImportValidationException.class)
                .hasMessage(BatchImportMessage.BATCH_ASSIGNOR_INVALID.message());
    }
}
