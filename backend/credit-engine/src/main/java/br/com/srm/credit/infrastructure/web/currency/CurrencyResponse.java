package br.com.srm.credit.infrastructure.web.currency;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "CurrencyResponse", description = "Moeda cadastrada no sistema.")
public record CurrencyResponse(
        @Schema(description = "Codigo da moeda.", example = "BRL") String code,
        @Schema(description = "Nome da moeda.", example = "Real brasileiro") String name,
        @Schema(description = "Simbolo da moeda.", example = "R$") String symbol,
        @Schema(description = "Momento de criacao.", example = "2026-06-27T10:00:00-03:00") OffsetDateTime createdAt) {}
