package br.com.srm.credit.infrastructure.configuration;

import br.com.srm.credit.infrastructure.persistence.repository.AssignorJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CreditAssignmentJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CreditBatchJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ReceivableTypeJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypesScanner;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
        basePackageClasses = {
            CurrencyJpaRepository.class,
            ExchangeRateJpaRepository.class,
            AssignorJpaRepository.class,
            CreditAssignmentJpaRepository.class,
            CreditBatchJpaRepository.class,
            ReceivableTypeJpaRepository.class
        })
public class JpaConfiguration {

    @Bean
    public PersistenceManagedTypes persistenceManagedTypes(ResourceLoader resourceLoader) {
        return new PersistenceManagedTypesScanner(resourceLoader, null)
                .scan("br.com.srm.credit.infrastructure.persistence.entity");
    }
}
