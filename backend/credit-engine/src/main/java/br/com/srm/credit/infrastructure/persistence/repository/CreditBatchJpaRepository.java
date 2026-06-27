package br.com.srm.credit.infrastructure.persistence.repository;

import br.com.srm.credit.infrastructure.persistence.entity.CreditBatchEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditBatchJpaRepository extends JpaRepository<CreditBatchEntity, Long> {

    boolean existsByBatchReference(String batchReference);

    Optional<CreditBatchEntity> findByBatchReference(String batchReference);
}
