package com.khoros.batch.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkQueue {
    private String lswUuid;
    private int id;
}
