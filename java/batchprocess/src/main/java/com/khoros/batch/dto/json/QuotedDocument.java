package com.khoros.batch.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotedDocument {

        private AuthorInfo author;
        private String postContent;
        private List<MediaInfo> media;

        


}
