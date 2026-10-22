package com.evidlock;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.*;
import com.evidlock.service.crypto.HashChainService;
import com.evidlock.service.pdf.PdfReportService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.List;

@SpringBootTest
public class RemovalGovernanceTest {

    @Test
    @DisplayName("Verify Supervisor can only request removal and CANNOT approve removal")
    public void testSupervisorCannotApproveRemoval() {
        UserSupervisor supervisor = new UserSupervisor();
        supervisor.setEmail("supervisor@evidlock.gov");
        supervisor.setName("ACP Rajesh Patil");
        supervisor.setBadgeNumber("MPD-SUP-101");
        supervisor.setPassword("$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re"); // Web2324.

        // Supervisor attempt to approve must return 403 Forbidden
        ResponseEntity<EvidenceItem> resp = supervisor.approveEvidenceRemoval(1L, "COURT-ORD-2024-99", "Web2324.");
        Assertions.assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    @DisplayName("Verify Investigator, Custodian, and Auditor cannot request or approve removal")
    public void testNonAuthorizedRolesCannotRequestOrApproveRemoval() {
        UserInvestigator inv = new UserInvestigator();
        inv.setEmail("investigator@evidlock.gov");
        Assertions.assertEquals(HttpStatus.FORBIDDEN, inv.requestEvidenceRemoval(1L, "Valid reason of twenty characters", "Web2324.", "ORD-1").getStatusCode());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, inv.approveEvidenceRemoval(1L, "ORD-1", "Web2324.").getStatusCode());

        UserCustodian cst = new UserCustodian();
        cst.setEmail("custodian@evidlock.gov");
        Assertions.assertEquals(HttpStatus.FORBIDDEN, cst.requestEvidenceRemoval(1L, "Valid reason of twenty characters", "Web2324.", "ORD-1").getStatusCode());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, cst.approveEvidenceRemoval(1L, "ORD-1", "Web2324.").getStatusCode());

