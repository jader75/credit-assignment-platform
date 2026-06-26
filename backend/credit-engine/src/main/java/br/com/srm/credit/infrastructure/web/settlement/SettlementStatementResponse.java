package br.com.srm.credit.infrastructure.web.settlement;

import br.com.srm.credit.application.settlement.SettlementStatementPage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "SettlementStatementResponse", description = "Resposta paginada do extrato de liquidacao.")
public record SettlementStatementResponse(
        List<SettlementStatementItemResponse> items, int page, int size, long totalElements, long totalPages) {

    public static SettlementStatementResponse from(SettlementStatementPage page) {
        var items = page.items().stream()
                .map(item -> new SettlementStatementItemResponse(
                        item.operationReference(),
                        item.batchReference(),
                        item.assignorDocumentNumber(),
                        item.assignorName(),
                        item.paymentCurrencyCode(),
                        item.faceAmount(),
                        item.netAmount(),
                        item.liquidatedAt(),
                        item.status()))
                .toList();
        return new SettlementStatementResponse(
                items, page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
