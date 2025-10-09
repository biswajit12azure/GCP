package com.playvox.batch.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playvox.batch.dto.EmailRequest;
import com.playvox.batch.dto.json.InteractionResultDto;
import com.playvox.batch.repo.DBRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
public class InteractionService {

    @Autowired
    RestClient restClient;

    @Value("${playvox.api.status.url}")
    private String interactionStatusURL;

    @Value("${playvox.api.auth.username}")
    private String user;

    @Value("${playvox.api.auth.password}")
    private String pwd;


    @Value("${notification.email.url}")
    private String emailURL;

    @Value("${notification.email.targets}")
    private String emailTarget;
    @Autowired
    private DBRepository dbRepository;


    @Autowired
    private CloudRunAuthenticator authenticator;

    public String getInteractionStatus(String playvoxId) {

        try {
            restClient.setHeaders(user, pwd);
            String statusURL = interactionStatusURL + playvoxId;
            LinkedHashMap statusResponse = restClient.sendRequest(statusURL, HttpMethod.GET, LinkedHashMap.class);
            JSONObject statusJson = new JSONObject(statusResponse);
            updateInteractionStatusResponse(statusJson, playvoxId);
            log.debug(" Interaction Status Response json : " + statusJson);

        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException at postInteraction : " + e.getMessage());
        } catch (Exception ex) {
            log.error("Exception at getInteractionStatus" + ex.getStackTrace().toString());
            throw ex;
        }

        return null;
    }


    /**
     * Parse Playvox status API response and update the response in Table.Sends mail notification incase of failed records.
     *
     * @param statusJson
     * @param playvoxId
     * @throws JsonProcessingException
     */
    public void updateInteractionStatusResponse(JSONObject statusJson, String playvoxId) throws JsonProcessingException {
        try {
            if (statusJson != null && statusJson.getBoolean("success")) {
                statusJson = statusJson.getJSONObject("result");
                ObjectMapper objectMapper = new ObjectMapper();
                InteractionResultDto resultDto = objectMapper.readValue(statusJson.toString(), InteractionResultDto.class);
                List<String> failedconversationIdList = new ArrayList<>();

                if (resultDto.getStatus().equalsIgnoreCase("completed")) {
                    List<InteractionResultDto.InteractionStatus> failed = resultDto.getFailed();
                    //Parsing and saving failed conversations
                    if (CollectionUtils.isNotEmpty(failed)) {
                        failed.stream().forEach(obj -> {
                            failedconversationIdList.add(obj.getInteraction_id());
                            log.info("Failed Conversation from Playvox Status API response : " + obj);
                        });
                    }
                    String failedIds = CollectionUtils.isEmpty(failedconversationIdList) ? null : String.join(",", failedconversationIdList);
                    boolean failureEmailSent = false;
                    //Email Notification
                    if (!StringUtils.isEmpty(failedIds) && !StringUtils.isEmpty(emailTarget)) {
                        List<String> receivers = Arrays.asList(emailTarget.split(","));
                        try {
                            String token = authenticator.getIdTokenFromMetadataServer(emailURL);
                            restClient.setBearerHeaders(token);
                            EmailRequest request = EmailRequest.builder()
                                    .message("Hi Team, \nFollowing conversation Ids have failed while trying to Post/Save in Playvox : " + failedIds + ".\n Please refer INTERACTION_STATUS table record id " + playvoxId + " for further details.\n \n  Note: This is an auto generated email. Please do not reply. ")
                                    .subject("Spotify : Playvox & Khoros Integration - Failed Records")
                                    .receiverList(receivers)
                                    .build();
                            restClient.post(emailURL, request, LinkedHashMap.class);
                            failureEmailSent = true;
                        } catch (Exception e) {
                            log.error("Exception while trying to send Email: " + e.getMessage());
                        }
                    }
                    dbRepository.updateAPIResponse(playvoxId, statusJson.toString(), failureEmailSent);


                }
            }
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException at updateInteractionStatusResponse : " + e.getMessage());
            throw e;
        } catch (Exception ex) {

            log.error("Exception at updateInteractionStatusResponse" + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }
}
