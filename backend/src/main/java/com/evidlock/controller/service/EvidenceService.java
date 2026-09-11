package com.evidlock.controller.service;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.EvidenceItem;
import com.evidlock.persistence.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200", "http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:8081", "http://127.0.0.1:8081"}, allowCredentials = "true")
public class EvidenceService {

    @GetMapping("/evidence")
    public ResponseEntity<List<EvidenceItem>> getEvidence(
            HttpServletRequest req,
            @RequestParam(required = false) String agencyCode,
            @RequestParam(required = false) Long caseId) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.getEvidenceItems(agencyCode, caseId);
    }

    @GetMapping("/evidence/{id}")
    public ResponseEntity<EvidenceItem> getEvidenceById(HttpServletRequest req, @PathVariable Long id) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.getEvidenceItem(id);
    }

    @PostMapping("/evidence/upload")
    public ResponseEntity<EvidenceItem> uploadEvidence(
            HttpServletRequest req,
            @RequestParam("file") MultipartFile file,
            @RequestParam("caseId") Long caseId,
            @RequestParam("tag") String tag,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "retentionDate", required = false) String retentionDateStr) {

        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        Date retentionDate = null;
        if (retentionDateStr != null && !retentionDateStr.isBlank()) {
            try {
                retentionDate = Date.valueOf(retentionDateStr);
            } catch (Exception ignored) {}
        }

        return user.uploadEvidence(file, caseId, tag, description, retentionDate);
    }

    @PostMapping("/evidence/update")
    public ResponseEntity<EvidenceItem> updateEvidence(HttpServletRequest req, @RequestBody EvidenceItem item) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.updateEvidence(item);
    }

    @DeleteMapping("/evidence/{id}")
    public ResponseEntity<String> deleteEvidence(HttpServletRequest req, @PathVariable Long id) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.deleteEvidence(id);
    }

    @PostMapping("/evidence/{id}/legal-hold")
    public ResponseEntity<EvidenceItem> toggleLegalHold(
            HttpServletRequest req,
            @PathVariable Long id,
            @RequestParam("hold") boolean hold) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.setLegalHold(id, hold);
    }

    @GetMapping("/evidence/search")
    public ResponseEntity<List<EvidenceItem>> searchEvidence(
            HttpServletRequest req,
            @RequestParam("q") List<String> keywords) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(DBManager.getInstance().getEvidenceItemDao().search(keywords), HttpStatus.OK);
    }

    // =========================================================================
    // Compatibility Aliases for Docufy / legacy UI routes
    // =========================================================================

    @GetMapping("/documenti-azienda")
    public ResponseEntity<List<EvidenceItem>> getDocumentiAzienda(HttpServletRequest req, @RequestParam(required = false) String pIva) {
        return getEvidence(req, pIva, null);
    }

    @GetMapping("/documento")
    public ResponseEntity<EvidenceItem> getDocumento(HttpServletRequest req, @RequestParam Long id) {
        return getEvidenceById(req, id);
    }

    @GetMapping("/documenti-validi")
    public ResponseEntity<List<EvidenceItem>> getDocumentiValidi(HttpServletRequest req, @RequestParam(required = false) String pIva) {
        return getEvidence(req, pIva, null);
    }

    @GetMapping("/documenti-in-scadenza")
    public ResponseEntity<List<EvidenceItem>> getDocumentiInScadenza(HttpServletRequest req, @RequestParam(required = false) String pIva) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(DBManager.getInstance().getEvidenceItemDao().findNearingRetention(30), HttpStatus.OK);
    }

    @GetMapping("/documenti-scaduti")
    public ResponseEntity<List<EvidenceItem>> getDocumentiScaduti(HttpServletRequest req, @RequestParam(required = false) String pIva) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(DBManager.getInstance().getEvidenceItemDao().findEligibleForDisposal(), HttpStatus.OK);
    }
}
