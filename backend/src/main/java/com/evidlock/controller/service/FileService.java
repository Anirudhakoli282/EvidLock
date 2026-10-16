package com.evidlock.controller.service;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.EvidenceItem;
import com.evidlock.persistence.model.User;
import com.evidlock.service.crypto.FileIntegrityService;
import com.evidlock.service.crypto.HashChainService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://*.vercel.app", "https://*.onrender.com"}, allowCredentials = "true")
public class FileService {

    @GetMapping("/get-file")
    public ResponseEntity<?> getFile(
            HttpServletRequest req,
            @RequestParam("path") String path,
            @RequestParam(value = "evidenceId", required = false) Long evidenceId) {

        User user = Utility.getRequestUser(req);
        if (user == null) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        File file = new File(path);
        if (!file.exists()) {
            file = new File(path.replaceFirst("^backend[/\\\\]", ""));
        }
        if (!file.exists()) {
            file = new File("backend", path);
        }
        if (!file.exists()) {
            return new ResponseEntity<>("Evidence file not found on storage", HttpStatus.NOT_FOUND);
        }

        // Locate EvidenceItem record
        EvidenceItem item = null;
        if (evidenceId != null) {
            item = DBManager.getInstance().getEvidenceItemDao().findById(evidenceId);
        } else {
            List<EvidenceItem> all = DBManager.getInstance().getEvidenceItemDao().getAll();
            for (EvidenceItem e : all) {
                if (path.equalsIgnoreCase(e.getFilePath())) {
                    item = e;
                    break;
                }
            }
        }

        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            // CRYPTOGRAPHIC INTEGRITY VERIFICATION ON DOWNLOAD
            if (item != null) {
                boolean intact = FileIntegrityService.getInstance().verifyFileIntegrity(item, fileBytes, user.getEmail());
                if (!intact) {
                    HttpHeaders errHeaders = new HttpHeaders();
                    errHeaders.add("X-Integrity-Status", "COMPROMISED");
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .headers(errHeaders)
                            .body("INTEGRITY COMPROMISED: Cryptographic file hash does not match stored master digest. Normal access blocked pending supervisor review.");
                }

                // Log DOWNLOADED event in immutable chain of custody
                HashChainService.getInstance().logEvent(
                        item.getId(),
                        user.getEmail(),
                        "DOWNLOADED",
                        item.getCurrentCustodianId(),
                        item.getCurrentCustodianId(),
                        "Evidence file downloaded and verified intact by " + user.getName() + " (" + user.getRole() + ")"
                );
            }

            ByteArrayResource resource = new ByteArrayResource(fileBytes);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            headers.add("X-Integrity-Status", "VERIFIED");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete-file")
    public ResponseEntity<String> deleteFile(HttpServletRequest req, @RequestParam("path") String path) {
        User user = Utility.getRequestUser(req);
        if (user == null || !"SUPERVISOR".equalsIgnoreCase(user.getRole())) {
            return new ResponseEntity<>("Only supervisors may remove evidence files", HttpStatus.FORBIDDEN);
        }

        // Check legal hold
        List<EvidenceItem> all = DBManager.getInstance().getEvidenceItemDao().getAll();
        for (EvidenceItem e : all) {
            if (path.equalsIgnoreCase(e.getFilePath()) && e.isLegalHold()) {
                return new ResponseEntity<>("Operation blocked: Evidence item is under active Legal Hold", HttpStatus.LOCKED);
            }
        }

        return Utility.deleteFile(path) ? new ResponseEntity<>("File deleted", HttpStatus.OK)
                : new ResponseEntity<>("Failed to delete file", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
