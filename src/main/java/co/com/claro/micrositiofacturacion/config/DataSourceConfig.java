package co.com.claro.micrositiofacturacion.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean(name = "gestionDataSourceProperties")
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties gestionDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(
            @Qualifier("gestionDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "auditDataSourceProperties")
    @ConfigurationProperties("multi-db.configs.audit")
    @ConditionalOnProperty(prefix = "multi-db.audit", name = "enabled", havingValue = "true")
    public DataSourceProperties auditDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "auditDataSource")
    @ConfigurationProperties("multi-db.configs.audit.hikari")
    @ConditionalOnProperty(prefix = "multi-db.audit", name = "enabled", havingValue = "true")
    public HikariDataSource auditDataSource(
            @Qualifier("auditDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "auditJdbcTemplate")
    @ConditionalOnProperty(prefix = "multi-db.audit", name = "enabled", havingValue = "true")
    public JdbcTemplate auditJdbcTemplate(@Qualifier("auditDataSource") DataSource auditDataSource) {
        return new JdbcTemplate(auditDataSource);
    }
}
