
package com.khoros.batch.job;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khoros.batch.dto.*;
import com.khoros.batch.dto.json.*;
import com.khoros.batch.repo.DBRepository;
import com.khoros.batch.service.DBConfig;
import com.khoros.batch.service.InteractionService;
import com.khoros.batch.service.RestClient;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;


@Log4j2
@Service
public class BatchWriter implements ItemWriter<Author>, StepExecutionListener {

    @Autowired
    private RestClient restClient;

    @Value("${khoros.api.auth.username}")
    private String authUser;

    @Value("${khoros.api.auth.password}")
    private String authPwd;

    @Value("${khoros.api.conversation.url}")
    private String conversationURL;

    @Autowired
    private Environment env;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private DBRepository repo;

    @Autowired
    private DBConfig dbConfig;


    private ExecutionContext executionContext;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        executionContext = stepExecution.getJobExecution().getExecutionContext();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
    /**
     * For Each chunk Size Khoros Conversation API is invoked,action logs are populated and then posted to playvox.
     */
    @Override
    public void write(List<? extends Author> items) throws Exception {

        try {

            Set<String> conversationIds = new HashSet<>();
            HashMap<String, Author> authorRecords = new HashMap<>();
            items.stream().forEach(author -> {
                List<String> conversationIdList = author.getConversationIds();
                conversationIds.addAll(conversationIdList);
                conversationIdList.stream().forEach(id -> {
                    authorRecords.put(id, author);
                });

            });


            List<String> conversationList = new ArrayList<>(conversationIds);

            //Setting Time interval in context for upcoming batches reference.
            if ( ! executionContext.containsKey("TimeInterval") || executionContext.get("TimeInterval") == null) {
                Author author = items.get(0);
                TimeInterval interval = TimeInterval.builder()
                        .date(author.getProcessDate())
                        .id(author.getIntervalId())
                        .batchRetryCount(author.getBatchRetryCount())
                        .build();
                executionContext.put("TimeInterval", interval);
            }

            //Get Action Logs for all conversations in this batch
            TimeInterval interval = (TimeInterval) executionContext.get("TimeInterval");
            String logsTableName = dbConfig.getActionLogTableName(interval.getDate());
            Map<String, String> logMap = repo.getActionLogsV2(interval.getId(), conversationList, logsTableName);

            String ids = String.join(",", conversationList);
            restClient.setHeaders(authUser, authPwd);
            String url = conversationURL + ids;
            LinkedHashMap response = restClient.sendRequest(url, HttpMethod.GET, LinkedHashMap.class);
            JSONObject responseJson = new JSONObject(response);
            JSONArray data = responseJson.getJSONArray("data");
            List<Interaction> interactions = parseAPIResponse(data, authorRecords, logMap);

            //Posting to Playvox
            interactionService.postInteraction(interactions, authorRecords, interval);

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Exception at Batch Writer : write method :  " + ex.getMessage());
        }
    }


