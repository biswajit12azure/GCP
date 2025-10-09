package com.notification.mail.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class EmailRequest {

    private List<String> receiverList;
    private String subject;
    private String message;
}
