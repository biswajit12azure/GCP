package com.khoros.batch.job;

import com.khoros.batch.dto.EmailRequest;
import com.khoros.batch.dto.TimeInterval;
import com.khoros.batch.repo.DBRepository;
import com.khoros.batch.service.CloudRunAuthenticator;
import com.khoros.batch.service.DBConfig;
import com.khoros.batch.service.RestClient;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.LinkedHashMap;

@Service
@Log4j2
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    @Autowired
    private DBRepository dbRepo;

    @Autowired
    private DBConfig dbConfig;

    @Value("${notification.email.url}")
    private String emailURL;


    @Value("${batch.interval.retry.limit}")
    private int batchRetryLimit;

    @Autowired
    private RestClient restClient;

    @Autowired
    private Environment env;

    @Autowired
    private CloudRunAuthenticator authenticator;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {

            ExecutionContext executionContext = jobExecution.getExecutionContext();
            TimeInterval interval = (TimeInterval) executionContext.get("TimeInterval");
            log.info(" Batch job completed for the Interval Id : " + interval);
            if ( interval != null) {
                String tableName = dbConfig.getAuthorTableName(interval.getDate());
                if( dbRepo.getUnProcessedRecordsCount(interval.getId(),tableName) == 0){
                    dbRepo.updateBatchProcessStatus(interval.getId(), "COMPLETED");
                }else{
                    dbRepo.updateBatchProcessStatus(interval.getId(), "ERROR");
                    if(interval.getBatchRetryCount() >= batchRetryLimit){
                        sendEmail(interval);
                    }
                }
            }

        }
    }



    private void sendEmail(TimeInterval interval){
        try{
            String token = authenticator.getIdTokenFromMetadataServer(emailURL);
            restClient.setBearerHeaders(token);
            String message = env.getProperty("notification.email.message");
            message = message.replace("<newline>" , "\n");
            message = message.replace("<interval_id>" , interval.getId() + "");

            EmailRequest request = EmailRequest.builder()
                    .message( message)
                    .subject(env.getProperty("notification.email.subject"))
                    .build();
            restClient.post(emailURL, request, LinkedHashMap.class);
        }catch (Exception ex) {
            log.error("Exception at sendEmail " + ex.getMessage());
        }
    }
}
