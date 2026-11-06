package com.tick_tracker.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController//this class handles web request
public class HealthController {

    @GetMapping("/health") //call this method for GET /health
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
