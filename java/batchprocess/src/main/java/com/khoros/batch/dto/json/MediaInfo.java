package com.khoros.batch.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaInfo {
    private String type;
    private String url;
    private String thumbUrl;
}
