package br.com.srm.credit.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "ApiErrorResponse", description = "Resposta padronizada de erro da API.")
public record ApiErrorResponse(
        @Schema(description = "Momento do erro.", example = "2026-06-26T04:35:00-03:00") OffsetDateTime timestamp,
        @Schema(description = "Codigo HTTP retornado.", example = "400") int status,
        @Schema(description = "Descricao do status HTTP.", example = "Bad Request") String error,
        @Schema(description = "Mensagem de erro.", example = "Corpo da requisicao invalido.") String message,
        @Schema(description = "Caminho da requisicao.", example = "/api/v1/pricing/simulations") String path) {}
