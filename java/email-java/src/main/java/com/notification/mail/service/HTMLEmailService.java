package com.notification.mail.service;

import com.notification.mail.dto.EmailRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * This class is used to send email with HTML content.
 */
@Service
@Log4j2
public class HTMLEmailService {
    @Value("${smtp.sender.id}")
    private String senderEmailId;
    @Value("${smtp.sender.password}")
    private String senderPassword;
    @Value("${smtp.host}")
    private String emailSMTPserver;

    @Value("${smtp.port}")
    private int port;

    @Value("${notification.email.targets}")
    private String defaultReceiverList;

    public void sendEmail(EmailRequest request) {
        Properties props = new Properties();
        try {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", emailSMTPserver);
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.port", port);

            Authenticator auth = new SMTPAuthenticator();
            Session session = Session.getInstance(props, auth);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmailId));
            List<String> receiverList = request.getReceiverList();
            if (CollectionUtils.isEmpty(receiverList)) {
                receiverList = Arrays.asList(defaultReceiverList.split(","));
                ;
            }
            Address[] addresses = new Address[receiverList.size()];
            for (int i = 0; i < receiverList.size(); i++) {
                addresses[i] = new InternetAddress(receiverList.get(i));
            }
            message.setRecipients(Message.RecipientType.TO, addresses);
            message.setSubject(request.getSubject());
            message.setContent(request.getMessage(), "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            log.error("MessagingException occurred in method sendEmail : " + e.getMessage());
        } catch (Exception e) {
            log.error("Exception occurred in method sendEmail : " + e.getMessage());
            throw e;
        }
    }

    private class SMTPAuthenticator extends
            javax.mail.Authenticator {
        public PasswordAuthentication
        getPasswordAuthentication() {
            return new PasswordAuthentication(senderEmailId,
                    senderPassword);
        }
    }

}
