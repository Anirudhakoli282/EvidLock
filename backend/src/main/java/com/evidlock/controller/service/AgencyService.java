package com.evidlock.controller.service;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.Agency;
import com.evidlock.persistence.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200", "http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:8081", "http://127.0.0.1:8081"}, allowCredentials = "true")
public class AgencyService {

    @GetMapping("/agencies")
    public ResponseEntity<List<Agency>> getAgencies(HttpServletRequest req) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(DBManager.getInstance().getAgencyDao().getAll(), HttpStatus.OK);
    }

    @GetMapping("/agency/{code}")
    public ResponseEntity<Agency> getAgency(HttpServletRequest req, @PathVariable String code) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        Agency a = DBManager.getInstance().getAgencyDao().findByCode(code);
        return a != null ? new ResponseEntity<>(a, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest req) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.getProfile();
    }

    // Legacy Docufy compatibility aliases
    @GetMapping("/aziende")
    public ResponseEntity<List<Agency>> getAziende(HttpServletRequest req) {
        return getAgencies(req);
    }

    @GetMapping("/azienda")
    public ResponseEntity<Agency> getAzienda(HttpServletRequest req, @RequestParam String pIva) {
        return getAgency(req, pIva);
    }

    @GetMapping("/profilo")
    public ResponseEntity<?> getProfilo(HttpServletRequest req) {
        return getProfile(req);
    }
}
