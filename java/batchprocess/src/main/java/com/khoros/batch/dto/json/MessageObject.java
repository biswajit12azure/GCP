package com.khoros.batch.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageObject {

    private String postContent;
    private int conversationDisplayId;
    private String network;
    private AuthorInfo author;
    private long receiveDate;
    private long publishDate;
    private QuotedDocument quotedDocument;
    private Agent respondingAgent;
    private List<MediaInfo> media;


}
