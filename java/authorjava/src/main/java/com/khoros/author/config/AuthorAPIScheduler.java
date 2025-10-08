//package com.khoros.author.config;
//
//import com.khoros.author.service.AuthorService;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
//@Component
//@Log4j2
//public class AuthorAPIScheduler {
//
//
//    @Autowired
//    private AuthorService authorService;
//
//    @Scheduled(cron = "0 0/5 * * * *") // Run every 5 minutes
//    public void processAuthorExport() throws UnAvailableException {
//        log.info("Scheduling Author export run " + new Date());
//        authorService.authorExport();
//    }
//}
