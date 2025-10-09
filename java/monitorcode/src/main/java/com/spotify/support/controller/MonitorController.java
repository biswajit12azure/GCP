package com.spotify.support.controller;

import com.spotify.support.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "support/")
public class MonitorController {


    @Autowired
    private MonitorService monitoringService;

    /**
     * Invoked automatically from GCP scheduler based on configured cron period in scheduler
     */
    @PutMapping
    @RequestMapping(value = "reset")
    public void restErrorRecords() {
        monitoringService.resetErrorRecords();
    }

    @PutMapping
    @RequestMapping(value = "report")
    public void sendReportEmail() {
        monitoringService.sendReportEmail();
    }


}
