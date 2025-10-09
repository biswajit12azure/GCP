package com.spotify.support.service;


import com.spotify.support.dto.ConversationData;
import com.spotify.support.dto.EmailRequest;
import com.spotify.support.dto.IntervalData;
import com.spotify.support.repo.DBRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@Log4j2
public class MonitorService {

    @Autowired
    private DBRepository dbRepo;

    @Autowired
    private RestClient restClient;

    @Autowired
    private Environment env;


    @Value("${notification.email.url}")
    private String emailURL;

    @Autowired
    private CloudRunAuthenticator authenticator;


    /**
     * Looks for the Records with ACTION_LOG_STATUS = 'ERROR'  & AUTHOR_STATUS = 'ERROR' in master table
     * resets them to "NEW" status to try for re-processing.
     */
    public void resetErrorRecords() {
        try {
            dbRepo.resetErrorRecords();

        } catch (Exception ex) {
            log.error("Exception at reset Error Records Process .Error Message : " + ex.getStackTrace());

        }
    }


    public void sendReportEmail() {
        try {
            ConversationData authorReport = dbRepo.getAuthorRecordsReport();
            List<IntervalData> list = dbRepo.getIntervalReport();
            String intervalReport = generateHtmlEmailContent(authorReport, list);

            sendEmail(intervalReport);
        } catch (Exception ex) {
            log.error("Exception at sendReportEmail .Error Message : " + ex.getStackTrace());
        }
    }

    private void sendEmail(String message) {
        try {
            String token = authenticator.getIdTokenFromMetadataServer(emailURL);
            restClient.setBearerHeaders(token);
            EmailRequest request = EmailRequest.builder()
                    .message(message)
                    .subject(env.getProperty("notification.email.subject"))
                    .build();
            restClient.post(emailURL, request, LinkedHashMap.class);
        } catch (Exception ex) {
            log.error("Exception at sendEmail " + ex.getMessage());
        }
    }

    public String generateHtmlEmailContent(ConversationData authorReport, List<IntervalData> list) {

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html lang=\"en\">");
        htmlBuilder.append("\n\n <body style=\"font-family: verdana, serif\">");
        htmlBuilder.append("\n <p>");
        htmlBuilder.append("\n  Hi Team, <br>");
        htmlBuilder.append("\n  Daily Summary Report of conversation status in Author table (" + authorReport.getDate() + ") are as follows : <br>");
        htmlBuilder.append("\n\t<b>Total Count : </b> " + authorReport.getTotalCount() + " <br>");
        htmlBuilder.append("\n\tRecords with Completed Status :  " + authorReport.getCompletedCount() + " <br>");
        htmlBuilder.append("\n\tRecords with Processing Status : " + authorReport.getProcessingCount() + " <br>");
        htmlBuilder.append("\n\tRecords with Queue_Not_Configured Status : " + authorReport.getQueueNotConfiguredCount() + " <br>");
        htmlBuilder.append("\n\tRecords with New Status : " + authorReport.getNewStatusCount() + " <br>");

        if (!CollectionUtils.isEmpty(list)) {
            htmlBuilder.append("\n\n Daily Summary Report of Unprocessed Intervals are as follows: ,<br>");
            htmlBuilder.append("\n </p>");
            htmlBuilder.append("\n<table border=1 style=\"border-collapse: collapse\">");
            htmlBuilder.append("\n <tr>");
            htmlBuilder.append("\n\t<th>ID</th>");
            htmlBuilder.append("\n\t<th>Start Time</th>");
            htmlBuilder.append("\n\t<th>End Time</th>");
            htmlBuilder.append("\n\t<th>Action Log Status</th>");
            htmlBuilder.append("\n\t<th>Author Status</th>");
            htmlBuilder.append("\n\t<th>Batch Process Status</th>");
            htmlBuilder.append("\n </tr>");

            list.stream().forEach(interval -> {
                htmlBuilder.append("\n\t<tr align=\"center\">");
                htmlBuilder.append("\n\t\t<td>" + interval.getId() + "</td>");
                htmlBuilder.append("\n\t\t<td>" + interval.getStartTime() + "</td>");
                htmlBuilder.append("\n\t\t<td>" + interval.getEndTime() + "</td>");
                htmlBuilder.append("\n\t\t<td>" + interval.getActionLogStatus() + "</td>");
                htmlBuilder.append("\n\t\t<td>" + interval.getAuthorStatus() + "</td>");
                htmlBuilder.append("\n\t\t<td>" + interval.getBatchProcessStatus() + "</td>");
                htmlBuilder.append("\n\t</tr>");
            });
            htmlBuilder.append("\n</table>");
        }

        htmlBuilder.append("\n\n<br>Records with <b>\"ERROR\"</b>  status will be attempted to re-process again by reset service.");
        htmlBuilder.append("\n\n<br><b>Note: </b> This is an auto generated email. Please do not reply");
        htmlBuilder.append("\n </body>");
        htmlBuilder.append("\n</html>");

        return htmlBuilder.toString();
    }


}