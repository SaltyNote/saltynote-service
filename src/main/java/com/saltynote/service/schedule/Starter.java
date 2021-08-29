package com.saltynote.service.schedule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.saltynote.service.utils.BaseUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile({"dev", "test", "local", "default"})
@Slf4j
public class Starter implements CommandLineRunner {

    @Value("${server.port}")
    private int port;

    /**
     * Inject the local server into email content.
     */
    @Override
    public void run(String... args) {
        String baseUrl = "http://127.0.0.1:" + port;
        BaseUtils.setBaseUrl(baseUrl);
        log.info("Set base url to {}", baseUrl);
    }
}
