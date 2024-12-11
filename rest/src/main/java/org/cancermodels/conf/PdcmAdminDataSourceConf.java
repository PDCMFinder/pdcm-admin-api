package org.cancermodels.conf;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
    basePackages = "org.cancermodels.pdcm_admin",
    entityManagerFactoryRef = "pdcmAdminEntityManagerFactory",
    transactionManagerRef = "pdcmAdminTransactionManager"
)
public class PdcmAdminDataSourceConf {
    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties pdcmAdminDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource pdcmAdminDataSource() {
        return pdcmAdminDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(name = "pdcmAdminEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean pdcmAdminEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
            .dataSource(pdcmAdminDataSource())
            .packages("org.cancermodels.pdcm_admin")
            .persistenceUnit("pdcmAdmin")

            .build();
    }

    @Bean(name = "pdcmAdminTransactionManager")
    public PlatformTransactionManager pdcmAdminTransactionManager(
        @Qualifier("pdcmAdminEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
