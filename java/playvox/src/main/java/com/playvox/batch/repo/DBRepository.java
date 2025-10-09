package com.playvox.batch.repo;


import com.playvox.batch.dto.PlayvoxRecord;
import com.playvox.batch.dto.TimeInterval;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
@Log4j2
public class DBRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment env;

    public List<PlayvoxRecord> getNewRecordsToProcess(BigInteger intervalId) {
        try {

            String newRecords = env.getProperty("interaction.table.new.records");

            log.debug("Interaction Status New Records Query : " + newRecords);

            List<PlayvoxRecord> playvoxIds = jdbcTemplate.query(newRecords, new Object[]{intervalId}, new InteractionResultSetExtractor(intervalId));
            return playvoxIds;
        } catch (Exception ex) {
            log.error("Exception at getNewRecordsToProcess " + ex.getMessage());
            throw ex;
        }

    }

    public void updatePlayvoxBatchProcessStatus(BigInteger id, String status) {

        try {
            String updateStatus = env.getProperty("interval.table.update");
            jdbcTemplate.update(updateStatus, status, new Timestamp(System.currentTimeMillis()), id);
            log.info(" Playvox Batch Process Status update completed");

        } catch (Exception ex) {
            log.error("Exception at updatePlayvoxBatchProcessStatus " + ex.getMessage());
            throw ex;
        }
    }

    private class InteractionResultSetExtractor implements ResultSetExtractor<List<PlayvoxRecord>> {

        private BigInteger intervalId;

        public InteractionResultSetExtractor(BigInteger intervalId) {
            this.intervalId = intervalId;
        }

        @Override
        public List<PlayvoxRecord> extractData(ResultSet resultSet) throws SQLException {
            List<PlayvoxRecord> records = new ArrayList<>();
            while (resultSet.next()) {
                records.add(PlayvoxRecord.builder()
                        .intervalId(intervalId)
                        .playvoxId(resultSet.getString("PLAYVOX_ACKNOWLEDGEMENT_ID"))
                        .build());
            }
            return records;
        }
    }


    public void updateAPIResponse(String playvoxId, String response,boolean failure) {

        try {

            String updateSQL = env.getProperty("interaction.table.update");
            jdbcTemplate.update(updateSQL, "COMPLETED", response, new Timestamp(System.currentTimeMillis()), "Playvox_Batch_Component",failure, playvoxId);
            log.info(" Playvox Status Table Update completed");

        } catch (Exception ex) {
            log.error("Exception at updateStatus " + ex.getMessage());
            throw ex;
        }
    }


    public TimeInterval getLatestInterval() {
        TimeInterval latestInterval = null;
        try {

            String intervals = env.getProperty("interval.table.records.latest");
            log.debug("Intervals New Records Query : " + intervals);
            latestInterval = jdbcTemplate.query(intervals, new TimeIntervalExtractor());


            log.info("I latestInterval: " + latestInterval);

            return latestInterval;
        } catch (Exception ex) {
            log.error("Exception at getLatestInterval " + ex.getMessage());
            throw ex;
        }

    }

    private class TimeIntervalExtractor implements ResultSetExtractor<TimeInterval> {
        @Override
        public TimeInterval extractData(ResultSet resultSet) throws SQLException {
            TimeInterval interval = null;
            if (resultSet.next()) {
                interval = TimeInterval.builder()
                        .id(BigInteger.valueOf(resultSet.getLong("ID")))
                        .startTimeEpoch(resultSet.getString("START_TIME_EPOCH"))
                        .endTimeEpoch(resultSet.getString("END_TIME_EPOCH"))
                        .startTime(resultSet.getTimestamp("START_TIME"))
                        .endTime(resultSet.getTimestamp("END_TIME"))
                        .date(resultSet.getDate("PROCESS_DATE"))
                        .build();
            }
            return interval;
        }

    }




}
