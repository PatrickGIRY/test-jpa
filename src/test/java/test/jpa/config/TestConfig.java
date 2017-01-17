package test.jpa.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import test.jpa.util.DBUnitForJpaHelper;


@Configuration
@Import({BaseConfig.class, DBUnitForJpaHelper.class})
@PropertySource(value = {"classpath:test.properties"}, ignoreResourceNotFound = true)
public class TestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}

