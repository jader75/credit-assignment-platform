package br.com.srm.credit.application.batch;

import br.com.srm.credit.domain.batch.BatchImportBusinessException;
import br.com.srm.credit.domain.batch.BatchImportMessage;
import br.com.srm.credit.domain.batch.BatchImportValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class BatchImportCsvParser {

    private static final List<String> EXPECTED_HEADERS = List.of(
            "operationReference",
            "assignorDocumentNumber",
            "assignorName",
            "riskRating",
            "receivableTypeCode",
            "receivableTypeName",
            "faceCurrencyCode",
            "faceAmount",
            "dueDate",
            "baseTaxRate",
            "termDays",
            "paymentCurrencyCode");

    List<BatchImportRow> parse(String csvContent) {
        if (csvContent == null || csvContent.isBlank()) {
            throw new BatchImportValidationException(BatchImportMessage.BATCH_FILE_EMPTY);
        }

        var lines = csvContent.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        if (lines.length < 2) {
            throw new BatchImportBusinessException(BatchImportMessage.BATCH_FILE_EMPTY);
        }

        var header = normalizeHeader(parseLine(lines[0]));
        if (!header.equals(EXPECTED_HEADERS)) {
            throw new BatchImportValidationException(BatchImportMessage.BATCH_HEADER_INVALID);
        }

        var rows = new ArrayList<BatchImportRow>();
        for (var index = 1; index < lines.length; index++) {
            var line = lines[index].trim();
            if (line.isEmpty()) {
                continue;
            }
            var columns = parseLine(line);
            if (columns.size() != EXPECTED_HEADERS.size()) {
                throw new BatchImportValidationException(BatchImportMessage.BATCH_ROW_INVALID);
            }
            rows.add(toRow(columns));
        }
        return rows;
    }

    private static List<String> normalizeHeader(List<String> header) {
        if (!header.isEmpty()) {
            header.set(0, header.get(0).replace("\uFEFF", ""));
        }
        return header;
    }

    private static BatchImportRow toRow(List<String> columns) {
        return new BatchImportRow(
                required(columns, 0, BatchImportMessage.BATCH_ROW_INVALID),
                required(columns, 1, BatchImportMessage.BATCH_ROW_INVALID),
                required(columns, 2, BatchImportMessage.BATCH_ROW_INVALID),
                required(columns, 3, BatchImportMessage.BATCH_ROW_INVALID),
                required(columns, 4, BatchImportMessage.BATCH_ROW_INVALID),
                required(columns, 5, BatchImportMessage.BATCH_ROW_INVALID),
                required(columns, 6, BatchImportMessage.BATCH_ROW_INVALID),
                parseDecimal(required(columns, 7, BatchImportMessage.BATCH_ROW_INVALID)),
                LocalDate.parse(required(columns, 8, BatchImportMessage.BATCH_ROW_INVALID)),
                parseDecimal(required(columns, 9, BatchImportMessage.BATCH_ROW_INVALID)),
                Integer.valueOf(required(columns, 10, BatchImportMessage.BATCH_ROW_INVALID)),
                required(columns, 11, BatchImportMessage.BATCH_ROW_INVALID));
    }

    private static String required(List<String> columns, int index, BatchImportMessage message) {
        if (index >= columns.size()) {
            throw new BatchImportValidationException(message);
        }
        var value = columns.get(index);
        if (value == null || value.isBlank()) {
            throw new BatchImportValidationException(message);
        }
        return value.trim();
    }

    private static BigDecimal parseDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new BatchImportValidationException(BatchImportMessage.BATCH_ROW_INVALID);
        }
    }

    private static List<String> parseLine(String line) {
        var values = new ArrayList<String>();
        var current = new StringBuilder();
        var inQuotes = false;

        for (var index = 0; index < line.length(); index++) {
            var character = line.charAt(index);
            if (inQuotes) {
                if (character == '"') {
                    if (index + 1 < line.length() && line.charAt(index + 1) == '"') {
                        current.append('"');
                        index++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(character);
                }
            } else if (character == ',') {
                values.add(current.toString().trim());
                current.setLength(0);
            } else if (character == '"') {
                inQuotes = true;
            } else {
                current.append(character);
            }
        }

        if (inQuotes) {
            throw new BatchImportValidationException(BatchImportMessage.BATCH_ROW_INVALID);
        }

        values.add(current.toString().trim());
        return values;
    }
}
