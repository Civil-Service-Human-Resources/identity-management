package uk.gov.cshr.job;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "uk.gov.cshr.job",
        entityManagerFactoryRef = "jobEntityManagerFactory",
        transactionManagerRef = "jobTransactionManager"
)
public class jobDbConfig {

    @Primary
    @Bean(name = "jobDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.jobs")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "jobDataSource")
    public DataSource dataSource(@Qualifier("jobDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(name = "jobEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("jobDataSource") DataSource dataSource,
            Map<String, Object> hibernateProperties) {
        return builder
                .dataSource(dataSource)
                .packages("uk.gov.cshr.job.entity")
                .persistenceUnit("job")
                .properties(hibernateProperties)
                .build();
    }

    @Primary
    @Bean(name = "jobTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("jobEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Primary
    @Bean(name = "jobsFlyway", initMethod = "migrate")
    public Flyway jobsFlyway(@Qualifier("jobDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/mysql")
                .baselineOnMigrate(true)
                .load();
    }

}
