package com.khoros.author;

import com.khoros.author.config.CustomRetryPolicy;
import com.khoros.author.config.RequestResponseLoggingInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
public class KhorosAuthor {


    @Value("${retry.policy.timeperiod}")
    private long timePeriod;

    @Value("${retry.policy.count}")
    private int  retriesCount;


    public static void main(String[] args) {
        SpringApplication.run(KhorosAuthor.class, args);
    }


    @Bean
    public RetryTemplate retryTemplate() {
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(timePeriod);
        RetryTemplate template = new RetryTemplate();
        template.setBackOffPolicy(backOffPolicy);
        template.setRetryPolicy(new CustomRetryPolicy(retriesCount));
        return template;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        restTemplate.getInterceptors().add(new RequestResponseLoggingInterceptor());
        return restTemplate;
    }

}
