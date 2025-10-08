package com.khoros.batch.repo;

import com.khoros.batch.dto.Author;
import com.khoros.batch.dto.TimeInterval;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Log4j2
public class AuthorResultSetExtractor implements ResultSetExtractor<List<Author>> {


    private BigInteger intervalId;

    private int retryCount;

    private Date processDate;

    public AuthorResultSetExtractor(TimeInterval interval) {
        this.processDate = interval.getDate();
        this.intervalId = interval.getId();
        this.retryCount = interval.getBatchRetryCount();
    }

    @Override
    public List<Author> extractData(ResultSet resultSet) throws SQLException {
        List<Author> records = new ArrayList<>();
        while (resultSet.next()) {

            String authorHandles = null;
            authorHandles = resultSet.getString("TWITTER_HANDLES");
            authorHandles = !StringUtils.isNotEmpty(authorHandles) ? resultSet.getString("FACEBOOK_DISPLAY_NAMES") : authorHandles;
            authorHandles = !StringUtils.isNotEmpty(authorHandles) ? resultSet.getString("GOOGLE_MESSENGER_DISPLAY_NAMES") : authorHandles;
            authorHandles = !StringUtils.isNotEmpty(authorHandles) ? resultSet.getString("INSTAGRAM_HANDLES") : authorHandles;
            authorHandles = !StringUtils.isNotEmpty(authorHandles) ? resultSet.getString("YOUTUBE_HANDLES") : authorHandles;
            authorHandles = !StringUtils.isNotEmpty(authorHandles) ? resultSet.getString("LINKEDIN_HANDLES") : authorHandles;
            authorHandles = !StringUtils.isNotEmpty(authorHandles) ? resultSet.getString("SMS_HANDLES") : authorHandles;
            authorHandles = !StringUtils.isNotEmpty(authorHandles) ? resultSet.getString("WECHAT_NAMES") : authorHandles;
            String conversationIds = resultSet.getString("CONVERSATION_IDS");
            conversationIds = conversationIds.replaceAll(" ", "");
            String[] stringArray = conversationIds.split(",");
            List<String> conversationIdsList = Arrays.asList(stringArray);
            String finalAuthorHandles = authorHandles;

            conversationIdsList.forEach(conversationId -> {
                try{
                    ArrayList<String> conversationList = new ArrayList<>(1);
                    conversationList.add(conversationId);
                    Author author = Author.builder().timePeriod(resultSet.getString("TIME_PERIOD"))
                            .lithiumUuid(resultSet.getString("LITHIUM_UUID"))
                            .authorNotes(resultSet.getString("AUTHOR_NOTES"))
                            .conversationIds(conversationList)
                            .usernames(resultSet.getString("USERNAMES"))
                            .authorHandle(finalAuthorHandles)
                            .intervalId(intervalId)
                            .processDate(processDate)
                            .batchRetryCount(retryCount)
                            .build();
                    records.add(author);
                }catch (Exception e){
                    log.error("Exception in extractData of AuthorResultSetExtractor : " + e.getMessage());
                    e.printStackTrace();
                }

            });


        }
        return records;
    }
}