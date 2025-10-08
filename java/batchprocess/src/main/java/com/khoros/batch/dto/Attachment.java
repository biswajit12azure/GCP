package com.khoros.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment {

    private String name;
    private String url;

    public Attachment(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
