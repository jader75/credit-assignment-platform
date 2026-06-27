package br.com.srm.credit.infrastructure.web.batch;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.srm.credit.application.batch.BatchImportApplicationService;
import br.com.srm.credit.application.batch.BatchImportResult;
import br.com.srm.credit.domain.batch.BatchImportMessage;
import br.com.srm.credit.domain.batch.BatchImportValidationException;
import br.com.srm.credit.infrastructure.web.ApiExceptionHandler;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class BatchImportControllerTest {

    private BatchImportApplicationService batchImportApplicationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        batchImportApplicationService = mock(BatchImportApplicationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new BatchImportController(batchImportApplicationService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldImportBatchFromMultipartFile() throws Exception {
        when(batchImportApplicationService.importBatch(anyString(), (byte[]) org.mockito.ArgumentMatchers.any()))
                .thenReturn(new BatchImportResult(
                        "BATCH-001", "PROCESSED", 1, 1, 1, OffsetDateTime.parse("2026-06-27T10:15:30Z")));

        var file = new MockMultipartFile(
                "file",
                "batch.csv",
                "text/csv",
                """
                        operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                        OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                        """
                        .getBytes());

        mockMvc.perform(multipart("/api/v1/batches/imports").file(file).param("batchReference", "BATCH-001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batchReference").value("BATCH-001"))
                .andExpect(jsonPath("$.assignmentsImported").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenBatchReferenceIsMissing() throws Exception {
        when(batchImportApplicationService.importBatch(anyString(), (byte[]) org.mockito.ArgumentMatchers.any()))
                .thenThrow(new BatchImportValidationException(BatchImportMessage.BATCH_REFERENCE_INVALID));

        var file = new MockMultipartFile("file", "batch.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/v1/batches/imports").file(file).param("batchReference", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(BatchImportMessage.BATCH_REFERENCE_INVALID.message()));
    }
}
