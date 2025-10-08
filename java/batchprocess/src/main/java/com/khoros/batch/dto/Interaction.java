package com.khoros.batch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Interaction {

    private String interaction_id;
    private String conversation_id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date interaction_date;
    private String status;
    private String network;
    private String assignee_id;
    private String notes;
    private List<Message> comments;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date closeddate;
    private String queue;
    private String queuename;
    private String username;
    private String assigned_agent_email;
    private String action_logs;
    private String previousconversations;
    @JsonIgnore
    private boolean oldConversationId;
    @JsonIgnore
    private String authorLithium;

}
