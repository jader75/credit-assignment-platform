package br.com.srm.credit.infrastructure.web.currency;

import br.com.srm.credit.application.currency.ExchangeRateAdministrationApplicationService;
import br.com.srm.credit.application.currency.ExchangeRateCommand;
import br.com.srm.credit.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exchange-rates")
@Tag(name = "Currency", description = "Administracao de taxas de cambio")
public class ExchangeRateController {

    private final ExchangeRateAdministrationApplicationService exchangeRateAdministrationApplicationService;

    public ExchangeRateController(
            ExchangeRateAdministrationApplicationService exchangeRateAdministrationApplicationService) {
        this.exchangeRateAdministrationApplicationService = exchangeRateAdministrationApplicationService;
    }

    @GetMapping
    @Operation(summary = "Listar taxas de cambio")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Taxas listadas com sucesso"),
        @ApiResponse(
                responseCode = "500",
                description = "Falha inesperada",
                content =
                        @io.swagger.v3.oas.annotations.media.Content(
                                schema =
                                        @io.swagger.v3.oas.annotations.media.Schema(
                                                implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<ExchangeRateResponse>> list() {
        var items = exchangeRateAdministrationApplicationService.list().stream()
                .map(item -> new ExchangeRateResponse(
                        item.id(),
                        item.fromCurrencyCode(),
                        item.toCurrencyCode(),
                        item.rate(),
                        item.quotedAt(),
                        item.source(),
                        item.createdAt()))
                .toList();
        return ResponseEntity.ok(items);
    }

    @PostMapping
    @Operation(summary = "Cadastrar taxa de cambio")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Taxa cadastrada com sucesso"),
        @ApiResponse(
                responseCode = "400",
                description = "Payload invalido",
                content =
                        @io.swagger.v3.oas.annotations.media.Content(
                                schema =
                                        @io.swagger.v3.oas.annotations.media.Schema(
                                                implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Regra de negocio violada",
                content =
                        @io.swagger.v3.oas.annotations.media.Content(
                                schema =
                                        @io.swagger.v3.oas.annotations.media.Schema(
                                                implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ExchangeRateResponse> create(@Valid @RequestBody ExchangeRateRequest request) {
        var created = exchangeRateAdministrationApplicationService.create(toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar taxa de cambio")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Taxa atualizada com sucesso"),
        @ApiResponse(
                responseCode = "400",
                description = "Payload invalido",
                content =
                        @io.swagger.v3.oas.annotations.media.Content(
                                schema =
                                        @io.swagger.v3.oas.annotations.media.Schema(
                                                implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Regra de negocio violada",
                content =
                        @io.swagger.v3.oas.annotations.media.Content(
                                schema =
                                        @io.swagger.v3.oas.annotations.media.Schema(
                                                implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ExchangeRateResponse> update(
            @PathVariable Long id, @Valid @RequestBody ExchangeRateRequest request) {
        var updated = exchangeRateAdministrationApplicationService.update(id, toCommand(request));
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover taxa de cambio")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Taxa removida com sucesso"),
        @ApiResponse(
                responseCode = "422",
                description = "Regra de negocio violada",
                content =
                        @io.swagger.v3.oas.annotations.media.Content(
                                schema =
                                        @io.swagger.v3.oas.annotations.media.Schema(
                                                implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exchangeRateAdministrationApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static ExchangeRateCommand toCommand(ExchangeRateRequest request) {
        return new ExchangeRateCommand(
                request.fromCurrencyCode(),
                request.toCurrencyCode(),
                request.rate(),
                request.quotedAt(),
                request.source());
    }

    private static ExchangeRateResponse toResponse(br.com.srm.credit.application.currency.ExchangeRateItem item) {
        return new ExchangeRateResponse(
                item.id(),
                item.fromCurrencyCode(),
                item.toCurrencyCode(),
                item.rate(),
                item.quotedAt(),
                item.source(),
                item.createdAt());
    }
}
