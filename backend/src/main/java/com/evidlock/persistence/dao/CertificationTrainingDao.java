package com.evidlock.persistence.dao;

import com.evidlock.persistence.model.CertificationTraining;

import java.util.List;

public interface CertificationTrainingDao {
    List<CertificationTraining> getAll();
    CertificationTraining findById(Long id);
    List<CertificationTraining> findByAgency(String agencyCode);
    Long insert(CertificationTraining training);
}
