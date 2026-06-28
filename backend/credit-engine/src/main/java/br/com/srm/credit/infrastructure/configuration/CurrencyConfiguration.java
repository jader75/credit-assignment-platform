package br.com.srm.credit.infrastructure.configuration;

import br.com.srm.credit.application.currency.CurrencyQueryService;
import br.com.srm.credit.application.currency.ExchangeRateAdministrationApplicationService;
import br.com.srm.credit.application.currency.ExchangeRateCacheClient;
import br.com.srm.credit.application.currency.ExchangeRateQueryService;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CurrencyConfiguration {

    @Bean
    public CurrencyQueryService currencyQueryService(CurrencyJpaRepository currencyJpaRepository) {
        return new CurrencyQueryService(currencyJpaRepository);
    }

    @Bean
    public ExchangeRateAdministrationApplicationService exchangeRateAdministrationApplicationService(
            CurrencyJpaRepository currencyJpaRepository, ExchangeRateJpaRepository exchangeRateJpaRepository) {
        return new ExchangeRateAdministrationApplicationService(currencyJpaRepository, exchangeRateJpaRepository);
    }

    @Bean
    public ExchangeRateQueryService exchangeRateQueryService(
            CurrencyJpaRepository currencyJpaRepository,
            ExchangeRateJpaRepository exchangeRateJpaRepository,
            ExchangeRateCacheClient exchangeRateCacheClient) {
        return new ExchangeRateQueryService(currencyJpaRepository, exchangeRateJpaRepository, exchangeRateCacheClient);
    }
}
