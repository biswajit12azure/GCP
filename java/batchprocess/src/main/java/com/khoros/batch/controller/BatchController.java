package com.khoros.batch.controller;


import com.khoros.batch.service.BatchService;
import com.khoros.batch.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "khoros/")
public class BatchController {


    @Autowired
    private BatchService service;

    @Autowired
    private InteractionService interaction;

    /**
     * Invoked automatically from GCP scheduler based on configured cron period in scheduler
     */
    @PostMapping
    @RequestMapping(value = "batch")
    public void batchProcess() {
        service.batchProcess();
    }


}
