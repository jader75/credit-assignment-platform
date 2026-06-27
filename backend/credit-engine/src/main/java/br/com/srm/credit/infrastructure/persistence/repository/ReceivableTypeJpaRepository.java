package br.com.srm.credit.infrastructure.persistence.repository;

import br.com.srm.credit.infrastructure.persistence.entity.ReceivableTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivableTypeJpaRepository extends JpaRepository<ReceivableTypeEntity, String> {}
