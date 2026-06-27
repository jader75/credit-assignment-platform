package br.com.srm.credit.infrastructure.persistence.repository;

import br.com.srm.credit.infrastructure.persistence.entity.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyJpaRepository extends JpaRepository<CurrencyEntity, String> {}
