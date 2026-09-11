package com.evidlock.controller.service;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.model.Case;
import com.evidlock.persistence.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200", "http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:8081", "http://127.0.0.1:8081"}, allowCredentials = "true")
public class CaseService {

    @GetMapping("/cases")
    public ResponseEntity<List<Case>> getCases(HttpServletRequest req) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.getCases();
    }

    @GetMapping("/cases/{id}")
    public ResponseEntity<Case> getCaseById(HttpServletRequest req, @PathVariable Long id) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.getCase(id);
    }

    @PostMapping("/cases")
    public ResponseEntity<Case> createCase(HttpServletRequest req, @RequestBody Case c) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.createCase(c);
    }

    @PutMapping("/cases/{id}")
    public ResponseEntity<Case> updateCase(HttpServletRequest req, @PathVariable Long id, @RequestBody Case c) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        c.setId(id);
        return user.updateCase(c);
    }
}
