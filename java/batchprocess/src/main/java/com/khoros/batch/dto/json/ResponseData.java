package com.khoros.batch.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseData {
    private String status;
    private AssignedToAgent assignedToAgent;
    private long createDate;
    private long closeDate;
    private String displayNumber;
    private MessageObject primaryMessage;
    private List<MessageObject> messages;
    private WorkQueue workQueue;
    private List<Note> notes;

    
}

