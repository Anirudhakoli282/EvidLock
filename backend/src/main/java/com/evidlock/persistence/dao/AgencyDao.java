package com.evidlock.persistence.dao;

import com.evidlock.persistence.model.Agency;

import java.util.List;

public interface AgencyDao {
    List<Agency> getAll();
    Agency findByCode(String agencyCode);
    Agency findByEmail(String email);
    boolean insert(Agency agency);
    boolean update(Agency agency);
}
