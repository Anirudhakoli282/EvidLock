package com.evidlock.controller.service;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.EvidenceItem;
import com.evidlock.persistence.model.User;
import com.evidlock.service.crypto.HashChainService;
import com.evidlock.service.pdf.PdfReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://*.vercel.app", "https://*.onrender.com"}, allowCredentials = "true")
public class ReportService {

    @GetMapping("/reports/chain-of-custody/{evidenceId}")
    public ResponseEntity<byte[]> getChainOfCustodyReport(HttpServletRequest req, @PathVariable Long evidenceId) {
        User user = Utility.getRequestUser(req);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(evidenceId);
        if (item == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Log official report generation in the custody ledger
        HashChainService.getInstance().logEvent(
                evidenceId,
                user.getEmail(),
                "REPORT_EXPORTED",
                item.getCurrentCustodianId(),
                item.getCurrentCustodianId(),
                "Official court-ready Chain of Custody PDF report exported by " + user.getName() + " (" + user.getRole() + ")"
        );

        byte[] pdfBytes = PdfReportService.getInstance().generateChainOfCustodyReport(evidenceId, user.getName() + " (" + user.getEmail() + ")");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "Chain_of_Custody_" + item.getEvidenceTag() + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
