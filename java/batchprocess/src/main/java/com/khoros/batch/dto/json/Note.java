package com.khoros.batch.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Note {
    private String source;
    private long createDate;
    private String note;
    private Agent agent;
}
