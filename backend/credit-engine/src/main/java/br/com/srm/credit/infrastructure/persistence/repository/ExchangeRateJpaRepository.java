package br.com.srm.credit.infrastructure.persistence.repository;

import br.com.srm.credit.domain.shared.ExchangeRateSource;
import br.com.srm.credit.infrastructure.persistence.entity.ExchangeRateEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateJpaRepository extends JpaRepository<ExchangeRateEntity, Long> {

    List<ExchangeRateEntity> findAllByOrderByQuotedAtDescIdDesc();

    List<ExchangeRateEntity> findAllBySourceInOrderByQuotedAtDescIdDesc(Collection<ExchangeRateSource> sources);

    Optional<ExchangeRateEntity> findFirstByFromCurrencyCodeAndToCurrencyCodeOrderByQuotedAtDescIdDesc(
            String fromCurrencyCode, String toCurrencyCode);

    Optional<ExchangeRateEntity> findFirstByFromCurrencyCodeAndToCurrencyCodeAndSourceInOrderByQuotedAtDescIdDesc(
            String fromCurrencyCode, String toCurrencyCode, Collection<ExchangeRateSource> sources);
}
