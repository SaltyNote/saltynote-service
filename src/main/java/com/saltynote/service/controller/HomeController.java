package com.saltynote.service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saltynote.service.domain.transfer.ServiceResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value = "Home Endpoint", description = "Home endpoint for saltynote service")
public class HomeController {

    @Value("${app.welcome.message}")
    private String welcomeMessage;

    @ApiOperation("Display a greeting message")
    @GetMapping("/")
    public ResponseEntity<ServiceResponse> home() {
        return ResponseEntity.ok(ServiceResponse.ok(welcomeMessage));
    }
}
