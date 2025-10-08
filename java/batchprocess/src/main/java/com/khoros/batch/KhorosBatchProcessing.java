package com.khoros.batch;

import com.khoros.batch.config.CustomIncrementer;
import com.khoros.batch.config.RequestResponseLoggingInterceptor;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@SpringBootApplication
public class KhorosBatchProcessing {




    public static void main(String[] args) {
        SpringApplication.run(KhorosBatchProcessing.class, args);
    }


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        restTemplate.getInterceptors().add(new RequestResponseLoggingInterceptor());
        return restTemplate;
    }

    /**
     * Spring batch uses default set of DB tables to store and use batch execution metadata.
     * Overriding default Spring batch table primary key incrementer to use SQL Primary key sequence to
     * avoid deadlocks during batch processes
     * @param dataSource
     * @return
     */
    @Bean
    public BatchConfigurer batchConfigurer(DataSource dataSource) {
        return new DefaultBatchConfigurer(dataSource) {
            @Override
            protected JobRepository createJobRepository() throws Exception {
                JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
                factory.setDataSource(dataSource);
                factory.setTransactionManager(getTransactionManager());
                factory.setIncrementerFactory(new CustomIncrementer(dataSource));
                factory.afterPropertiesSet();
                return factory.getObject();
            }
        };
    }

}
