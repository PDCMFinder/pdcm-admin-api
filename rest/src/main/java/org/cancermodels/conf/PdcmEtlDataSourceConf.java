package org.cancermodels.conf;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
    basePackages = "org.cancermodels.pdcm_etl",
    entityManagerFactoryRef = "pdcmEtlEntityManagerFactory",
    transactionManagerRef = "pdcmEtlTransactionManager"
)
public class PdcmEtlDataSourceConf {
    @Bean
    @ConfigurationProperties("spring.pdcm-etl")
    public DataSourceProperties pdcmEtlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource pdcmEtlDataSource() {
        return pdcmEtlDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "pdcmEtlEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean pdcmEtlEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
            .dataSource(pdcmEtlDataSource())
            .packages("org.cancermodels.pdcm_etl")
            .persistenceUnit("pdcmEtl")
            .build();
    }

    @Bean(name = "pdcmEtlTransactionManager")
    public PlatformTransactionManager secondTransactionManager(
        @Qualifier("pdcmEtlEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
