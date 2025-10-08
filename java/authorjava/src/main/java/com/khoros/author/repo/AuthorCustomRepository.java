package com.khoros.author.repo;


import com.khoros.author.dto.Author;
import com.khoros.author.dto.TimeInterval;
import com.khoros.author.service.DBConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
@Log4j2
public class AuthorCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${author.table.insert}")
    private String insertSQL;

    @Autowired
    private DBConfig dbConfig;


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment env;


    /**
     * Saves the response records from Khoros API response
     */

    public int[] saveAuthorRecords(List<Author> entities, String timePeriod, TimeInterval interval) {

        try {
            String insertSQLQuery = insertSQL.replaceAll("tablename", dbConfig.getAuthorTableName(interval.getDate()));
            int[] result = jdbcTemplate.batchUpdate(insertSQLQuery, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {


                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());


                    Author entity = entities.get(i);
                    preparedStatement.setString(1, entity.getLithiumUuid());
                    preparedStatement.setString(2, entity.getAuthorNotes());
                    preparedStatement.setString(3, entity.getConversationIds());
                    preparedStatement.setString(4, entity.getUsernames());
                    preparedStatement.setString(5, entity.getTwitterHandles());
                    preparedStatement.setString(6, entity.getFacebookDisplayNames());
                    preparedStatement.setString(7, entity.getGoogleMessengerDisplayNames());
                    preparedStatement.setString(8, entity.getInstagramHandles());
                    preparedStatement.setString(9, entity.getYoutubeHandles());
                    preparedStatement.setString(10, entity.getLinkedinHandles());
                    preparedStatement.setString(11, entity.getSmsHandles());
                    preparedStatement.setString(12, entity.getWechatNames());
                    preparedStatement.setString(13, entity.getStatus());
                    preparedStatement.setString(14, timePeriod);
                    preparedStatement.setTimestamp(15, timestamp);
                    preparedStatement.setString(16, "Author_Component");
                    preparedStatement.setLong(17,interval.getId().longValue());
                }
                @Override
                public int getBatchSize() {
                    return entities.size();
                }

            });

            log.info("Author Record Batch Update Completed  ");

            return result;
        } catch (Exception ex) {
            log.error("Exception at saveAuthorRecords " + ex.getMessage());
            throw ex;
        }
    }




    public TimeInterval getLatestInterval() {
        TimeInterval latestInterval = null;
        try {

            String intervals = env.getProperty("interval.table.records.latest");
            log.debug("Intervals New Records Query : " + intervals);
            latestInterval = jdbcTemplate.query(intervals, new TimeIntervalExtractor());
            log.info("Interval PIcked for Processing : " + latestInterval);
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
                        .authorRetryCount(resultSet.getInt("AUTHOR_RETRY_COUNT"))
                        .build();
            }
            return interval;
        }

    }


    public void updateAuthorStatus(BigInteger id,String Status) {

        try {
            String updateStatus = env.getProperty("interval.table.update");
            jdbcTemplate.update(updateStatus,  Status, new Timestamp(System.currentTimeMillis()), id);
            log.info(" Author Export Status UPDATE completed");

        } catch (Exception ex) {
            log.error("Exception at updateAuthorStatus " + ex.getMessage());
            throw ex;
        }
    }
}
