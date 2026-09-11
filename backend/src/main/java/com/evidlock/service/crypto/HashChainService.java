package com.evidlock.service.crypto;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.ChainVerificationResult;
import com.evidlock.persistence.model.CustodyEvent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class HashChainService {

    public static final String GENESIS_HASH = "GENESIS";
    private static HashChainService instance = null;

    private HashChainService() {}

    public static synchronized HashChainService getInstance() {
        if (instance == null) {
            instance = new HashChainService();
        }
        return instance;
    }

    /**
     * Computes the SHA-256 event hash:
     * eventHash = SHA-256(previousEventHash + actorId + action + timestamp + evidenceItemId)
     */
    public String computeEventHash(String previousEventHash, String actorId, String action, Timestamp timestamp, Long evidenceItemId) {
        String tsStr = formatTimestamp(timestamp);
        String raw = previousEventHash + actorId + action + tsStr + evidenceItemId;
        return sha256Hex(raw);
    }

    public static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            timestamp = new Timestamp(System.currentTimeMillis());
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Appends a new immutable CustodyEvent linked cryptographically to the prior event.
     */
    public CustodyEvent logEvent(Long evidenceItemId, String actorId, String action,
                                 String fromCustodianId, String toCustodianId, String notes) {
        List<CustodyEvent> existingEvents = DBManager.getInstance().getCustodyEventDao().findByEvidenceItem(evidenceItemId);
        String previousHash;
        if (existingEvents.isEmpty()) {
            previousHash = GENESIS_HASH;
        } else {
            previousHash = existingEvents.get(existingEvents.size() - 1).getEventHash();
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        String eventHash = computeEventHash(previousHash, actorId, action, now, evidenceItemId);

        CustodyEvent event = new CustodyEvent();
        event.setEvidenceItemId(evidenceItemId);
        event.setActorId(actorId);
        event.setAction(action);
        event.setTimestamp(now);
        event.setFromCustodianId(fromCustodianId);
        event.setToCustodianId(toCustodianId);
        event.setNotes(notes);
        event.setPreviousEventHash(previousHash);
        event.setEventHash(eventHash);

        Long eventId = DBManager.getInstance().getCustodyEventDao().insert(event);
        event.setId(eventId);
        return event;
    }

    /**
     * Verifies the full cryptographic hash chain for an evidence item.
     * Iterates sequentially through all events, verifying prior pointer and recomputing hash.
     */
    public ChainVerificationResult verifyChain(Long evidenceItemId) {
        List<CustodyEvent> events = DBManager.getInstance().getCustodyEventDao().findByEvidenceItem(evidenceItemId);
        ChainVerificationResult result = new ChainVerificationResult();
        result.setEvidenceItemId(evidenceItemId);
        result.setEvents(events);
        result.setTotalEvents(events.size());

        if (events.isEmpty()) {
            result.setValid(true);
            result.setStatusMessage("Genesis state: No custody events recorded yet.");
            return result;
        }

        for (int i = 0; i < events.size(); i++) {
            CustodyEvent event = events.get(i);

            // Check genesis or previous link pointer
            if (i == 0) {
                if (!GENESIS_HASH.equalsIgnoreCase(event.getPreviousEventHash())) {
                    result.setValid(false);
                    result.setBrokenAtEventId(event.getId());
                    result.setBrokenReason("Genesis block violation: initial event does not reference " + GENESIS_HASH);
                    result.setStatusMessage("INTEGRITY COMPROMISED at event #" + event.getId());
                    return result;
                }
            } else {
                CustodyEvent previousEvent = events.get(i - 1);
                if (!previousEvent.getEventHash().equalsIgnoreCase(event.getPreviousEventHash())) {
                    result.setValid(false);
                    result.setBrokenAtEventId(event.getId());
                    result.setBrokenReason("Broken hash chain: Event #" + event.getId() + " previous hash pointer (" +
                            event.getPreviousEventHash() + ") does not match preceding event #" + previousEvent.getId() +
                            " hash (" + previousEvent.getEventHash() + ")");
                    result.setStatusMessage("INTEGRITY COMPROMISED: Chain broken at event #" + event.getId());
                    return result;
                }
            }

            // Recompute expected hash
            String computedHash = computeEventHash(
                    event.getPreviousEventHash(),
                    event.getActorId(),
                    event.getAction(),
                    event.getTimestamp(),
                    evidenceItemId
            );

            if (!computedHash.equalsIgnoreCase(event.getEventHash())) {
                result.setValid(false);
                result.setBrokenAtEventId(event.getId());
                result.setBrokenReason("Cryptographic signature mismatch at event #" + event.getId() +
                        ": Stored=" + event.getEventHash() + ", Computed=" + computedHash +
                        ". Event parameters have been tampered with!");
                result.setStatusMessage("INTEGRITY COMPROMISED: Data tamper detected at event #" + event.getId());
                return result;
            }
        }

        result.setValid(true);
        result.setStatusMessage("CRYPTOGRAPHIC CHAIN VERIFIED: Unbroken chain across " + events.size() + " immutable custody events.");
        return result;
    }
}
