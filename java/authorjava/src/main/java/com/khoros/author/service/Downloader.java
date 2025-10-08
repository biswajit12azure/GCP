package com.khoros.author.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khoros.author.config.UnAvailableException;
import com.khoros.author.dto.Author;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@Log4j2
public class Downloader {

    /**
     * Download URL is configured with Retry Template in Service Aspect class.
     * as there may be cases where khoros api might not return download url in First attempts.
     * It may take time to process the export.
     */
    public String getAuthorExportDownloadURL(String statusURL, RestClient restClient) throws UnAvailableException {

        try {

            LinkedHashMap response = restClient.sendRequest(statusURL, HttpMethod.GET, LinkedHashMap.class);
            JSONObject json = new JSONObject(response);
            //log.info("AuthorExport Status Response json : " + json);
            json = json.getJSONObject("result");
            json = json.getJSONObject("jobInfo");
            String downloadUrl = json.getString("downloadUrl");
            //Custom Exception "UnAvailableException" is thrown in case of Empty URL and handled in CustomRetryPolicy class accordingly.
            if (StringUtils.isEmpty(downloadUrl)) {
                //No.of retries and time delay within retry attempt are configured in SpringBootApplication Main Class
                log.info("Attempting to fetch download URL again for : " + statusURL);
                throw new UnAvailableException("Download URL is Empty.Process Running retry after sometime.");
            }
            return downloadUrl;

        } catch (Exception ex) {
            log.error(" Exception at  getAuthorExportDownloadURL : " + ex.getMessage());
            throw ex;
        }
    }


    public List<Author> invokeDownloadURL(String downloadURL, RestClient restClient) {

        try {
            String response = restClient.sendRequest(downloadURL, HttpMethod.GET, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            List<Author> authorList = objectMapper.readValue(response, new TypeReference<List<Author>>() {
            });

            return authorList;
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException at invokeDownloadURL" + e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception ex) {
            log.error("Exception at invokeDownloadURL" + ex.getMessage());
            throw ex;
        }
    }


}
