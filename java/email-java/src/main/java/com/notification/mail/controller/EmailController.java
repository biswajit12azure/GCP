package com.notification.mail.controller;

import com.notification.mail.dto.EmailRequest;
import com.notification.mail.service.EmailService;
import com.notification.mail.service.HTMLEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "email/")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private HTMLEmailService  htmlEmailService;

    @PostMapping
    @RequestMapping(value = "send")
    public void sendEmail(@RequestBody EmailRequest request) {
        emailService.sendEmail(request);
    }

    @PostMapping
    @RequestMapping(value = "html/send")
    public void sendHTMLContentEmail(@RequestBody EmailRequest request) {
        htmlEmailService.sendEmail(request);
    }


}
