package com.saltynote.service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saltynote.service.domain.transfer.ServiceResponse;

@RestController
public class HomeController {

  @Value("${app.welcome.message}")
  private String welcomeMessage;

  @GetMapping("/")
  public ResponseEntity<ServiceResponse> home() {
    return ResponseEntity.ok(ServiceResponse.ok(welcomeMessage));
  }
}
