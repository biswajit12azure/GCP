package com.khoros.batch.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khoros.batch.dto.Author;
import com.khoros.batch.dto.Interaction;
import com.khoros.batch.dto.TimeInterval;
import com.khoros.batch.repo.DBRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class InteractionService {

    @Autowired
    RestClient restClient;

    @Value("${playvox.api.interaction.url}")
    private String interactionURL;


    @Value("${playvox.api.auth.username}")
    private String user;

    @Value("${playvox.api.auth.password}")
    private String pwd;

    @Autowired
    private DBRepository dbRepository;

    public LinkedHashMap postInteraction(List<Interaction> interactions, HashMap<String, Author> authorRecords, TimeInterval interval) {

        try {
            JSONObject json = null;
            if (CollectionUtils.isNotEmpty(interactions)) {
                restClient.setHeaders(user, pwd);

                ObjectMapper objectMapper = new ObjectMapper();
                String requestBody = objectMapper.writeValueAsString(interactions);
                int requestSizeInBytes = requestBody.getBytes().length;

                log.debug("Play API Request Size (bytes): " + requestSizeInBytes);

                LinkedHashMap response = restClient.post(interactionURL, interactions, LinkedHashMap.class);
                JSONObject responseJson = new JSONObject(response);
                updateInteractionResponse(responseJson, authorRecords, interval);
                return response;

            }else{
                updateInteractionResponse(null, authorRecords,interval);
            }

            log.debug("PostInteraction Response json : " + json);



        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException at postInteraction : " + e.getMessage());
        } catch (Exception ex) {
            log.error("Exception at postInteraction" + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }

        return null;
    }

    /**
     * Update the batch execution status in Interval MASTER table and insert Playvox acknowledgement Id in InteractionStatus table
     * @param json
     * @param authorRecords
     * @param interval
     * @throws JsonProcessingException
     */
    public void updateInteractionResponse(JSONObject json, HashMap<String, Author> authorRecords,TimeInterval interval) throws JsonProcessingException {
        try {
            String statusCheckId = null;
            if (json != null) {
                json = json.getJSONObject("result");
                statusCheckId = json != null ? json.getString("_id") : null;
            }
            dbRepository.updateStatus(authorRecords, statusCheckId,interval.getDate());

            if(StringUtils.isNotEmpty(statusCheckId)){
                dbRepository.savePlayvoxId(interval.getId(),statusCheckId);
            }
        } catch (Exception ex) {
            log.error("Exception at updateInteractionResponse" + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }


}