    public List<Interaction> parseAPIResponse(JSONArray conversations, HashMap<String, Author> authorMap, Map<String, String> logMap) {
        try {

            List<Interaction> interactionList = new ArrayList<>();

            HashMap<String, String> authorPrevConversationIds = new HashMap<>();


            conversations.forEach(object -> {
                try {
                    JSONObject conversationJson = (JSONObject) object;
                    Interaction interaction = new Interaction();
                    ObjectMapper objectMapper = new ObjectMapper();
                    ResponseData conversationResponse = objectMapper.readValue(conversationJson.toString(), ResponseData.class);
                    Author author = authorMap.get(conversationResponse.getDisplayNumber());

                    interaction = parseEachConversation(conversationResponse, author);

                    String actionLogs = logMap.get(interaction.getInteraction_id());
                    if (StringUtils.isEmpty(actionLogs)) {
                        log.info("Action Logs not available for : " + interaction.getConversation_id() + " . Time Period: " + author.getTimePeriod());
                    } else {
                        interaction.setAction_logs(actionLogs);
                    }
                    String authorQueue = author.getQueueId();
                    //WorkQueue Validation
                    if (StringUtils.isEmpty(authorQueue)) {
                        author.setQueueId(interaction.getQueue());
                    } else {
                        author.setQueueId(authorQueue + "," + interaction.getQueue());
                    }
                    if (StringUtils.isEmpty(interaction.getQueuename())) {
                        author.setRecordStatus("QUEUE_NOT_CONFIGURED");
                    }
                    authorMap.put(conversationResponse.getDisplayNumber(), author);

                    if (interaction != null) {
                        if (interaction.isOldConversationId()) {
                            authorPrevConversationIds.put(author.getLithiumUuid(), conversationResponse.getDisplayNumber());
                        } else if (CollectionUtils.isNotEmpty(interaction.getComments())) {
                            log.info("Conversation to be Processed with Empty Messages : " + interaction.getInteraction_id());
                        }
                        if (StringUtils.isNotEmpty(interaction.getQueuename())) {
                            interaction.setAuthorLithium(author.getLithiumUuid());
                            interactionList.add(interaction);
                        }
                    }

                } catch (JsonProcessingException e) {
                    log.error("JsonProcessingException at parseAPIResponse : " + e.getMessage());
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Exception at parseAPIResponse : " + e.getMessage());
                    throw e;
                }

            });


            interactionList.stream().forEach(interaction -> {
                String authorId = interaction.getAuthorLithium();
                interaction.setPreviousconversations(authorPrevConversationIds.get(authorId));
            });

            return interactionList;
        } catch (Exception ex) {
            log.error("Exception at Batch Writer : parseAPIResponse :  " + ex.getMessage());
            throw ex;
        }


    }

    /**
     * Parses each conversation details (like status,customer info,agent info,notes,messages,media etc)
     * and builds request objects to post to Playvox
     * @param response
     * @param author
     * @return
     */
    private Interaction parseEachConversation(ResponseData response, Author author) {
        Interaction interaction = new Interaction();
        try {
            String conversationStatus = response.getStatus();
            String conversationId = response.getDisplayNumber();
            String timePeriod = author.getTimePeriod();
            interaction.setStatus(conversationStatus);
            interaction.setInteraction_date(formatTime(response.getCreateDate() + ""));

            if (conversationStatus.equalsIgnoreCase("CLOSED")) {
                int indexOfHyphen = timePeriod.indexOf("-");
                String startTime = timePeriod.substring(0, indexOfHyphen);
                Date startDateTime = formatTime(startTime);
                Date closedDateTime = formatTime(response.getCloseDate() + "");
                if (closedDateTime.before(startDateTime)) {
                    interaction.setOldConversationId(true);
                    log.info(conversationId + " was closed at " + closedDateTime + " prior to Interval start Time . Hence this conversation Ignored.");
                    return interaction;
                }
            }

            interaction.setNotes(author.getAuthorNotes());
            interaction.setInteraction_id(conversationId);
            interaction.setConversation_id(conversationId);
            interaction.setNotes(StringUtils.isNotEmpty(author.getAuthorNotes()) ? author.getAuthorNotes() : null);
            interaction.setUsername(StringUtils.isNotEmpty(author.getUsernames()) ? author.getUsernames() : null);

            WorkQueue workQueue = response.getWorkQueue();
            String queueId = workQueue.getLswUuid();
            interaction.setQueue(queueId);
            if (StringUtils.isNotEmpty(env.getProperty(queueId))) {
                interaction.setQueuename(env.getProperty(queueId));
            } else {
                log.info("Queue not configured for Import : " + queueId);
                interaction.setComments(null);
                return interaction;
            }


            List<MessageObject> messagesToParse = new ArrayList<>();
            MessageObject primaryMessage = response.getPrimaryMessage();
            interaction.setNetwork(primaryMessage.getNetwork());
            messagesToParse.add(primaryMessage);
            messagesToParse.addAll(response.getMessages());
            List<Message> messages = parseMessage(messagesToParse, author);
            if (CollectionUtils.isEmpty(messages)) {
                log.info("Empty Messages : " + interaction.getConversation_id());
                messages = new ArrayList<>();
            }
            interaction.setComments(messages);


            AssignedToAgent assignedToAgent = response.getAssignedToAgent();
            if (assignedToAgent != null && StringUtils.isNotEmpty(assignedToAgent.getEmail())) {
                interaction.setAssignee_id(assignedToAgent.getEmail());
                interaction.setAssigned_agent_email(assignedToAgent.getEmail());
            } else {
                interaction.setAssignee_id("SYSTEM");
                interaction.setAssigned_agent_email("SYSTEM");
            }

            List<Note> notes = response.getNotes();
            List<Message> internalNotes = parseInternalNotes(notes);
            if (!CollectionUtils.isEmpty(internalNotes)) {
                if(CollectionUtils.isNotEmpty(interaction.getComments())){
                    interaction.getComments().addAll(internalNotes);
                }else {
                    interaction.setComments(internalNotes);
                }
            }

        } catch (Exception ex) {
            log.error("Exception at Batch Writer : parseEachConversation :  " + ex.getMessage());
            throw ex;
        }

        return interaction;

    }

