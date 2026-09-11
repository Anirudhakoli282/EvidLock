package com.evidlock.persistence.dao;

import com.evidlock.persistence.model.Case;

import java.util.List;

public interface CaseDao {
    List<Case> getAll();
    List<Case> findByAgency(String agencyCode);
    Case findById(Long id);
    Case findByCaseNumber(String caseNumber);
    Long insert(Case c);
    boolean update(Case c);
    boolean updateStatus(Long id, String status);
    long countByStatus(String status);
}
