package com.playvox.batch.service;


import com.playvox.batch.dto.PlayvoxRecord;
import com.playvox.batch.dto.TimeInterval;
import com.playvox.batch.repo.DBRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class StatusService {


    @Autowired
    private DBRepository customRepository;


    @Autowired
    private InteractionService interactionService;

    /**
     * 1.Picks a Time interval to process.
     * 2.Invokes Playvox Status API to get Status of all conversations Posted corresponding to that interval
     * 3.Updates the Status Response in table and sends notification mail incase of failed conversations identified.
     */
    public void fetchAndUpdateStatus() {

        TimeInterval intervalProcessed = null;
        try {

            intervalProcessed = customRepository.getLatestInterval();
            List<PlayvoxRecord> records = getIntervalPlayvoxRecords(intervalProcessed);
            long startTime = System.nanoTime();
            records.stream().forEach(playvox -> {
                interactionService.getInteractionStatus(playvox.getPlayvoxId());
            });
            long endTime = System.nanoTime();
            double timeTakenSeconds = (endTime - startTime) / 1_000_000_000.0;
            log.info("Time taken to get Status of all Playvox Ids: " + timeTakenSeconds + " seconds for Interval : " + intervalProcessed.getId());
            log.info("Time taken list size: " + records.size());
            customRepository.updatePlayvoxBatchProcessStatus(intervalProcessed.getId(), "COMPLETED");

        } catch (Exception ex) {
            if (intervalProcessed != null && intervalProcessed.getId() != null) {
                customRepository.updatePlayvoxBatchProcessStatus(intervalProcessed.getId(), "ERROR");
            }
            log.error("Exception at Playvox Batch Process .Error Message : " + ex.getMessage());
        }
    }


    public List<PlayvoxRecord> getIntervalPlayvoxRecords(TimeInterval intervalProcessed) throws Exception {
        List<PlayvoxRecord> entities = null;
        try {

            log.info("Intervl Picked to process : " + intervalProcessed.getId());
            entities = customRepository.getNewRecordsToProcess(intervalProcessed.getId());
            if (CollectionUtils.isNotEmpty(entities)) {
                customRepository.updatePlayvoxBatchProcessStatus(intervalProcessed.getId(), "PROCESSING");
            }
        } catch (Exception ex) {
            log.error("Exception at Batch Process .Error Message : " + ex.getMessage());
            throw ex;
        }
        return entities;
    }


}
