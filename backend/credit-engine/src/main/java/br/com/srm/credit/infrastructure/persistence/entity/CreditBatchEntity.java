package br.com.srm.credit.infrastructure.persistence.entity;

import br.com.srm.credit.domain.shared.BatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;

@Entity
@Table(name = "credit_batches")
public class CreditBatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_reference", nullable = false, unique = true, length = 50)
    private String batchReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BatchStatus status;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected CreditBatchEntity() {}

    public CreditBatchEntity(String batchReference, BatchStatus status) {
        this.batchReference = batchReference;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getBatchReference() {
        return batchReference;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public Long getVersion() {
        return version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void markProcessed(OffsetDateTime processedAt) {
        this.status = BatchStatus.PROCESSED;
        this.processedAt = processedAt;
    }
}
