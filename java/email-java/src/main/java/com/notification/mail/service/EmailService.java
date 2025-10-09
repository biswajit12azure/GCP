package com.notification.mail.service;


import com.notification.mail.dto.EmailRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Log4j2
public class EmailService {

    private final JavaMailSender mailSender;


    @Value("${notification.email.targets}")
    private String defaultReceiverList;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(EmailRequest request) {

        try {
            List<String> receiverList = request.getReceiverList();
            if ( CollectionUtils.isEmpty(receiverList)) {
                receiverList = Arrays.asList(defaultReceiverList.split(","));;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            String targets[] = receiverList.toArray(new String[receiverList.size()]);
            message.setTo(targets);
            message.setSubject(request.getSubject());
            message.setText(request.getMessage());
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Exception occurred in method sendEmail : " + e.getMessage());
            throw e;
        }

    }
}
