package com.spotify.support.repo;


import com.spotify.support.dto.ConversationData;
import com.spotify.support.dto.IntervalData;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Log4j2
public class DBRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment env;


    public void resetErrorRecords() {

        try {

            String updateSQL = env.getProperty("actionlog.status.reset");
            jdbcTemplate.update(updateSQL);
            updateSQL = env.getProperty("author.status.reset");
            jdbcTemplate.update(updateSQL);
            updateSQL = env.getProperty("batch.status.reset");
            jdbcTemplate.update(updateSQL);

            updateSQL = env.getProperty("playvox.status.reset");
            jdbcTemplate.update(updateSQL);
            log.info("Error Records Reset completed");


        } catch (Exception ex) {
            log.error("Exception at updateStatus " + ex.getMessage());
            throw ex;
        }
    }


    public ConversationData getAuthorRecordsReport() {
        try {

            String date = env.getProperty("report.date");

            date = jdbcTemplate.queryForObject(date, String.class);
            String tableSuffix = date.replaceAll("-", "_");
            String reportQuery = env.getProperty("report.author.records");
            reportQuery = reportQuery.replaceAll("_date", tableSuffix);

            log.debug("Get AuthorRecords Report Query : " + reportQuery);
            ConversationData data = jdbcTemplate.query(reportQuery, new EmailBody());
            data.setDate(date);
            return data;

        } catch (Exception ex) {
            log.error("Exception at getAuthorRecordsReport " + ex.getMessage());
            throw ex;
        }

    }

    public List<IntervalData> getIntervalReport() {
        try {
            String query = env.getProperty("report.interval.records");

            List<IntervalData> spotifyDataList = jdbcTemplate.query(query, (rs, rowNum) -> {
                IntervalData spotifyData = new IntervalData();
                spotifyData.setId(rs.getLong("ID"));
                spotifyData.setStartTime(rs.getTimestamp("START_TIME"));
                spotifyData.setEndTime(rs.getTimestamp("END_TIME"));
                spotifyData.setActionLogStatus(rs.getString("ACTION_LOG_STATUS"));
                spotifyData.setAuthorStatus(rs.getString("AUTHOR_STATUS"));
                spotifyData.setBatchProcessStatus(rs.getString("BATCH_PROCESS_STATUS"));
                return spotifyData;
            });

            return spotifyDataList;
        } catch (Exception ex) {
            log.error("Exception at getIntervalReport " + ex.getMessage());
            throw ex;
        }

    }


    private class EmailBody implements ResultSetExtractor<ConversationData> {
        @Override
        public ConversationData extractData(ResultSet resultSet) throws SQLException {
            if (resultSet.next()) {
                ConversationData data =ConversationData.builder()
                        .totalCount(resultSet.getLong("TotalCount"))
                        .completedCount(resultSet.getLong("CompletedCount"))
                        .QueueNotConfiguredCount(resultSet.getLong("QueueNotConfiguredCount"))
                        .processingCount(resultSet.getLong("ProcessingCount"))
                        .newStatusCount(resultSet.getLong("NewCount"))
                        .build();
               return data;
            }
            return null;
        }
    }



}
