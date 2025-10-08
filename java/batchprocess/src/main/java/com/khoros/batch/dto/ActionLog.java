package com.khoros.batch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import java.util.Date;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ActionLog {


    private String conversationId;
    private String actionPerformed;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy hh:mm:ss a Z")
    private Date actionPerformedDate;
    private String historyAction;
    private String actingAgent;
    private String actingAgentEmail;
    private String workqueueCurrent;
    private String tags;
    private String comment;
    private String note;
}