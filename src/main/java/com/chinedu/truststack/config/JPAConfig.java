package com.chinedu.truststack.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.*;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

@Configuration
@EnableTransactionManagement
@ComponentScan("com.chinedu.truststack.persist")
@EnableJpaRepositories("com.chinedu.truststack.persist")
@PropertySource("classpath:datasource.properties")
public class JPAConfig {

    @Value("${jdbc.driver.class}")
    private String jdbcDriverClassName;

    @Value("${jdbc.url}")
    private String jdbcUrl;

    @Value("${jdbc.maxPoolSize}")
    private Integer jdbcMaxPoolSize;

    @Value("${jdbc.maxLifetime}")
    private Integer jdbcMaxLifeTime;

    @Value("${jdbc.idletime}")
    private Integer jdbcIdleTime;

    @Value("${jdbc.username}")
    private String jdbcUsername;

    @Value("${jdbc.password}")
    private String jdbcPassword;

    @Value("${hibernate.dialect}")
    private String hibernateDialect;

    @Value("${hibernate.show.sql}")
    private String hibernateShowSql;

    @Value("${hibernate.hbm2ddl.auto}")
    private String hibernateUpate;

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(jdbcDriverClassName);
        hikariDataSource.setJdbcUrl(jdbcUrl);
        hikariDataSource.setMaximumPoolSize(jdbcMaxPoolSize);
        hikariDataSource.setMaxLifetime(jdbcMaxLifeTime);
        hikariDataSource.setIdleTimeout(jdbcIdleTime);
        hikariDataSource.setUsername(jdbcUsername);
        hikariDataSource.setPassword(jdbcPassword);

        return hikariDataSource;

    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setPackagesToScan("com.chinedu.truststack.persist");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        vendorAdapter.setDatabasePlatform(hibernateDialect);
        factoryBean.setJpaVendorAdapter(vendorAdapter);

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.show_sql",hibernateShowSql);
        jpaProperties.put("hibernate.hbm2ddl.auto",hibernateUpate);
        factoryBean.setJpaProperties(jpaProperties);
        return factoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactoryBean().getObject());
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public DozerBeanMapper getMapper() {
        return new DozerBeanMapper();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
