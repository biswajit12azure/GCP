package com.khoros.batch.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InteractionResultDto {

    @JsonProperty(value = "_id")
    private String resultId;
    private String reference_id;
    private int total;
    private String status;
    private int total_failed;
    private int total_completed;
    private List<InteractionStatus> completed;
    private List<InteractionStatus> failed;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InteractionStatus {

        @JsonProperty(value = "_id")
        private String _id;
        private String interaction_id;
        private String error;

    }

}

