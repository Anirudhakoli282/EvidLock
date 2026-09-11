package com.evidlock.service.scheduler;

import com.evidlock.controller.EmailSender;
import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.EvidenceItem;
import com.evidlock.persistence.model.User;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@EnableScheduling
public class RetentionAlertScheduler {

    /**
     * Scheduled retention policy check.
     * Runs daily at 08:00 AM, and can also be triggered manually via API.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public int checkRetentionAndNotifySupervisors() {
        List<EvidenceItem> eligibleForDisposal = DBManager.getInstance().getEvidenceItemDao().findEligibleForDisposal();
        List<EvidenceItem> nearingRetention = DBManager.getInstance().getEvidenceItemDao().findNearingRetention(30);

        if (eligibleForDisposal.isEmpty() && nearingRetention.isEmpty()) {
            return 0;
        }

        // Fetch all supervisors
        List<User> supervisors = DBManager.getInstance().getUserDao().getAll().stream()
                .filter(u -> "SUPERVISOR".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());

        for (User sup : supervisors) {
            sendRetentionAlertEmail(sup.getEmail(), eligibleForDisposal, nearingRetention);
        }

        return eligibleForDisposal.size() + nearingRetention.size();
    }

    public boolean sendRetentionAlertEmail(String supervisorEmail, List<EvidenceItem> eligible, List<EvidenceItem> nearing) {
        StringBuilder body = new StringBuilder();
        body.append("EVIDLOCK EVIDENCE RETENTION & DISPOSAL ALERT\n\n");
        body.append("Dear Supervisor,\n\nThe system has identified evidence items requiring review per statutory retention guidelines:\n\n");

        if (!eligible.isEmpty()) {
            body.append("=== EVIDENCE ELIGIBLE FOR DISPOSAL (Non-Held, Passed Retention Date) ===\n");
            for (EvidenceItem item : eligible) {
                body.append("• Tag: ").append(item.getEvidenceTag())
                    .append(" | File: ").append(item.getOriginalFilename())
                    .append(" | Retention Date: ").append(item.getRetentionDate())
                    .append(" | Custodian: ").append(item.getCurrentCustodianId())
                    .append("\n");
            }
            body.append("\n");
        }

        if (!nearing.isEmpty()) {
            body.append("=== EVIDENCE NEARING RETENTION DEADLINE (Next 30 Days) ===\n");
            for (EvidenceItem item : nearing) {
                body.append("• Tag: ").append(item.getEvidenceTag())
                    .append(" | File: ").append(item.getOriginalFilename())
                    .append(" | Retention Date: ").append(item.getRetentionDate())
                    .append("\n");
            }
            body.append("\n");
        }

        body.append("Please log into EvidLock to review these items and authorize disposal or extend legal holds.\n\n");
        body.append("EvidLock Automated Compliance Monitor");

        return EmailSender.sendConfirmationMail(supervisorEmail, null, "[ACTION REQUIRED] EvidLock Evidence Retention Alert", body.toString());
    }
}
