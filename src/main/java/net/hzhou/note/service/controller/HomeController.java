package net.hzhou.note.service.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

  @GetMapping("/")
  public ResponseEntity<Map<String, String>> home() {
    return ResponseEntity.ok(Collections.singletonMap("hello", "world"));
  }
}
