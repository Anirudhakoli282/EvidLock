package com.evidlock.persistence.dao.postgres;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.dao.EvidenceItemDao;
import com.evidlock.persistence.model.EvidenceItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvidenceItemDaoPostgres implements EvidenceItemDao {

    private static EvidenceItemDaoPostgres instance = null;

    public static synchronized EvidenceItemDaoPostgres getInstance() {
        if (instance == null) {
            instance = new EvidenceItemDaoPostgres();
        }
        return instance;
    }

    private Connection getConnection() {
        return DBManager.getInstance().getConnection();
    }

    private EvidenceItem mapRow(ResultSet rs) throws SQLException {
        EvidenceItem item = new EvidenceItem();
        item.setId(rs.getLong("id"));
        item.setCaseId(rs.getLong("case_id"));
        item.setEvidenceTag(rs.getString("evidence_tag"));
        item.setDescription(rs.getString("description"));
        item.setFileHash(rs.getString("file_hash"));
        item.setOriginalFilename(rs.getString("original_filename"));
        item.setFilePath(rs.getString("file_path"));
        item.setFileType(rs.getString("file_type"));
        item.setFileSize(rs.getLong("file_size"));
        item.setUploadedBy(rs.getString("uploaded_by"));
        item.setUploadedAt(rs.getTimestamp("uploaded_at"));
        item.setCurrentCustodianId(rs.getString("current_custodian_id"));
        item.setStatus(rs.getString("status"));
        item.setLegalHold(rs.getBoolean("legal_hold"));
        item.setIntegrityStatus(rs.getString("integrity_status"));
        item.setRetentionDate(rs.getDate("retention_date"));
        item.setAgencyCode(rs.getString("agency_code"));

        // Join case number if available
        try {
            item.setCaseNumber(rs.getString("case_number"));
        } catch (SQLException ignored) {}

        return item;
    }

    private static final String BASE_SELECT =
            "SELECT e.*, c.case_number FROM evidence_items e LEFT JOIN cases c ON e.case_id = c.id ";

    @Override
    public List<EvidenceItem> getAll() {
        List<EvidenceItem> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY e.uploaded_at DESC";
        try (PreparedStatement st = getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all evidence items", e);
        }
        return list;
    }

    @Override
    public List<EvidenceItem> findByCase(Long caseId) {
        List<EvidenceItem> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE e.case_id = ? ORDER BY e.uploaded_at DESC";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, caseId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching evidence for case " + caseId, e);
        }
        return list;
    }

    @Override
    public List<EvidenceItem> findByAgency(String agencyCode) {
        List<EvidenceItem> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE e.agency_code = ? ORDER BY e.uploaded_at DESC";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, agencyCode);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching evidence for agency " + agencyCode, e);
        }
        return list;
    }

    @Override
    public List<EvidenceItem> findByCustodian(String custodianEmail) {
        List<EvidenceItem> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE e.current_custodian_id = ? ORDER BY e.uploaded_at DESC";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, custodianEmail);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching evidence for custodian " + custodianEmail, e);
        }
        return list;
    }

    @Override
    public EvidenceItem findById(Long id) {
        String sql = BASE_SELECT + "WHERE e.id = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching evidence item " + id, e);
        }
        return null;
    }

    @Override
    public EvidenceItem findByTag(String tag) {
        String sql = BASE_SELECT + "WHERE e.evidence_tag = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, tag);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching evidence item by tag " + tag, e);
        }
        return null;
    }

    @Override
    public Long insert(EvidenceItem item) {
        String sql = "INSERT INTO evidence_items (case_id, evidence_tag, description, file_hash, " +
                "original_filename, file_path, file_type, file_size, uploaded_by, current_custodian_id, " +
                "status, legal_hold, integrity_status, retention_date, agency_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, item.getCaseId());
            st.setString(2, item.getEvidenceTag());
            st.setString(3, item.getDescription());
            st.setString(4, item.getFileHash());
            st.setString(5, item.getOriginalFilename());
            st.setString(6, item.getFilePath());
            st.setString(7, item.getFileType());
            st.setLong(8, item.getFileSize() != null ? item.getFileSize() : 0L);
            st.setString(9, item.getUploadedBy());
            st.setString(10, item.getCurrentCustodianId());
            st.setString(11, item.getStatus() != null ? item.getStatus() : "Collected");
            st.setBoolean(12, item.isLegalHold());
            st.setString(13, item.getIntegrityStatus() != null ? item.getIntegrityStatus() : "VERIFIED");
            st.setDate(14, item.getRetentionDate());
            st.setString(15, item.getAgencyCode());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting evidence item", e);
        }
        return null;
    }

    @Override
    public boolean update(EvidenceItem item) {
        // ENFORCE LEGAL HOLD AT DAO LAYER
        EvidenceItem existing = findById(item.getId());
        if (existing != null && existing.isLegalHold()) {
            if ("Disposed".equalsIgnoreCase(item.getStatus()) ||
                    (item.getFilePath() != null && !item.getFilePath().equals(existing.getFilePath()))) {
                throw new IllegalStateException("Operation blocked: Evidence item " + existing.getEvidenceTag() +
                        " is under active Legal Hold and cannot be disposed or modified.");
            }
        }

        String sql = "UPDATE evidence_items SET description = ?, status = ?, legal_hold = ?, " +
                "integrity_status = ?, retention_date = ?, agency_code = ?, file_type = ?, original_filename = ? " +
                "WHERE id = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, item.getDescription());
            st.setString(2, item.getStatus());
            st.setBoolean(3, item.isLegalHold());
            st.setString(4, item.getIntegrityStatus());
            st.setDate(5, item.getRetentionDate());
            st.setString(6, item.getAgencyCode());
            st.setString(7, item.getFileType());
            st.setString(8, item.getOriginalFilename());
            st.setLong(9, item.getId());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating evidence item " + item.getId(), e);
        }
    }

    @Override
    public boolean delete(Long id) {
        // ENFORCE LEGAL HOLD AT DAO LAYER
        EvidenceItem existing = findById(id);
        if (existing != null && existing.isLegalHold()) {
            throw new IllegalStateException("Operation blocked: Evidence item " + existing.getEvidenceTag() +
                    " is under active Legal Hold and cannot be deleted.");
        }

        String sql = "DELETE FROM evidence_items WHERE id = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting evidence item " + id, e);
        }
    }

    @Override
    public boolean updateCustodian(Long id, String newCustodianEmail, String newStatus) {
        String sql = "UPDATE evidence_items SET current_custodian_id = ?, status = ? WHERE id = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, newCustodianEmail);
            st.setString(2, newStatus);
            st.setLong(3, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating custodian for evidence " + id, e);
        }
    }

    @Override
    public boolean updateLegalHold(Long id, boolean hold) {
        String sql = "UPDATE evidence_items SET legal_hold = ? WHERE id = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setBoolean(1, hold);
            st.setLong(2, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating legal hold for evidence " + id, e);
        }
    }

    @Override
    public boolean updateIntegrityStatus(Long id, String status) {
        String sql = "UPDATE evidence_items SET integrity_status = ? WHERE id = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, status);
            st.setLong(2, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating integrity status for evidence " + id, e);
        }
    }

    @Override
    public List<EvidenceItem> findEligibleForDisposal() {
        List<EvidenceItem> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE e.retention_date <= CURRENT_DATE AND e.legal_hold = FALSE AND e.status <> 'Disposed' ORDER BY e.retention_date ASC";
        try (PreparedStatement st = getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching disposal eligible evidence", e);
        }
        return list;
    }

    @Override
    public List<EvidenceItem> findNearingRetention(int days) {
        List<EvidenceItem> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE e.retention_date BETWEEN CURRENT_DATE AND (CURRENT_DATE + (? || ' days')::interval) " +
                "AND e.status <> 'Disposed' ORDER BY e.retention_date ASC";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setInt(1, days);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching evidence nearing retention", e);
        }
        return list;
    }

    @Override
    public List<EvidenceItem> findIntegrityCompromised() {
        List<EvidenceItem> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE e.integrity_status = 'COMPROMISED' ORDER BY e.uploaded_at DESC";
        try (PreparedStatement st = getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching compromised evidence", e);
        }
        return list;
    }

    @Override
    public List<EvidenceItem> search(List<String> keywords) {
        List<EvidenceItem> list = new ArrayList<>();
        if (keywords == null || keywords.isEmpty()) {
            return getAll();
        }

        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE ");
        for (int i = 0; i < keywords.size(); i++) {
            sql.append("(e.evidence_tag ILIKE ? OR e.description ILIKE ? OR e.original_filename ILIKE ? OR c.case_number ILIKE ?)");
            if (i < keywords.size() - 1) {
                sql.append(" OR ");
            }
        }
        sql.append(" ORDER BY e.uploaded_at DESC");

        try (PreparedStatement st = getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (String kw : keywords) {
                String pattern = "%" + kw + "%";
                st.setString(paramIndex++, pattern);
                st.setString(paramIndex++, pattern);
                st.setString(paramIndex++, pattern);
                st.setString(paramIndex++, pattern);
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching evidence items", e);
        }
        return list;
    }
}
