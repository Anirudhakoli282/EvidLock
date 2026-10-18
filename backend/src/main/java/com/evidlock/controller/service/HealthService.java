package com.evidlock.controller.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight health-check endpoint.
 * GET /api/health → 200 OK {"status":"UP"}
 * Used by Vercel cron to keep the Render backend alive (free tier).
 */
@RestController
@CrossOrigin(originPatterns = {"*"})
public class HealthService {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "EvidLock Backend"
        ));
    }
}
