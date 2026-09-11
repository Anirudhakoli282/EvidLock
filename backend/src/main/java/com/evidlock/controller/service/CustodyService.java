package com.evidlock.controller.service;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.ChainVerificationResult;
import com.evidlock.persistence.model.CustodyEvent;
import com.evidlock.persistence.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200", "http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:8081", "http://127.0.0.1:8081"}, allowCredentials = "true")
public class CustodyService {

    @GetMapping("/custody/events/{evidenceId}")
    public ResponseEntity<List<CustodyEvent>> getCustodyEvents(HttpServletRequest req, @PathVariable Long evidenceId) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.getCustodyEvents(evidenceId);
    }

    @GetMapping("/custody/verify/{evidenceId}")
    public ResponseEntity<ChainVerificationResult> verifyChain(HttpServletRequest req, @PathVariable Long evidenceId) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.verifyChain(evidenceId);
    }

    @GetMapping("/custody/recent")
    public ResponseEntity<List<CustodyEvent>> getRecentEvents(HttpServletRequest req, @RequestParam(defaultValue = "15") int limit) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(DBManager.getInstance().getCustodyEventDao().findRecent(limit), HttpStatus.OK);
    }

    @GetMapping("/dashboard/metrics")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics(HttpServletRequest req) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.getDashboardMetrics();
    }
}
