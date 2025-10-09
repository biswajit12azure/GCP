package com.spotify.support.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class IntervalData {
    private Long id;
    private Timestamp startTime;
    private Timestamp endTime;
    private String actionLogStatus;
    private String authorStatus;
    private String batchProcessStatus;

    // Getters and setters

    @Override
    public String toString() {
        return "ID: " + id + ", Start Time: " + startTime + ", End Time: " + endTime +
                ", Action Log Status: " + actionLogStatus + ", Author Status: " + authorStatus +
                ", Batch Process Status: " + batchProcessStatus;
    }
}

