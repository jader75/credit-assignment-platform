package br.com.srm.credit.infrastructure.web.pricing;

import br.com.srm.credit.application.pricing.PricingSimulationApplicationService;
import br.com.srm.credit.application.pricing.PricingSimulationCommand;
import br.com.srm.credit.application.pricing.PricingSimulationResult;
import br.com.srm.credit.domain.pricing.PricingRuleCode;
import br.com.srm.credit.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pricing/simulations")
@Tag(name = "Pricing", description = "Operacoes de simulacao de precificacao")
public class PricingSimulationController {

    private final PricingSimulationApplicationService pricingSimulationApplicationService;

    public PricingSimulationController(PricingSimulationApplicationService pricingSimulationApplicationService) {
        this.pricingSimulationApplicationService = pricingSimulationApplicationService;
    }

    @PostMapping
    @Operation(summary = "Simular precificacao de um recebivel")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Simulacao realizada com sucesso",
                content = @Content(schema = @Schema(implementation = PricingSimulationResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Payload invalido",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Regra de negocio violada",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PricingSimulationResponse> simulate(@Valid @RequestBody PricingSimulationRequest request) {
        var command = new PricingSimulationCommand(
                request.operationReference(),
                request.receivableTypeCode(),
                PricingRuleCode.fromCode(request.receivablePricingRuleCode()),
                request.receivableTypeBaseSpread(),
                request.receivableTypeActive(),
                request.faceCurrencyCode(),
                request.paymentCurrencyCode(),
                request.faceAmount(),
                request.baseTaxRate(),
                request.termDays(),
                request.exchangeRate());

        PricingSimulationResult result = pricingSimulationApplicationService.simulate(command);
        return ResponseEntity.status(HttpStatus.OK).body(map(result));
    }

    private static PricingSimulationResponse map(PricingSimulationResult result) {
        return new PricingSimulationResponse(
                result.operationReference(),
                result.receivablePricingRuleCode(),
                result.faceAmount(),
                result.baseTaxRate(),
                result.appliedSpread(),
                result.termDays(),
                result.discountedAmount(),
                result.exchangeRate(),
                result.netAmount(),
                result.crossCurrency());
    }
}
