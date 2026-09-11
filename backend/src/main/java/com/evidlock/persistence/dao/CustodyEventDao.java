package com.evidlock.persistence.dao;

import com.evidlock.persistence.model.CustodyEvent;

import java.util.List;

/**
 * APPEND-ONLY DAO for Custody Events.
 * By design and policy, NO UPDATE OR DELETE operations are exposed or supported.
 */
public interface CustodyEventDao {
    Long insert(CustodyEvent event);
    CustodyEvent findById(Long id);
    List<CustodyEvent> findByEvidenceItem(Long evidenceItemId);
    CustodyEvent findLatestByEvidenceItem(Long evidenceItemId);
    List<CustodyEvent> getAll();
    List<CustodyEvent> findRecent(int limit);
}
