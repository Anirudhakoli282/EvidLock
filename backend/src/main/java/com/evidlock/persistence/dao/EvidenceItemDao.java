package com.evidlock.persistence.dao;

import com.evidlock.persistence.model.EvidenceItem;

import java.util.List;

public interface EvidenceItemDao {
    List<EvidenceItem> getAll();
    List<EvidenceItem> findByCase(Long caseId);
    List<EvidenceItem> findByAgency(String agencyCode);
    List<EvidenceItem> findByCustodian(String custodianEmail);
    EvidenceItem findById(Long id);
    EvidenceItem findByTag(String tag);
    Long insert(EvidenceItem item);
    boolean update(EvidenceItem item);
    boolean delete(Long id);
    boolean updateCustodian(Long id, String newCustodianEmail, String newStatus);
    boolean updateLegalHold(Long id, boolean hold);
    boolean updateIntegrityStatus(Long id, String status);
    List<EvidenceItem> findEligibleForDisposal();
    List<EvidenceItem> findNearingRetention(int days);
    List<EvidenceItem> findIntegrityCompromised();
    List<EvidenceItem> search(List<String> keywords);
}
