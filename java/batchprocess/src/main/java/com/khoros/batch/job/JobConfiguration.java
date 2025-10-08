package com.khoros.batch.job;

import com.khoros.batch.dto.Author;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
@EnableBatchProcessing
@Log4j2
public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public ItemReader<Author> dataReader() {
        log.debug("Initializing Batch Data Reader");
        return new DataReader();
    }



    @Bean
    public ItemWriter<Author> writer() {
        return new BatchWriter();
    }


    @Bean
    public Job myJob(JobCompletionNotificationListener listener,Step myStep) {
        return jobBuilderFactory.get("BatchJob")
                .incrementer(new RunIdIncrementer())

                .start(myStep)
                .listener(listener)
                .build();
    }

    /**
     * Chunk Size 20 IS NOT MEANT TO BE CHANGED as only upto 20 conversation ids are supported by Khoros Conversation API
     * Refer documentation for restriction details : https://developer.khoros.com/khoroscaredevdocs/reference/displayidsdisplaynumber-1
     * @param writer
     * @return
     */
    @Bean
    public Step step1(ItemWriter<Author> writer) {
        log.info("Step1 Execution ");
        return stepBuilderFactory.get("step1")
                .<Author, Author>chunk(20)
                .reader(dataReader())
                .writer(writer())
                .build();
    }


}
