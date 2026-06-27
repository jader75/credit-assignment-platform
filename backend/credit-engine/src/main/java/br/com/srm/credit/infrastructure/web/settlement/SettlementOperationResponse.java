package br.com.srm.credit.infrastructure.web.settlement;

import br.com.srm.credit.application.settlement.SettlementOperationPage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "SettlementOperationResponse", description = "Resposta paginada das operacoes de liquidacao.")
public record SettlementOperationResponse(
        List<SettlementOperationItemResponse> items, int page, int size, long totalElements, long totalPages) {

    public static SettlementOperationResponse from(SettlementOperationPage page) {
        var items = page.items().stream()
                .map(item -> new SettlementOperationItemResponse(
                        item.operationReference(),
                        item.batchReference(),
                        item.assignorDocumentNumber(),
                        item.assignorName(),
                        item.receivableTypeCode(),
                        item.faceCurrencyCode(),
                        item.paymentCurrencyCode(),
                        item.faceAmount(),
                        item.netAmount(),
                        item.dueDate(),
                        item.pricingAt(),
                        item.liquidatedAt(),
                        item.status().name()))
                .toList();
        return new SettlementOperationResponse(
                items, page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
