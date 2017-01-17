package test.jpa.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class BaseConfig {
    private static final int MAX_WAIT = 300000;

    @Bean(name = "clientSource")
    public DataSource dataSource( //
                  @Value("${db.url}") String url, //
                  @Value("${db.driver}") String driver, //
                  @Value("${db.username}") String username, //
                  @Value("${db.password}") String password) {

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setDriverClassName(driver);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);

        basicDataSource.setDefaultAutoCommit(false);
        basicDataSource.setTestOnBorrow(true);
        basicDataSource.setValidationQuery("SELECT 42");
        basicDataSource.setMaxActive(-1);
        basicDataSource.setMaxIdle(-1);
        basicDataSource.setMaxWait(MAX_WAIT);

        return basicDataSource;
    }

    @Bean(name = "base")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory( //
                            @Qualifier("clientSource") DataSource dataSource, //
                            @Value("${db.jpa.dialect}") String dbJpaDialect, //
                            @Value("${db.hibernate.hbm2ddl.auto:}") String dbHibernateHbm2ddlAuto, //
                            @Value("${db.hibernate.show_sql}") boolean dbHibernateShowSql, //
                            @Value("${db.hibernate.format.sql}") boolean dbHibernateFormatSql) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource);
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("basePU");
        localContainerEntityManagerFactoryBean.setPackagesToScan("test.jpa");

        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabasePlatform(dbJpaDialect);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);

        localContainerEntityManagerFactoryBean.setJpaProperties(JpaPropertiesBuilder.jpaProperties() //
                .setDbHibernateEnableLazyLoadNoTrans(false) //
                .setDbHibernateShowSql(dbHibernateShowSql) //
                .setDbHibernateFormatSql(dbHibernateFormatSql) //
                .setDbHibernateHbm2ddlAuto(dbHibernateHbm2ddlAuto) //
                .setDbHibernateDefaultBatchFetchSize(20) //
                .build());

        return localContainerEntityManagerFactoryBean;
    }

    @Bean(name = "transactionManager")
    public JpaTransactionManager getJpaTransactionManager(@Qualifier("base") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);

        return jpaTransactionManager;
    }

    private static class JpaPropertiesBuilder {

        private static final Logger LOGGER = LoggerFactory.getLogger(JpaPropertiesBuilder.class);

        private Properties jpaProperties = new Properties();

        private JpaPropertiesBuilder() {
        }

        static JpaPropertiesBuilder jpaProperties() {
            return new JpaPropertiesBuilder();
        }

        JpaPropertiesBuilder setDbHibernateHbm2ddlAuto(String dbHibernateHbm2ddlAuto) {
            if (dbHibernateHbm2ddlAuto != null && !dbHibernateHbm2ddlAuto.isEmpty()) {
                setProperty(AvailableSettings.HBM2DDL_AUTO, dbHibernateHbm2ddlAuto);
            }
            return this;
        }

        JpaPropertiesBuilder setDbHibernateEnableLazyLoadNoTrans(boolean dbHibernateEnableLazyLoadNoTrans) {
            setBooleanProperty(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, dbHibernateEnableLazyLoadNoTrans);
            return this;
        }

        JpaPropertiesBuilder setDbHibernateShowSql(boolean dbHibernateShowSql) {
            setBooleanProperty(AvailableSettings.SHOW_SQL, dbHibernateShowSql);
            return this;
        }

        JpaPropertiesBuilder setDbHibernateFormatSql(boolean dbHibernateFormatSql) {
            setBooleanProperty(AvailableSettings.FORMAT_SQL, dbHibernateFormatSql);
            return this;
        }

        JpaPropertiesBuilder setDbHibernateDefaultBatchFetchSize(int size) {
            setProperty(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, String.valueOf(size));
            return this;
        }

        Properties build() {
            return jpaProperties;
        }

        private void setBooleanProperty(String name, boolean value) {
            setProperty(name, String.valueOf(value));
        }

        private void setProperty(String name, String value) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Jpa property [{}] is {}", name, value);
            }
            jpaProperties.setProperty(name, value);
        }
    }
}

