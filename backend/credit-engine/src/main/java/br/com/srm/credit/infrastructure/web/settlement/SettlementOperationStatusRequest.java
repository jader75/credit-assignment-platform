package br.com.srm.credit.infrastructure.web.settlement;

import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "SettlementOperationStatusRequest", description = "Pedido de mudança de status operacional.")
public record SettlementOperationStatusRequest(
        @NotNull @Schema(description = "Status de destino.", example = "LIQUIDATED")
                CreditAssignmentStatus targetStatus) {}
