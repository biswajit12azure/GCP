package com.khoros.batch.service;


import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class BatchService {

    @Autowired
    private RestClient restClient;

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job processJob;

    /**
     * 1.Picks a Time interval (for which Action Log Status & Author Status = 'Completed') to process.
     * 2.Fetches all conversations to be processed for that record.
     * 3.Uses Spring Batch to Process the records in specified chunk size.
     * 4.For Each chunk Size Khoros Conversation API is invoked,action logs are populated and then posted to playvox.
     */
    public void batchProcess() {
        try {


            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(processJob, jobParameters);


        } catch (JobExecutionAlreadyRunningException e) {
            throw new RuntimeException(e);
        } catch (JobRestartException e) {
            throw new RuntimeException(e);
        } catch (JobInstanceAlreadyCompleteException e) {
            throw new RuntimeException(e);
        } catch (JobParametersInvalidException e) {
            throw new RuntimeException(e);
        } catch (Exception ex) {
            log.error("Exception at Batch Process .Error Message : " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }


}
