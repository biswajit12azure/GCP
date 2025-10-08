package com.khoros.batch.job;

import com.khoros.batch.dto.Author;
import com.khoros.batch.dto.TimeInterval;
import com.khoros.batch.repo.DBRepository;
import com.khoros.batch.service.DBConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Service
@Log4j2
public class DataReader implements ItemReader<Author>, StepExecutionListener {

    @Autowired
    private DBConfig dbConfig;

    @Autowired
    private DBRepository customRepository;

    private List<Author> entities;

    private int currentIndex;


    private TimeInterval intervalProcessed;


    private Date processDate;

    @Autowired
    public DataReader() {
        this.currentIndex = 0;
    }


    /**
     * 1.Picks a Time interval (for which Action Log Status & Author Status = 'Completed') to process.
     * 2.Fetches all conversations to be processed for that record
     */
    @Override
    public Author read() throws Exception {
        if (CollectionUtils.isEmpty(entities)) {
            log.debug("Fetching Data Batch from Database");

            intervalProcessed = customRepository.getLatestInterval();
            log.info("Interval picked to Process : " + intervalProcessed);
            BigInteger intervalId = intervalProcessed.getId();
            String authorTableName = dbConfig.getAuthorTableName(intervalProcessed.getDate());
            entities = customRepository.getNewRecordsToProcess(authorTableName, intervalProcessed);
            if (CollectionUtils.isNotEmpty(entities)) {
                customRepository.updateBatchProcessStatus(intervalId, "PROCESSING");
            }
        }
        int entitiesSize = CollectionUtils.isEmpty(entities) ? 0 : entities.size();
        Author entity = null;
        if (currentIndex < entitiesSize) {
            entity = entities.get(currentIndex);
            log.debug("Batch Processing Index : " + currentIndex);
            currentIndex++;
        }
        return entity;
    }


    @Override
    public void beforeStep(StepExecution stepExecution) {

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
