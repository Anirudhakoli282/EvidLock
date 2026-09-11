package com.evidlock;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.EvidenceItem;
import com.evidlock.service.crypto.FileIntegrityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

@SpringBootTest
public class FileIntegrityTest {

    @Test
    @DisplayName("Verify SHA-256 calculation against known cryptographic vector")
    public void testSha256Calculation() {
        FileIntegrityService service = FileIntegrityService.getInstance();
        byte[] input = "test".getBytes(StandardCharsets.UTF_8);
        String hash = service.computeSha256(input);
        // Known SHA-256 of "test"
        Assertions.assertEquals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", hash);
    }

    @Test
    @DisplayName("Verify file integrity check succeeds when file bytes match stored hash")
    public void testFileIntegrityPassesOnValidBytes() {
        FileIntegrityService service = FileIntegrityService.getInstance();
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(1L);
        Assertions.assertNotNull(item);

        // Fetch original bytes from disk
        try {
            java.nio.file.Path p = java.nio.file.Paths.get(item.getFilePath());
            if (!java.nio.file.Files.exists(p)) {
                p = java.nio.file.Paths.get(item.getFilePath().replaceFirst("^backend[/\\\\]", ""));
            }
            if (!java.nio.file.Files.exists(p)) {
                p = java.nio.file.Paths.get("backend", item.getFilePath());
            }
            byte[] bytes = java.nio.file.Files.readAllBytes(p);

            boolean result = service.verifyFileIntegrity(item, bytes, "tester@evidlock.gov");
            Assertions.assertTrue(result, "File integrity check should pass for unaltered original evidence file");
            Assertions.assertEquals("VERIFIED", item.getIntegrityStatus());
        } catch (Exception e) {
            Assertions.fail("Failed reading test file: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify file integrity check fails and flags item when file bytes are altered")
    public void testFileIntegrityFailsAndFlagsCompromisedOnAlteredBytes() {
        FileIntegrityService service = FileIntegrityService.getInstance();
        EvidenceItem item = new EvidenceItem();
        item.setId(2L);
        item.setEvidenceTag("EV-2024-00201");
        item.setCurrentCustodianId("custodian@evidlock.gov");
        item.setFileHash("0b540389889aa5cda8e9b6a6a4beffee754600b65930b139ef769691883b399f");

        // Simulate corrupted / altered payload
        byte[] corruptedBytes = "MALICIOUS TAMPERED PAYLOAD CORRUPTING EVIDENCE BYTES".getBytes(StandardCharsets.UTF_8);

        boolean result = service.verifyFileIntegrity(item, corruptedBytes, "investigator@evidlock.gov");
        Assertions.assertFalse(result, "Integrity check must fail when bytes differ from stored hash!");
        Assertions.assertEquals("COMPROMISED", item.getIntegrityStatus(), "Evidence item must be flagged as COMPROMISED");

        // Verify DB update
        EvidenceItem refreshed = DBManager.getInstance().getEvidenceItemDao().findById(2L);
        Assertions.assertEquals("COMPROMISED", refreshed.getIntegrityStatus());

        // Restore clean state for other tests
        DBManager.getInstance().getEvidenceItemDao().updateIntegrityStatus(2L, "VERIFIED");
    }
}
