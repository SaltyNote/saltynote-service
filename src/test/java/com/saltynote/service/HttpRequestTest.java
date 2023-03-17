package com.saltynote.service;

import com.saltynote.service.domain.transfer.ServiceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpRequestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${app.welcome.message}")
    private String welcomeMessage;

    @Test
    void welcomePageShouldOK() {
        ServiceResponse sr =
                this.restTemplate.getForObject("http://localhost:" + port + "/", ServiceResponse.class);
        assertEquals(HttpStatus.OK, sr.getStatus());
        assertEquals(welcomeMessage, sr.getMessage());
    }
}
