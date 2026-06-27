package br.com.srm.credit.infrastructure.configuration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class FlywayMigrationConfiguration {

    @Bean
    static BeanFactoryPostProcessor runFlywayMigration(Environment environment) {
        return beanFactory -> {
            DataSource dataSource = migrationDataSource(environment);
            Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();
        };
    }

    private static DataSource migrationDataSource(Environment environment) {
        var dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(
                environment.getProperty("spring.datasource.driver-class-name", "org.postgresql.Driver"));
        dataSource.setUrl(environment.getProperty(
                "spring.datasource.url", "jdbc:postgresql://localhost:5432/credit_assignment_db"));
        dataSource.setUsername(environment.getProperty("spring.datasource.username", "platform_user"));
        dataSource.setPassword(environment.getProperty("spring.datasource.password", "platform_password"));
        return dataSource;
    }
}