    private List<Message> parseMessage(List<MessageObject> messages, Author author) {

        List<Message> comments = new ArrayList<>();

        messages.stream().forEach(msgObject -> {
            Message msg = new Message();

            if (msgObject.getPublishDate() != 0) {
                msg.setAdded_at(formatTime(msgObject.getPublishDate() + ""));
            }

            msg.setType("customer_comment");
            Agent respondingAgent = msgObject.getRespondingAgent();
            AuthorInfo authorInfo = msgObject.getAuthor();
            if (respondingAgent != null) {
                msg.setType("agent_comment");

                if (StringUtils.isNotEmpty(respondingAgent.getEmail())) {
                    msg.setAuthor_id(respondingAgent.getEmail());
                }

            } else if (author != null && StringUtils.isNotEmpty(author.getAuthorHandle())) {
                msg.setAuthor_id(author.getAuthorHandle());
            }

            if (msgObject.getQuotedDocument() != null) {
                QuotedDocument quotedDocument = msgObject.getQuotedDocument();
                if (StringUtils.isNotEmpty(quotedDocument.getPostContent())) {
                    msg.setBody("Quoted " + "\u2023" + " \"" + quotedDocument.getPostContent() + "\"");
                }
                if (CollectionUtils.isNotEmpty(quotedDocument.getMedia())) {
                    msg.setAttachments(parseMedia(quotedDocument.getMedia()));
                }
                comments.add(msg);
            }

            if (StringUtils.isNotEmpty(msgObject.getPostContent())) {
                msg.setBody(msgObject.getPostContent());
                if (CollectionUtils.isNotEmpty(msgObject.getMedia())) {
                    msg.setAttachments(parseMedia(msgObject.getMedia()));
                }
                comments.add(msg);
            }

        });

        return comments;
    }


    private List<Message> parseInternalNotes(List<Note> notes) {

        List<Message> internalNotes = new ArrayList<>();

        notes.forEach(note -> {
            Message msg = new Message();
            msg.setType("internal_note");
            if (note.getCreateDate() != 0) {
                msg.setAdded_at(formatTime(note.getCreateDate() + ""));
            }
            if (note.getAgent() != null) {
                msg.setAuthor_id(note.getAgent().getName());
            }
            if (StringUtils.isNotEmpty(note.getNote())) {
                msg.setBody(note.getNote());
                internalNotes.add(msg);
            }
        });
        return internalNotes;
    }


    private List<Attachment> parseMedia(List<MediaInfo> mediaList) {
        List<Attachment> attachments = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        if (!mediaList.isEmpty()) {
            mediaList.forEach(media -> {
                String mediaURL = media.getUrl();
                if(!urls.contains(mediaURL)){
                    Attachment file = new Attachment(media.getType(), mediaURL);
                    attachments.add(file);
                    urls.add(mediaURL);
                }
            });
        }
        return attachments;
    }

    private Date formatTime(String epochString) {
        Date date = null;
        if (StringUtils.isNotEmpty(epochString)) {
            Instant instant = Instant.ofEpochMilli(Long.parseLong(epochString));
            long epoch = Long.parseLong(epochString);
            date = new Date(epoch);
        }
        return date;
    }


}
