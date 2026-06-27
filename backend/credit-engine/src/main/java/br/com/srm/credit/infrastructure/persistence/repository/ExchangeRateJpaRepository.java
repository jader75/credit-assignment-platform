package br.com.srm.credit.infrastructure.persistence.repository;

import br.com.srm.credit.infrastructure.persistence.entity.ExchangeRateEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateJpaRepository extends JpaRepository<ExchangeRateEntity, Long> {

    List<ExchangeRateEntity> findAllByOrderByQuotedAtDescIdDesc();

    Optional<ExchangeRateEntity> findFirstByFromCurrencyCodeAndToCurrencyCodeOrderByQuotedAtDescIdDesc(
            String fromCurrencyCode, String toCurrencyCode);
}
