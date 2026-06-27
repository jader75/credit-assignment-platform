package br.com.srm.credit.infrastructure.web.currency;

import br.com.srm.credit.application.currency.CurrencyQueryService;
import br.com.srm.credit.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/currencies")
@Tag(name = "Currency", description = "Consulta de moedas")
public class CurrencyController {

    private final CurrencyQueryService currencyQueryService;

    public CurrencyController(CurrencyQueryService currencyQueryService) {
        this.currencyQueryService = currencyQueryService;
    }

    @GetMapping
    @Operation(summary = "Listar moedas cadastradas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Moedas listadas com sucesso"),
        @ApiResponse(
                responseCode = "500",
                description = "Falha inesperada",
                content =
                        @io.swagger.v3.oas.annotations.media.Content(
                                schema =
                                        @io.swagger.v3.oas.annotations.media.Schema(
                                                implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<CurrencyResponse>> list() {
        var items = currencyQueryService.list().stream()
                .map(item -> new CurrencyResponse(item.code(), item.name(), item.symbol(), item.createdAt()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
