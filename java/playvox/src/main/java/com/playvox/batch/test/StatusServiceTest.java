package com.playvox.batch.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
/*
This Spring Boot Test Classes makes use of DB configurations mentioned in application-dev.properties file
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=dev")
public class StatusServiceTest {

    @Test
    public void contextLoads() {
    }

}