package br.com.srm.credit.infrastructure.configuration;

import br.com.srm.credit.application.settlement.SettlementOperationApplicationService;
import br.com.srm.credit.application.settlement.SettlementOperationReadRepository;
import br.com.srm.credit.infrastructure.persistence.report.JdbcSettlementOperationReadRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CreditAssignmentJpaRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class SettlementOperationConfiguration {

    @Bean
    public SettlementOperationReadRepository settlementOperationReadRepository(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new JdbcSettlementOperationReadRepository(namedParameterJdbcTemplate);
    }

    @Bean
    public SettlementOperationApplicationService settlementOperationApplicationService(
            SettlementOperationReadRepository settlementOperationReadRepository,
            CreditAssignmentJpaRepository creditAssignmentJpaRepository,
            MeterRegistry meterRegistry) {
        return new SettlementOperationApplicationService(
                settlementOperationReadRepository, creditAssignmentJpaRepository, meterRegistry);
    }
}
