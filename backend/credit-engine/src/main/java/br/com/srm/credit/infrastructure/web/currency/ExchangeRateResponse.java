package br.com.srm.credit.infrastructure.web.currency;

import br.com.srm.credit.domain.shared.ExchangeRateSource;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(name = "ExchangeRateResponse", description = "Taxa de cambio cadastrada.")
public record ExchangeRateResponse(
        @Schema(description = "Identificador da taxa.", example = "1") Long id,
        @Schema(description = "Moeda de origem.", example = "BRL") String fromCurrencyCode,
        @Schema(description = "Moeda de destino.", example = "USD") String toCurrencyCode,
        @Schema(description = "Taxa de cambio.", example = "5.20000000") BigDecimal rate,
        @Schema(description = "Momento da cotacao.", example = "2026-06-27T10:00:00-03:00") OffsetDateTime quotedAt,
        @Schema(description = "Origem da taxa.", example = "MANUAL") ExchangeRateSource source,
        @Schema(description = "Momento de criacao.", example = "2026-06-27T10:00:05-03:00") OffsetDateTime createdAt) {}
