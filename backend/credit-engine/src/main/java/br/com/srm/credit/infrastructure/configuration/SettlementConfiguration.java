package br.com.srm.credit.infrastructure.configuration;

import br.com.srm.credit.application.settlement.SettlementStatementApplicationService;
import br.com.srm.credit.application.settlement.SettlementStatementReadRepository;
import br.com.srm.credit.infrastructure.persistence.report.JdbcSettlementStatementReadRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class SettlementConfiguration {

    @Bean
    public SettlementStatementReadRepository settlementStatementReadRepository(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new JdbcSettlementStatementReadRepository(namedParameterJdbcTemplate);
    }

    @Bean
    public SettlementStatementApplicationService settlementStatementApplicationService(
            SettlementStatementReadRepository settlementStatementReadRepository, MeterRegistry meterRegistry) {
        return new SettlementStatementApplicationService(settlementStatementReadRepository, meterRegistry);
    }
}
