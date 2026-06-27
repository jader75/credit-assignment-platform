package br.com.srm.credit.infrastructure.web.currency;

import br.com.srm.credit.domain.shared.ExchangeRateSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(name = "ExchangeRateRequest", description = "Solicitacao de cadastro ou ajuste de taxa de cambio.")
public record ExchangeRateRequest(
        @Schema(description = "Moeda de origem.", example = "BRL") @NotBlank String fromCurrencyCode,
        @Schema(description = "Moeda de destino.", example = "USD") @NotBlank String toCurrencyCode,
        @Schema(description = "Taxa de cambio.", example = "5.20000000") @NotNull BigDecimal rate,
        @Schema(description = "Momento da cotacao.", example = "2026-06-27T10:00:00-03:00") @NotNull
                OffsetDateTime quotedAt,
        @Schema(description = "Origem da taxa.", example = "MANUAL") @NotNull ExchangeRateSource source) {}
