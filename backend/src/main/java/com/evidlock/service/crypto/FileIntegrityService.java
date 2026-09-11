package com.evidlock.service.crypto;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.EvidenceItem;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileIntegrityService {

    private static FileIntegrityService instance = null;

    private FileIntegrityService() {}

    public static synchronized FileIntegrityService getInstance() {
        if (instance == null) {
            instance = new FileIntegrityService();
        }
        return instance;
    }

    /**
     * Computes the SHA-256 hash of the given byte array.
     */
    public String computeSha256(byte[] fileBytes) {
        if (fileBytes == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(fileBytes);
            return HashChainService.bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    /**
     * Verifies file bytes against stored evidenceItem.getFileHash().
     * If mismatch, flags evidence item as 'COMPROMISED' in DB, logs an INTEGRITY_ALERT custody event,
     * and returns false to block normal access.
     */
    public boolean verifyFileIntegrity(EvidenceItem item, byte[] fileBytes, String actorId) {
        if (item == null || fileBytes == null) {
            return false;
        }

        String computedHash = computeSha256(fileBytes);
        boolean matches = computedHash.equalsIgnoreCase(item.getFileHash());

        if (!matches) {
            // Flag item as INTEGRITY COMPROMISED
            item.setIntegrityStatus("COMPROMISED");
            DBManager.getInstance().getEvidenceItemDao().updateIntegrityStatus(item.getId(), "COMPROMISED");

            // Log critical custody event
            HashChainService.getInstance().logEvent(
                    item.getId(),
                    actorId != null ? actorId : "SYSTEM",
                    "INTEGRITY_ALERT",
                    item.getCurrentCustodianId(),
                    null,
                    "CRITICAL: File integrity mismatch detected! Expected SHA-256: " + item.getFileHash() +
                            ", Computed from storage: " + computedHash + ". Access blocked pending supervisor review."
            );
            return false;
        }

        return true;
    }
}
