package com.khoros.author.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Author {


    @JsonProperty(value = "smm_uuid")
    private String lithiumUuid;


    @JsonProperty(value = "Notes")
    private String authorNotes;


    @JsonProperty(value = "ConversationIDs")
    private String conversationIds;


    @JsonProperty(value = "Username")
    private String usernames;


    @JsonProperty(value = "TwitterHandles")
    private String twitterHandles;

    @JsonProperty(value = "FacebookDisplayNames")
    private String facebookDisplayNames;

    @JsonProperty(value = "GoogleMessengerDisplayNames")
    private String googleMessengerDisplayNames;

    @JsonProperty(value = "InstagramHandles")
    private String instagramHandles;

    @JsonProperty(value = "youtube_handles")
    private String youtubeHandles;

    @JsonProperty(value = "linkedin_handles")
    private String linkedinHandles;

    @JsonProperty(value = "sms_handles")
    private String smsHandles;

    @JsonProperty(value = "wechat_handles")
    private String wechatNames;

    @JsonIgnore
    private String status = "NEW";
}

