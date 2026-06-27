package br.com.srm.credit.infrastructure.persistence.repository;

import br.com.srm.credit.infrastructure.persistence.entity.AssignorEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignorJpaRepository extends JpaRepository<AssignorEntity, Long> {

    Optional<AssignorEntity> findByDocumentNumber(String documentNumber);
}
