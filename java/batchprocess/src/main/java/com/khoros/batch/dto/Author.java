package com.khoros.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Author {

    private String lithiumUuid;

    private String authorNotes;

    //  private String conversationIds;

    private String usernames;

    private String authorHandle;

    private String recordStatus;

    private String timePeriod;

    private List<String> conversationIds;

    private String queueId;

    private String playvoxId;

    private BigInteger intervalId;

    private Date processDate;

    private int batchRetryCount;

}

