package br.com.srm.credit.infrastructure.persistence.repository;

import br.com.srm.credit.infrastructure.persistence.entity.CreditAssignmentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditAssignmentJpaRepository extends JpaRepository<CreditAssignmentEntity, Long> {

    boolean existsByOperationReference(String operationReference);

    Optional<CreditAssignmentEntity> findByOperationReference(String operationReference);
}
