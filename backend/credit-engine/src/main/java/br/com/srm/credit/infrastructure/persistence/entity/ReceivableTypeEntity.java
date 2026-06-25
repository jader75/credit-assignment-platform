package br.com.srm.credit.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "receivable_types")
public class ReceivableTypeEntity {

    @Id
    @Column(name = "code", nullable = false, length = 30, updatable = false)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "pricing_rule_code", nullable = false, length = 50)
    private String pricingRuleCode;

    @Column(name = "base_spread", nullable = false, precision = 8, scale = 4)
    private BigDecimal baseSpread;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected ReceivableTypeEntity() {}

    public ReceivableTypeEntity(
            String code, String name, String pricingRuleCode, BigDecimal baseSpread, boolean active) {
        this.code = code;
        this.name = name;
        this.pricingRuleCode = pricingRuleCode;
        this.baseSpread = baseSpread;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPricingRuleCode() {
        return pricingRuleCode;
    }

    public BigDecimal getBaseSpread() {
        return baseSpread;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
