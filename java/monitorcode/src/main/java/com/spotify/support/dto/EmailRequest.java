package com.spotify.support.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class EmailRequest {

    private String subject;
    private String message;
}
