package com.khoros.batch.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khoros.batch.dto.Author;
import com.khoros.batch.dto.Interaction;
import com.khoros.batch.dto.TimeInterval;
import com.khoros.batch.job.BatchWriter;
import com.khoros.batch.service.BatchService;
import com.khoros.batch.service.InteractionService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BatchTest extends BatchServiceTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;


    @Autowired
    BatchService batchService;

    @Autowired
    BatchWriter batchWriter;

    @Autowired
    InteractionService interactionService;


    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

//    @Test
//    public void testCompleteAPIFlow() throws Exception {
//        mockMvc.perform(get("/khoros/batch/")).andExpect(status().isOk());
//    }


    @Test
    public void testPlayvoxAPI() throws Exception {

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        List<Interaction> interactions = setTestData();
        HashMap<String, Author> authorRecords = setAuthorRecords();

        TimeInterval interval = TimeInterval.builder()
                .id(BigInteger.valueOf(1910))
                .startTimeEpoch("1689927600547")
                .endTimeEpoch("1689927900547")
                .endTime(dateTimeFormat.parse("2023-07-21 08:25:00.547"))
                .startTime(dateTimeFormat.parse("2023-07-21 08:20:00.547"))
                .date(dateFormat.parse("2023-07-21"))
                .build();

        //Posting to Playvox
        LinkedHashMap response  = interactionService.postInteraction(interactions, authorRecords, interval);

        System.out.println(response);


    }

    private List<Interaction> setTestData() {

        List<Interaction> interactions = new ArrayList<>();

        try {
            String filename = "KhorosConversationResponse.json";
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(filename);

            if (inputStream == null) {
                System.err.println("File not found: " + filename);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            // Replace "YourDataClass" with your custom class representing the JSON structure
            JSONTokener tokener = new JSONTokener(new InputStreamReader(inputStream));
            // Create a JSONObject from the JSONTokener
            JSONObject jsonObject = new JSONObject(tokener);

//            String jsonData = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
//            // Convert the JSON string to a JSONObject
//            JSONObject jsonObject = new JSONObject(jsonData);
            HashMap logMap = new HashMap();

            HashMap<String, Author> authorRecords = setAuthorRecords();
            JSONArray data = jsonObject.getJSONArray("data");
            interactions = batchWriter.parseAPIResponse(data, authorRecords, logMap);


        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        return interactions;
    }


    private HashMap<String, Author> setAuthorRecords() throws ParseException {

        HashMap<String, Author> authorRecords = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Author author = Author.builder()
                .lithiumUuid("6ef377d0-2b22-4075-b777-ea87c3baacb4")
                .authorNotes("Sample author notes")
                .usernames("user1, user2")
                .authorHandle("JohnDoe")
                .recordStatus("PROCESSING")
                .timePeriod("1689927600547-1689927900547")
                .conversationIds(List.of("410684737"))
                .intervalId(BigInteger.valueOf(1910))
                .processDate(dateFormat.parse("2023-07-21"))
                .build();

        authorRecords.put("410684737", author);


        author = Author.builder()
                .lithiumUuid("443395ac-a76c-4eea-8334-5f7b1c717115")
                .authorNotes("Sample author notes")
                .usernames("user1, user2")
                .authorHandle("Nick")
                .recordStatus("PROCESSING")
                .timePeriod("1689927600547-1689927900547")
                .conversationIds(List.of("410540481"))
                .intervalId(BigInteger.valueOf(1910))
                .processDate(dateFormat.parse("2023-07-21"))
                .build();

        authorRecords.put("410540481", author);

        return authorRecords;
    }
}
