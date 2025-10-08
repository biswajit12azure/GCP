package com.khoros.author.controller;

import com.khoros.author.config.UnAvailableException;
import com.khoros.author.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "khoros/")
public class AuthorController {


    @Autowired
    private AuthorService authorService;


    /**
     * Invoked automatically from GCP scheduler based on configured cron period in scheduler
     */
    @RequestMapping(value = "author/")
    public void authorExport() {
        authorService.authorExport();
    }


}
