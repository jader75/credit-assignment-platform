package br.com.srm.credit.infrastructure.web.settlement;

import br.com.srm.credit.application.settlement.SettlementStatementApplicationService;
import br.com.srm.credit.application.settlement.SettlementStatementFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/settlements/statements")
@Tag(name = "Settlements", description = "Consultas analiticas de liquidacao")
public class SettlementStatementController {

    private final SettlementStatementApplicationService settlementStatementApplicationService;

    public SettlementStatementController(SettlementStatementApplicationService settlementStatementApplicationService) {
        this.settlementStatementApplicationService = settlementStatementApplicationService;
    }

    @GetMapping
    @Operation(summary = "Listar extrato de liquidacao")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parametros invalidos")
    })
    public ResponseEntity<SettlementStatementResponse> search(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String assignorDocumentNumber,
            @RequestParam(required = false) String paymentCurrencyCode,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var filter = new SettlementStatementFilter(
                startDate, endDate, assignorDocumentNumber, paymentCurrencyCode, page, size);
        return ResponseEntity.ok(
                SettlementStatementResponse.from(settlementStatementApplicationService.search(filter)));
    }
}
