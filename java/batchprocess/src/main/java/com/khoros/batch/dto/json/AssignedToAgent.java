package com.khoros.batch.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignedToAgent {
    private String name;
    private String email;
    private String lswUuid;
    private String ssoId;

}
