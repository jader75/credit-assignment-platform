package br.com.srm.credit.infrastructure.web.settlement;

import br.com.srm.credit.application.settlement.SettlementOperationApplicationService;
import br.com.srm.credit.application.settlement.SettlementOperationFilter;
import br.com.srm.credit.application.settlement.SettlementOperationStatusCommand;
import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/settlements/operations")
@Tag(name = "Settlements", description = "Operacoes de liquidacao e ajustes de status")
public class SettlementOperationController {

    private final SettlementOperationApplicationService settlementOperationApplicationService;

    public SettlementOperationController(SettlementOperationApplicationService settlementOperationApplicationService) {
        this.settlementOperationApplicationService = settlementOperationApplicationService;
    }

    @GetMapping
    @Operation(summary = "Listar operacoes para mesa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parametros invalidos")
    })
    public ResponseEntity<SettlementOperationResponse> search(
            @RequestParam(required = false) List<CreditAssignmentStatus> status,
            @RequestParam(required = false) String batchReference,
            @RequestParam(required = false) String assignorDocumentNumber,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var filter = new SettlementOperationFilter(status, batchReference, assignorDocumentNumber, page, size);
        return ResponseEntity.ok(
                SettlementOperationResponse.from(settlementOperationApplicationService.search(filter)));
    }

    @PatchMapping("/{operationReference}/status")
    @Operation(summary = "Alterar status operacional")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parametros invalidos"),
        @ApiResponse(responseCode = "422", description = "Regra de negocio violada")
    })
    public ResponseEntity<SettlementOperationItemResponse> updateStatus(
            @PathVariable String operationReference, @Valid @RequestBody SettlementOperationStatusRequest request) {
        var result = settlementOperationApplicationService.updateStatus(
                new SettlementOperationStatusCommand(operationReference, request.targetStatus()));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SettlementOperationItemResponse(
                        result.operationReference(),
                        result.batchReference(),
                        result.assignorDocumentNumber(),
                        result.assignorName(),
                        result.receivableTypeCode(),
                        result.faceCurrencyCode(),
                        result.paymentCurrencyCode(),
                        result.faceAmount(),
                        result.netAmount(),
                        result.dueDate(),
                        result.pricingAt(),
                        result.liquidatedAt(),
                        result.status().name()));
    }
}
