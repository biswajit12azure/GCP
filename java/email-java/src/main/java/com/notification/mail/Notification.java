package com.notification.mail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Notification {
    public static void main(String[] args) {
        SpringApplication.run(Notification.class, args);
    }

}
