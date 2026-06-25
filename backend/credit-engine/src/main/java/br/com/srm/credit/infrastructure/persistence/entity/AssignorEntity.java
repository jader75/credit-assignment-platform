package br.com.srm.credit.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "assignors")
public class AssignorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_number", nullable = false, unique = true, length = 14)
    private String documentNumber;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "risk_rating", nullable = false, length = 5)
    private String riskRating;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected AssignorEntity() {}

    public AssignorEntity(String documentNumber, String name, String riskRating) {
        this.documentNumber = documentNumber;
        this.name = name;
        this.riskRating = riskRating;
    }

    public Long getId() {
        return id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getName() {
        return name;
    }

    public String getRiskRating() {
        return riskRating;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
