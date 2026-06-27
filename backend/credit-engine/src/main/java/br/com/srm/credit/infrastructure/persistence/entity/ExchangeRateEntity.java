package br.com.srm.credit.infrastructure.persistence.entity;

import br.com.srm.credit.domain.shared.ExchangeRateSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "exchange_rates")
public class ExchangeRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrencyCode;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrencyCode;

    @Column(name = "rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal rate;

    @Column(name = "quoted_at", nullable = false)
    private OffsetDateTime quotedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private ExchangeRateSource source;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected ExchangeRateEntity() {}

    public ExchangeRateEntity(
            String fromCurrencyCode,
            String toCurrencyCode,
            BigDecimal rate,
            OffsetDateTime quotedAt,
            ExchangeRateSource source) {
        this.fromCurrencyCode = fromCurrencyCode;
        this.toCurrencyCode = toCurrencyCode;
        this.rate = rate;
        this.quotedAt = quotedAt;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public String getFromCurrencyCode() {
        return fromCurrencyCode;
    }

    public String getToCurrencyCode() {
        return toCurrencyCode;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void update(
            String fromCurrencyCode,
            String toCurrencyCode,
            BigDecimal rate,
            OffsetDateTime quotedAt,
            ExchangeRateSource source) {
        this.fromCurrencyCode = fromCurrencyCode;
        this.toCurrencyCode = toCurrencyCode;
        this.rate = rate;
        this.quotedAt = quotedAt;
        this.source = source;
    }

    public OffsetDateTime getQuotedAt() {
        return quotedAt;
    }

    public ExchangeRateSource getSource() {
        return source;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
