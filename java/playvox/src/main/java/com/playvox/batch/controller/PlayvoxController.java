package com.playvox.batch.controller;


import com.playvox.batch.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "playvox/")
public class PlayvoxController {

    @Autowired
    private StatusService service;

    /**
     * Invoked automatically from GCP scheduler based on configured cron period in scheduler
     */
    @PostMapping
    @RequestMapping(value = "status")
    public void statusJob() {
        service.fetchAndUpdateStatus();
    }


}
