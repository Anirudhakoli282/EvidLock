package com.evidlock;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.EvidenceItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LegalHoldEnforcementTest {

    @Test
    @DisplayName("Verify DAO layer blocks deletion of evidence under active legal hold")
    public void testLegalHoldBlocksDeletionAtDaoLayer() {
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(1L);
        Assertions.assertNotNull(item);
        Assertions.assertTrue(item.isLegalHold(), "Item 1 should be under active legal hold");

        // Attempt delete
        Assertions.assertThrows(IllegalStateException.class, () -> {
            DBManager.getInstance().getEvidenceItemDao().delete(1L);
        }, "DAO layer MUST throw IllegalStateException when attempting to delete an item under active Legal Hold!");
    }

    @Test
    @DisplayName("Verify DAO layer blocks status update to Disposed when under legal hold")
    public void testLegalHoldBlocksDisposalAtDaoLayer() {
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(1L);
        Assertions.assertNotNull(item);
        Assertions.assertTrue(item.isLegalHold());

        item.setStatus("Disposed");
        Assertions.assertThrows(IllegalStateException.class, () -> {
            DBManager.getInstance().getEvidenceItemDao().update(item);
        }, "DAO layer MUST throw IllegalStateException when attempting to dispose an item under active Legal Hold!");
    }

    @Test
    @DisplayName("Verify legal hold can be toggled by supervisor and protection dynamically responds")
    public void testLegalHoldToggleDynamicallyEnforcesLock() {
        // Item 3 starts with legal_hold = false
        EvidenceItem item3 = DBManager.getInstance().getEvidenceItemDao().findById(3L);
        Assertions.assertNotNull(item3);
        Assertions.assertFalse(item3.isLegalHold());

        // Place legal hold
        boolean placed = DBManager.getInstance().getEvidenceItemDao().updateLegalHold(3L, true);
        Assertions.assertTrue(placed);

        // Deletion must now be blocked
        Assertions.assertThrows(IllegalStateException.class, () -> {
            DBManager.getInstance().getEvidenceItemDao().delete(3L);
        });

        // Lift legal hold
        boolean lifted = DBManager.getInstance().getEvidenceItemDao().updateLegalHold(3L, false);
        Assertions.assertTrue(lifted);

        EvidenceItem refreshed = DBManager.getInstance().getEvidenceItemDao().findById(3L);
        Assertions.assertFalse(refreshed.isLegalHold(), "Legal hold should be lifted");
    }
}
