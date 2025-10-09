package com.spotify.support.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationData {

    private long totalCount;
    private long completedCount;
    private long QueueNotConfiguredCount;
    private long  processingCount;
    private long newStatusCount;
    private  String date;
}
