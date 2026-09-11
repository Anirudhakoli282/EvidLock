package com.evidlock;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.ChainVerificationResult;
import com.evidlock.persistence.model.CustodyEvent;
import com.evidlock.service.crypto.HashChainService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class HashChainTest {

    @Test
    @DisplayName("Verify valid cryptographic hash chain computation and integrity")
    public void testValidHashChainComputation() {
        HashChainService service = HashChainService.getInstance();

        // 1. Genesis Event (Upload)
        Timestamp t1 = Timestamp.valueOf("2024-03-01 10:00:00");
        String genesisHash = HashChainService.GENESIS_HASH;
        String hash1 = service.computeEventHash(genesisHash, "investigator@evidlock.gov", "UPLOADED", t1, 999L);
        Assertions.assertNotNull(hash1);
        Assertions.assertEquals(64, hash1.length());

        // 2. Transfer Event
        Timestamp t2 = Timestamp.valueOf("2024-03-02 12:00:00");
        String hash2 = service.computeEventHash(hash1, "custodian@evidlock.gov", "TRANSFERRED", t2, 999L);
        Assertions.assertNotNull(hash2);
        Assertions.assertEquals(64, hash2.length());
        Assertions.assertNotEquals(hash1, hash2);

        // 3. Analysis Event
        Timestamp t3 = Timestamp.valueOf("2024-03-03 15:30:00");
        String hash3 = service.computeEventHash(hash2, "custodian@evidlock.gov", "ANALYZED", t3, 999L);
        Assertions.assertNotNull(hash3);
        Assertions.assertEquals(64, hash3.length());
    }

    @Test
    @DisplayName("Verify that mutating any past CustodyEvent breaks the hash chain (Tamper Detection)")
    public void testTamperDetectionBreaksChain() {
        HashChainService service = HashChainService.getInstance();

        // Construct in-memory 3-event chain
        Timestamp t1 = Timestamp.valueOf("2024-01-01 08:00:00");
        String hash1 = service.computeEventHash(HashChainService.GENESIS_HASH, "investigator@evidlock.gov", "UPLOADED", t1, 888L);
        CustodyEvent e1 = new CustodyEvent(1L, 888L, "investigator@evidlock.gov", "UPLOADED", t1, null, "investigator@evidlock.gov", "Initial intake", HashChainService.GENESIS_HASH, hash1);

        Timestamp t2 = Timestamp.valueOf("2024-01-02 10:00:00");
        String hash2 = service.computeEventHash(hash1, "custodian@evidlock.gov", "TRANSFERRED", t2, 888L);
        CustodyEvent e2 = new CustodyEvent(2L, 888L, "custodian@evidlock.gov", "TRANSFERRED", t2, "investigator@evidlock.gov", "custodian@evidlock.gov", "Transferred to vault", hash1, hash2);

        Timestamp t3 = Timestamp.valueOf("2024-01-03 14:00:00");
        String hash3 = service.computeEventHash(hash2, "custodian@evidlock.gov", "ANALYZED", t3, 888L);
        CustodyEvent e3 = new CustodyEvent(3L, 888L, "custodian@evidlock.gov", "ANALYZED", t3, "custodian@evidlock.gov", "custodian@evidlock.gov", "Forensic analysis", hash2, hash3);

        List<CustodyEvent> chain = new ArrayList<>(List.of(e1, e2, e3));

        // Tamper simulation: Attacker modifies the actor or action of past event #1
        e1.setActorId("malicious_insider@evidlock.gov");

        // Verify chain detects tampering at event #1
        boolean tampered = false;
        for (int i = 0; i < chain.size(); i++) {
            CustodyEvent ev = chain.get(i);
            String computed = service.computeEventHash(ev.getPreviousEventHash(), ev.getActorId(), ev.getAction(), ev.getTimestamp(), ev.getEvidenceItemId());
            if (!computed.equalsIgnoreCase(ev.getEventHash())) {
                tampered = true;
                Assertions.assertEquals(1L, ev.getId(), "Tampering should be detected starting at Event #1");
                break;
            }
        }
        Assertions.assertTrue(tampered, "Tampering in past event MUST break the hash chain verification!");
    }

    @Test
    @DisplayName("Verify that existing database seeded evidence item has valid intact chain")
    public void testSeededEvidenceChainVerification() {
        ChainVerificationResult result = HashChainService.getInstance().verifyChain(1L);
        Assertions.assertTrue(result.isValid(), "Evidence Item 1 should have a mathematically valid unbroken chain");
        Assertions.assertTrue(result.getTotalEvents() >= 3, "Evidence Item 1 should have at least 3 events");
    }
}
