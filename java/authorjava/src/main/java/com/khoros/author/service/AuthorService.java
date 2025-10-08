package com.khoros.author.service;


import com.khoros.author.config.UnAvailableException;
import com.khoros.author.dto.Author;
import com.khoros.author.dto.EmailRequest;
import com.khoros.author.dto.TimeInterval;
import com.khoros.author.repo.AuthorCustomRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@Log4j2
public class AuthorService {

    @Autowired
    private RestClient restClient;

    @Autowired
    private Downloader downloader;


    @Autowired
    private RetryTemplate retryTemplate;

    @Value("${khoros.api.authorexport.url}")
    private String authorURL;


    @Value("${khoros.api.auth.username}")
    private String authUser;

    @Value("${khoros.api.auth.password}")
    private String authPwd;

    @Value("${khoros.interval.retry.limit}")
    private int intervalRetryLimit;

    @Value("${notification.email.url}")
    private String emailURL;


    @Autowired
    private Environment env;


    @Autowired
    private DBConfig dbConfig;

    @Autowired
    private AuthorCustomRepository customRepository;

    @Autowired
    private CloudRunAuthenticator authenticator;


    /**
     * 1.Picks a Time interval to process.
     * 2.Invokes Khoros Raw Author Export API for that interval
     * 3. Saves the records received in dynamic Table created for each Process date. Table name pattern : AUTHOR_dd_mm_yyyy
     */
    public void authorExport() {

        String timeInterval = null;
        TimeInterval interval = null;
        try {
            interval = pickIntervalToProcess();
            if (interval != null) {
                invokeKhorosAndSaveRecords(interval);
            }


        } catch (Exception ex) {
            log.error("Exception at authorExport for Time Interval : " + timeInterval + "Error Message : " + ex.getMessage());
            log.error("Stacktrace : " + ex);
            if (interval != null) {
                customRepository.updateAuthorStatus(interval.getId(), "ERROR");
                if(interval.getAuthorRetryCount() >= intervalRetryLimit){
                    sendEmail(interval);
                }
            }
        }
    }


    /**
     * Khoros API invocation and records insertion
     * @param interval
     * @throws UnAvailableException
     */
    public void invokeKhorosAndSaveRecords(TimeInterval interval) throws UnAvailableException {
        String timeInterval = null;
        try {
            String startTime = interval.getStartTimeEpoch();
            String endTime = interval.getEndTimeEpoch();
            timeInterval = startTime + "-" + endTime;

            customRepository.updateAuthorStatus(interval.getId(), "PROCESSING");
            String authorReportURL = authorURL + "&startTime=" + startTime + "&endTime=" + endTime;
            restClient.setHeaders(authUser, authPwd);
            LinkedHashMap response = restClient.sendRequest(authorReportURL, HttpMethod.GET, LinkedHashMap.class);
            JSONObject json = new JSONObject(response);
            log.debug("Author Export API Response json : " + json);

            json = json.getJSONObject("result");
            String statusURL = json.getString("statusUrl");
            log.debug("Status URL for Time Interval (" + timeInterval + " ) : " + statusURL);
            //Status URL to be invoked to get Download URl
            if (StringUtils.isNotEmpty(statusURL)) {
                String downloadURL = downloader.getAuthorExportDownloadURL(statusURL, restClient);
                log.info("Download URL for Time Interval (" + timeInterval + " ) : " + downloadURL);
                List<Author> authorEntities = downloader.invokeDownloadURL(downloadURL, restClient);
                if (!CollectionUtils.isEmpty(authorEntities)) {
                    log.info(" No .of Records received for Time Interval   " + timeInterval + " : " + authorEntities.size());
                    ///Dynamic table creation happens if table doesn't exist for the interval's corresponding date
                    dbConfig.createTableForDate(interval.getDate());
                    int[] batchResult = customRepository.saveAuthorRecords(authorEntities, timeInterval, interval);
                    log.info("Author Record Batch Update Result : " + batchResult);
                } else {
                    log.info("No Records received for Time Interval : " + timeInterval);
                }
                customRepository.updateAuthorStatus(interval.getId(), "COMPLETED");
            }
        } catch (Exception ex) {
            log.error("Exception at invokeKhorosAndSaveRecords for Time Interval : " + interval + "Error Message : " + ex.getMessage());
            log.error("Stacktrace : " + ex);
            throw ex;
        }
    }

    private TimeInterval pickIntervalToProcess() {
        try {
            TimeInterval latestInterval = customRepository.getLatestInterval();
            return latestInterval;
        } catch (Exception ex) {
            log.error("Exception at pickIntervalToProcess " + ex.getMessage());
            throw ex;
        }

    }


    private void sendEmail(TimeInterval interval){
        try{
            String token = authenticator.getIdTokenFromMetadataServer(emailURL);
            restClient.setBearerHeaders(token);
            String message = env.getProperty("notification.email.message");
            message = message.replace("<newline>" , "\n");
            message = message.replace("<interval_id>" , interval.getId() + "");
            message = message.replace("<StartTimeEpoch>" , interval.getStartTimeEpoch());
            message = message.replace("<EndTimeEpoch>" , interval.getEndTimeEpoch());

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
