package com.notification.mail.test;

import com.notification.mail.service.EmailService;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.WebApplicationContext;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExportTest extends EmailServiceTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;


    @Autowired
    EmailService authorService;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testCompleteAPIFlow() throws Exception {
        mockMvc.perform(get("/khoros/author/")).andExpect(status().isOk());
    }


}
