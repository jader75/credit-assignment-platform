package br.com.srm.credit.application.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.srm.credit.domain.batch.BatchImportMessage;
import br.com.srm.credit.domain.batch.BatchImportValidationException;
import org.junit.jupiter.api.Test;

class BatchImportCsvParserTest {

    private final BatchImportCsvParser parser = new BatchImportCsvParser();

    @Test
    void shouldParseContractWithoutExchangeRateColumn() {
        var rows = parser.parse(
                """
                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                """);

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().operationReference()).isEqualTo("OP-001");
        assertThat(rows.getFirst().paymentCurrencyCode()).isEqualTo("BRL");
    }

    @Test
    void shouldRejectLegacyHeaderWithExchangeRateColumn() {
        assertThatThrownBy(
                        () -> parser.parse(
                                """
                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode,exchangeRate
                OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL,1.00000000
                """))
                .isInstanceOf(BatchImportValidationException.class)
                .hasMessage(BatchImportMessage.BATCH_HEADER_INVALID.message());
    }

    @Test
    void shouldRejectEmptyFileAndMalformedRow() {
        assertThatThrownBy(() -> parser.parse(" "))
                .isInstanceOf(BatchImportValidationException.class)
                .hasMessage(BatchImportMessage.BATCH_FILE_EMPTY.message());

        assertThatThrownBy(
                        () -> parser.parse(
                                """
                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00
                """))
                .isInstanceOf(BatchImportValidationException.class)
                .hasMessage(BatchImportMessage.BATCH_ROW_INVALID.message());
    }

    @Test
    void shouldAcceptBomPrefixedHeader() {
        var rows = parser.parse(
                """
                \uFEFFoperationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                """);

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().operationReference()).isEqualTo("OP-001");
    }

    @Test
    void shouldParseQuotedFieldsAndWindowsLineEndings() {
        var rows = parser.parse(
                "operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode\r\n"
                        + "\"OP-002\",\"12345678901\",\"Acme, LTDA\",\"AA\",\"TRADE_RECEIVABLE\",\"Duplicata \"\"Mercantil\"\"\",\"BRL\",\"1000.50\",\"2026-07-20\",\"0.0200\",\"30\",\"BRL\"\r\n"
                        + "\r\n");

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().assignorName()).isEqualTo("Acme, LTDA");
        assertThat(rows.getFirst().receivableTypeName()).isEqualTo("Duplicata \"Mercantil\"");
    }

    @Test
    void shouldRejectUnclosedQuotedRow() {
        assertThatThrownBy(
                        () -> parser.parse(
                                """
                operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
                "OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL
                """))
                .isInstanceOf(BatchImportValidationException.class)
                .hasMessage(BatchImportMessage.BATCH_ROW_INVALID.message());
    }
}
