package com.khoros.batch.repo;


import com.khoros.batch.dto.ActionLog;
import com.khoros.batch.dto.Author;
import com.khoros.batch.dto.TimeInterval;
import com.khoros.batch.service.DBConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Repository
@Log4j2
public class DBRepository {

    @PersistenceContext
    private EntityManager entityManager;


    @Autowired
    private DBConfig dbScheduler;


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @Autowired
    private Environment env;


    public List<Author> getNewRecordsToProcess(String authorTableName, TimeInterval interval) {
        try {

            String newRecords = env.getProperty("author.table.new.records");
            newRecords = newRecords.replaceAll("tablename", authorTableName);
            log.debug("Author New Records Query : " + newRecords);

            List<Author> authors = jdbcTemplate.query(newRecords, new Object[]{interval.getId()}, new AuthorResultSetExtractor(interval));

            return authors;
        } catch (Exception ex) {
            log.error("Exception at getNewRecordsToProcess " + ex.getMessage());
            throw ex;
        }

    }


    public List<String> getActionLogs(BigInteger intervalId, String conversationId, String timePeriod, String logTableName) {
        try {

            String getActionLogs = env.getProperty("conversation.table.actionlogs");
            getActionLogs = getActionLogs.replaceAll("tablename", logTableName);
            log.debug("Get ActionLogs Query : " + getActionLogs);

            List<String> logs = jdbcTemplate.query(getActionLogs, new Object[]{intervalId, timePeriod, conversationId}, new ActionLogExtractor());
            return logs;

        } catch (Exception ex) {
            log.error("Exception at getNewRecordsToProcess " + ex.getMessage());
            throw ex;
        }

    }

    public Map<String,String> getActionLogsV2(BigInteger intervalId, List<String> conversationIds,   String logTableName) {
        try {

            String getActionLogs = env.getProperty("conversation.table.actionlogsV2");
            getActionLogs = getActionLogs.replaceAll("tablename", logTableName);
            getActionLogs = getActionLogs.replaceAll("idslist",String.join(",", conversationIds));
            log.debug("Get ActionLogs Query : " + getActionLogs);

            Map<String,String> logs = jdbcTemplate.query(getActionLogs, new Object[]{intervalId}, new ActionLogExtractor2());
            return logs;

        } catch (Exception ex) {
            log.error("Exception at getNewRecordsToProcess " + ex.getMessage());
            throw ex;
        }

    }

    private class ActionLogExtractor2 implements ResultSetExtractor<Map<String,String>> {
        @Override
        public Map<String,String> extractData(ResultSet resultSet) throws SQLException {

            Map<String,String> actionLogMap  = new HashMap<>();

            while (resultSet.next()) {
                String log = ! StringUtils.isEmpty(resultSet.getString("CONCATENATED_ACTIONS")) ?
                        "\n \u2022" + resultSet.getString("CONCATENATED_ACTIONS").replaceAll("\n" , "\n \u2022") : null;
                actionLogMap.put(resultSet.getString("CONVERSATION_ID"),log);
            }
            return actionLogMap;
        }
    }


    private class ActionLogExtractor implements ResultSetExtractor<List<String>> {
        @Override
        public List<String> extractData(ResultSet resultSet) throws SQLException {
            List<String> records = new ArrayList<>();
            while (resultSet.next()) {

                ActionLog log = ActionLog.builder().conversationId(resultSet.getString("CONVERSATION_ID"))
                        .actionPerformed(resultSet.getString("ACTION_PERFORMED"))
                        .actionPerformedDate(resultSet.getDate("ACTION_PERFORMEDDATE"))
                        .historyAction(resultSet.getString("HISTORY_ACTION"))
                        .actingAgent(resultSet.getString("ACTING_AGENT"))
                        .actingAgentEmail(resultSet.getString("ACTING_AGENTEMAIL"))
                        .workqueueCurrent(resultSet.getString("WORKQUEUE_CURRENT"))
                        .tags(resultSet.getString("TAGS"))
                        .comment(resultSet.getString("COMMENT"))
                        .note(resultSet.getString("NOTE"))
                        .build();
                if (log.getActionPerformed().contains("Tag Attached")) {
                    String actionLog = log.getActingAgent() + " Attached tag : " + log.getTags() + " at " + log.getActionPerformedDate() + "\n";
                    records.add(actionLog);
                } else {
                    String actionLog = " " + log.getActingAgent() + " " + log.getActionPerformed() + " " + log.getHistoryAction() + "\n";
                    records.add(actionLog);
                }
            }
            return records;
        }
    }

    public void updateStatus(HashMap<String, Author> map, String playvoxId, Date processDate) {

        try {

            String updateSQL = env.getProperty("author.table.update");
            updateSQL = updateSQL.replaceAll("tablename", dbScheduler.getAuthorTableName(processDate));

            jdbcTemplate.batchUpdate(updateSQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {


                    Map.Entry<String, Author> entry = map.entrySet().stream().skip(i).findFirst().orElse(null);

                    if (entry != null) {
                        Author author = entry.getValue();
                        String playvoxStatusId =  StringUtils.isEmpty(author.getRecordStatus()) ? playvoxId : null;
                        String recordStatus = StringUtils.isEmpty(author.getRecordStatus()) ? "COMPLETED" : author.getRecordStatus();
                        ps.setString(1, playvoxStatusId);
                        ps.setString(2,"%" + playvoxStatusId + "%"  );
                        ps.setString(3, playvoxStatusId);
                        ps.setString(4, recordStatus);
                        ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                        ps.setString(6, "Batch_Component");
                        ps.setString(7, author.getQueueId());
                        ps.setString(8, author.getTimePeriod());
                        ps.setString(9, author.getLithiumUuid());
                    }
                }

                @Override
                public int getBatchSize() {
                    return map.size();
                }
            });
            log.info("Author Records Update Status Completed");


        } catch (Exception ex) {
            log.error("Exception at updateStatus " + ex.getMessage());
            throw ex;
        }
    }

    public void savePlayvoxId(BigInteger intervalId,String playvoxId) {

        try {
            String insertSQL = env.getProperty("playvox.table.insert");
            jdbcTemplate.update(insertSQL,intervalId, playvoxId, "NEW", new Timestamp(System.currentTimeMillis()), "Batch_Component");
            log.info(" Playvox Status Table insert completed");

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
                        .batchRetryCount(resultSet.getInt("BATCH_RETRY_COUNT"))
                        .build();
            }
            return interval;
        }

    }

    public void updateBatchProcessStatus(BigInteger id, String status) {

        try {
            String updateStatus = env.getProperty("interval.table.update");
            jdbcTemplate.update(updateStatus, status, new Timestamp(System.currentTimeMillis()), id);
            log.info(" Batch Process Status update completed");

        } catch (Exception ex) {
            log.error("Exception at updateBatchProcessStatus " + ex.getMessage());
            throw ex;
        }
    }


    public int getUnProcessedRecordsCount(BigInteger intervalId,String tableName) {
        try {

            String getUnProcessedRecordsCount = env.getProperty("author.table.unprocessedRecords");
            getUnProcessedRecordsCount = getUnProcessedRecordsCount.replaceAll("tablename" , tableName);
            log.debug("Get getUnProcessedRecordsCount Query : " + getUnProcessedRecordsCount);
            return jdbcTemplate.queryForObject(getUnProcessedRecordsCount,new Object[]{intervalId} , Integer.class);

        } catch (Exception ex) {
            log.error("Exception at getUnProcessedRecordsCount " + ex.getMessage());
            throw ex;
        }
    }

}
