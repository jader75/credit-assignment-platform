package br.com.srm.credit.infrastructure.persistence.entity;

import br.com.srm.credit.domain.settlement.SettlementBusinessException;
import br.com.srm.credit.domain.settlement.SettlementMessage;
import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "credit_assignments")
public class CreditAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private CreditBatchEntity batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignor_id", nullable = false)
    private AssignorEntity assignor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receivable_type_code", nullable = false)
    private ReceivableTypeEntity receivableType;

    @Column(name = "operation_reference", nullable = false, unique = true, length = 50)
    private String operationReference;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "face_currency", nullable = false, length = 3)
    private String faceCurrencyCode;

    @Column(name = "face_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal faceAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "base_tax_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal baseTaxRate;

    @Column(name = "applied_spread", nullable = false, precision = 8, scale = 4)
    private BigDecimal appliedSpread;

    @Column(name = "term_days", nullable = false)
    private Integer termDays;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "payment_currency", nullable = false, length = 3)
    private String paymentCurrencyCode;

    @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "net_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "pricing_at", nullable = false)
    private OffsetDateTime pricingAt;

    @Column(name = "liquidated_at")
    private OffsetDateTime liquidatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CreditAssignmentStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected CreditAssignmentEntity() {}

    public CreditAssignmentEntity(
            CreditBatchEntity batch,
            AssignorEntity assignor,
            ReceivableTypeEntity receivableType,
            String operationReference,
            String faceCurrencyCode,
            BigDecimal faceAmount,
            LocalDate dueDate,
            BigDecimal baseTaxRate,
            BigDecimal appliedSpread,
            Integer termDays,
            String paymentCurrencyCode,
            BigDecimal exchangeRate,
            BigDecimal netAmount,
            OffsetDateTime pricingAt,
            CreditAssignmentStatus status) {
        this.batch = batch;
        this.assignor = assignor;
        this.receivableType = receivableType;
        this.operationReference = operationReference;
        this.faceCurrencyCode = faceCurrencyCode;
        this.faceAmount = faceAmount;
        this.dueDate = dueDate;
        this.baseTaxRate = baseTaxRate;
        this.appliedSpread = appliedSpread;
        this.termDays = termDays;
        this.paymentCurrencyCode = paymentCurrencyCode;
        this.exchangeRate = exchangeRate;
        this.netAmount = netAmount;
        this.pricingAt = pricingAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public CreditBatchEntity getBatch() {
        return batch;
    }

    public AssignorEntity getAssignor() {
        return assignor;
    }

    public ReceivableTypeEntity getReceivableType() {
        return receivableType;
    }

    public String getOperationReference() {
        return operationReference;
    }

    public String getFaceCurrencyCode() {
        return faceCurrencyCode;
    }

    public BigDecimal getFaceAmount() {
        return faceAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getBaseTaxRate() {
        return baseTaxRate;
    }

    public BigDecimal getAppliedSpread() {
        return appliedSpread;
    }

    public Integer getTermDays() {
        return termDays;
    }

    public String getPaymentCurrencyCode() {
        return paymentCurrencyCode;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public OffsetDateTime getPricingAt() {
        return pricingAt;
    }

    public OffsetDateTime getLiquidatedAt() {
        return liquidatedAt;
    }

    public CreditAssignmentStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void liquidate(OffsetDateTime liquidatedAt) {
        changeStatus(CreditAssignmentStatus.LIQUIDATED, liquidatedAt);
    }

    public void changeStatus(CreditAssignmentStatus targetStatus, OffsetDateTime changedAt) {
        if (targetStatus == null) {
            throw new SettlementBusinessException(SettlementMessage.TARGET_STATUS_INVALID);
        }
        if (this.status == CreditAssignmentStatus.LIQUIDATED && targetStatus != CreditAssignmentStatus.LIQUIDATED) {
            throw new SettlementBusinessException(SettlementMessage.OPERATION_ALREADY_LIQUIDATED);
        }
        if (targetStatus == CreditAssignmentStatus.LIQUIDATED) {
            this.status = targetStatus;
            this.liquidatedAt = changedAt;
            return;
        }
        this.status = targetStatus;
        this.liquidatedAt = null;
    }
}
