package com.saltynote.service.schedule;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.saltynote.service.utils.BaseUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile({"dev", "test", "local"})
@Slf4j
public class Starter implements CommandLineRunner {
  @Override
  public void run(String... args) {
    String baseUrl = "http://127.0.0.1:8080";
    BaseUtils.setBaseUrl(baseUrl);
    log.info("Set base url to {}", baseUrl);
  }
}
