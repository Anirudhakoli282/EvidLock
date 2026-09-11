package com.evidlock.persistence.dao;

import com.evidlock.persistence.model.CustodyTransferRequest;

import java.sql.Timestamp;
import java.util.List;

public interface CustodyTransferRequestDao {
    Long insert(CustodyTransferRequest request);
    CustodyTransferRequest findById(Long id);
    List<CustodyTransferRequest> findByEvidenceItem(Long evidenceItemId);
    List<CustodyTransferRequest> findPendingForCustodian(String custodianEmail);
    List<CustodyTransferRequest> getAllPending();
    List<CustodyTransferRequest> getAll();
    boolean updateStatus(Long id, String status, Timestamp timestamp, boolean isAccepted);
}