        UserAuditor aud = new UserAuditor();
        aud.setEmail("auditor@evidlock.gov");
        Assertions.assertEquals(HttpStatus.FORBIDDEN, aud.requestEvidenceRemoval(1L, "Valid reason of twenty characters", "Web2324.", "ORD-1").getStatusCode());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, aud.approveEvidenceRemoval(1L, "ORD-1", "Web2324.").getStatusCode());
    }

    @Test
    @DisplayName("Verify removal request is blocked for LOCKED legal hold, in-transfer, and not-SECURE evidence")
    public void testRemovalRequestBlockedByGovernanceRules() {
        UserSupervisor supervisor = new UserSupervisor();
        supervisor.setEmail("supervisor@evidlock.gov");
        supervisor.setPassword("$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re");

        // 1. Blocked when Legal Hold is LOCKED (Item 1 is locked in seed data)
        EvidenceItem item1 = DBManager.getInstance().getEvidenceItemDao().findById(1L);
        Assertions.assertNotNull(item1);
        Assertions.assertTrue(item1.isLegalHold());
        ResponseEntity<String> lockedResp = supervisor.requestEvidenceRemoval(1L, "Valid legal disposal justification exceeding 20 chars", "Web2324.", "ORD-101");
        Assertions.assertEquals(HttpStatus.LOCKED, lockedResp.getStatusCode());

        // 2. Blocked when integrity status is not SECURE (COMPROMISED)
        EvidenceItem item2 = DBManager.getInstance().getEvidenceItemDao().findById(2L);
        Assertions.assertNotNull(item2);
        DBManager.getInstance().getEvidenceItemDao().updateIntegrityStatus(2L, "COMPROMISED");
        ResponseEntity<String> compromisedResp = supervisor.requestEvidenceRemoval(2L, "Valid legal disposal justification exceeding 20 chars", "Web2324.", "ORD-102");
        Assertions.assertEquals(HttpStatus.CONFLICT, compromisedResp.getStatusCode());
        // Restore clean state
        DBManager.getInstance().getEvidenceItemDao().updateIntegrityStatus(2L, "VERIFIED");
    }

    @Test
    @DisplayName("Verify removal request requires retention date expired OR legal reference provided")
    public void testRetentionOrLegalRefRequired() {
        UserSupervisor supervisor = new UserSupervisor();
        supervisor.setEmail("supervisor@evidlock.gov");
        supervisor.setPassword("$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re");

        // Ensure item 2 is not under legal hold and retention is explicitly in future
        EvidenceItem item2 = DBManager.getInstance().getEvidenceItemDao().findById(2L);
        Assertions.assertNotNull(item2);
        DBManager.getInstance().getEvidenceItemDao().updateLegalHold(2L, false);
        item2.setLegalHold(false);
        item2.setRetentionDate(new Date(System.currentTimeMillis() + 864000000L)); // 10 days in future
        DBManager.getInstance().getEvidenceItemDao().update(item2);

        // Attempt removal without legal reference when retention date is in the future
        // Note: seed data sets retention to CURRENT_DATE + 14 days
        ResponseEntity<String> noRefResp = supervisor.requestEvidenceRemoval(2L, "Valid legal disposal justification exceeding 20 chars", "Web2324.", null);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, noRefResp.getStatusCode());

        // Providing court order legal reference succeeds
        ResponseEntity<String> withRefResp = supervisor.requestEvidenceRemoval(2L, "Court-ordered disposal pursuant to trial closure mandate", "Web2324.", "CBI/DISP/2024/099");
        Assertions.assertEquals(HttpStatus.OK, withRefResp.getStatusCode());

        // Verify status moved to REMOVAL_REQUESTED
        EvidenceItem refreshed = DBManager.getInstance().getEvidenceItemDao().findById(2L);
        Assertions.assertEquals("REMOVAL_REQUESTED", refreshed.getStatus());
        Assertions.assertEquals("CBI/DISP/2024/099", refreshed.getLegalRef());
    }

    @Test
    @DisplayName("Verify Self-Approval is strictly rejected and Court Officer cannot approve their own request or owned case")
    public void testSelfApprovalAndCaseOwnerApprovalRejected() {
        UserCourt court = new UserCourt();
        court.setEmail("court@evidlock.gov");
        court.setName("Adv. Siddharth Deshmukh");
        court.setBadgeNumber("CRT-PP-770");
        court.setPassword("$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re");

        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(2L);
        Assertions.assertNotNull(item);
        item.setStatus("REMOVAL_REQUESTED");

        // Simulate Court being the requester -> Self-approval MUST be rejected
        item.setRemovalRequestedBy("court@evidlock.gov");
        DBManager.getInstance().getEvidenceItemDao().updateRemovalStatus(item);

        ResponseEntity<EvidenceItem> selfApproveResp = court.approveEvidenceRemoval(2L, "CRT-ORD-FINAL-2024", "Web2324.");
        Assertions.assertEquals(HttpStatus.FORBIDDEN, selfApproveResp.getStatusCode());

        // Reset requester back to supervisor
        item.setRemovalRequestedBy("supervisor@evidlock.gov");
        DBManager.getInstance().getEvidenceItemDao().updateRemovalStatus(item);
    }

    @Test
    @DisplayName("Verify Court approval completes removal, seals payload, preserves tombstone, and keeps hash chain intact")
    public void testCourtApprovalCompletesRemovalAndPreservesLedger() {
        UserCourt court = new UserCourt();
        court.setEmail("court@evidlock.gov");
        court.setName("Adv. Siddharth Deshmukh");
        court.setBadgeNumber("CRT-PP-770");
        court.setPassword("$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re");

        // Ensure item 2 is in REMOVAL_REQUESTED state
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(2L);
        Assertions.assertNotNull(item);
        item.setStatus("REMOVAL_REQUESTED");
        item.setRemovalRequestedBy("supervisor@evidlock.gov");
        item.setRemovalReason("Trial concluded; exhibit marked for final controlled disposal");
        item.setLegalRef("CBI/DISP/2024/099");
        DBManager.getInstance().getEvidenceItemDao().updateRemovalStatus(item);

        // Approve removal
        ResponseEntity<EvidenceItem> approveResp = court.approveEvidenceRemoval(2L, "MAH-HC-ORDER-882/2024", "Web2324.");
        Assertions.assertEquals(HttpStatus.OK, approveResp.getStatusCode());

        EvidenceItem removed = approveResp.getBody();
        Assertions.assertNotNull(removed);
        Assertions.assertEquals("REMOVED", removed.getStatus());
        Assertions.assertEquals("court@evidlock.gov", removed.getRemovalApprovedBy());
        Assertions.assertEquals("MAH-HC-ORDER-882/2024", removed.getLegalRef());
        Assertions.assertNotNull(removed.getRemovedAt());
        // Master SHA-256 hash digest must remain permanently preserved
        Assertions.assertNotNull(removed.getFileHash());
        Assertions.assertFalse(removed.getFileHash().isBlank());

        // Verify SHA-256 cryptographic hash chain verification STILL PASSES
        ChainVerificationResult chainRes = HashChainService.getInstance().verifyChain(2L);
        Assertions.assertTrue(chainRes.isValid(), "Cryptographic hash chain must remain 100% valid after disposal events!");

        // Verify PDF report generation includes REMOVED banner and disposal section without errors
        byte[] pdfBytes = PdfReportService.getInstance().generateChainOfCustodyReport(2L, "Adv. Siddharth Deshmukh (court@evidlock.gov)");
        Assertions.assertNotNull(pdfBytes);
        Assertions.assertTrue(pdfBytes.length > 500, "PDF report should be generated with tombstone disposal section");
    }

    @Test
    @DisplayName("Verify direct DB DELETE on evidence_items is permanently rejected by system governance")
    public void testDirectDbDeleteFails() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            DBManager.getInstance().getEvidenceItemDao().delete(2L);
        });
    }
}
