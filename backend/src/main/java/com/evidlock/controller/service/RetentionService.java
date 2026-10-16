package com.evidlock.controller.service;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.EvidenceItem;
import com.evidlock.persistence.model.User;
import com.evidlock.service.scheduler.RetentionAlertScheduler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://*.vercel.app", "https://*.onrender.com"}, allowCredentials = "true")
public class RetentionService {

    @Autowired(required = false)
    private RetentionAlertScheduler retentionAlertScheduler;

    @GetMapping("/retention/eligible")
    public ResponseEntity<List<EvidenceItem>> getEligibleForDisposal(HttpServletRequest req) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(DBManager.getInstance().getEvidenceItemDao().findEligibleForDisposal(), HttpStatus.OK);
    }

    @GetMapping("/retention/nearing")
    public ResponseEntity<List<EvidenceItem>> getNearingRetention(HttpServletRequest req, @RequestParam(defaultValue = "30") int days) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(DBManager.getInstance().getEvidenceItemDao().findNearingRetention(days), HttpStatus.OK);
    }

    @GetMapping("/retention/compromised")
    public ResponseEntity<List<EvidenceItem>> getCompromisedItems(HttpServletRequest req) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(DBManager.getInstance().getEvidenceItemDao().findIntegrityCompromised(), HttpStatus.OK);
    }

    @PostMapping("/retention/check-now")
    public ResponseEntity<Map<String, Object>> triggerRetentionCheck(HttpServletRequest req) {
        User user = Utility.getRequestUser(req);
        if (user == null || !"SUPERVISOR".equalsIgnoreCase(user.getRole())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (retentionAlertScheduler == null) {
            retentionAlertScheduler = new RetentionAlertScheduler();
        }

        int count = retentionAlertScheduler.checkRetentionAndNotifySupervisors();
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("itemsProcessed", count);
        resp.put("message", "Retention check completed and email alerts dispatched.");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
