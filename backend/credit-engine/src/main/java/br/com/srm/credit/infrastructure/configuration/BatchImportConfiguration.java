package br.com.srm.credit.infrastructure.configuration;

import br.com.srm.credit.application.batch.BatchImportApplicationService;
import br.com.srm.credit.application.currency.ExchangeRateQueryService;
import br.com.srm.credit.domain.pricing.ReceivablePricingService;
import br.com.srm.credit.domain.pricing.strategy.PricingStrategyResolver;
import br.com.srm.credit.infrastructure.persistence.repository.AssignorJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CreditAssignmentJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CreditBatchJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ReceivableTypeJpaRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchImportConfiguration {

    @Bean
    public BatchImportApplicationService batchImportApplicationService(
            CreditBatchJpaRepository creditBatchJpaRepository,
            CreditAssignmentJpaRepository creditAssignmentJpaRepository,
            AssignorJpaRepository assignorJpaRepository,
            ReceivableTypeJpaRepository receivableTypeJpaRepository,
            CurrencyJpaRepository currencyJpaRepository,
            ReceivablePricingService receivablePricingService,
            PricingStrategyResolver pricingStrategyResolver,
            ExchangeRateQueryService exchangeRateQueryService,
            MeterRegistry meterRegistry) {
        return new BatchImportApplicationService(
                creditBatchJpaRepository,
                creditAssignmentJpaRepository,
                assignorJpaRepository,
                receivableTypeJpaRepository,
                currencyJpaRepository,
                receivablePricingService,
                pricingStrategyResolver,
                exchangeRateQueryService,
                meterRegistry);
    }
}
