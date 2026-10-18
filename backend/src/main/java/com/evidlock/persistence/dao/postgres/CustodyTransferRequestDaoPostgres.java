package com.evidlock.persistence.dao.postgres;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.dao.CustodyTransferRequestDao;
import com.evidlock.persistence.model.CustodyTransferRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustodyTransferRequestDaoPostgres implements CustodyTransferRequestDao {

    private static CustodyTransferRequestDaoPostgres instance = null;

    public static synchronized CustodyTransferRequestDaoPostgres getInstance() {
        if (instance == null) {
            instance = new CustodyTransferRequestDaoPostgres();
        }
        return instance;
    }

    private Connection getConnection() {
        return DBManager.getInstance().getConnection();
    }

    private CustodyTransferRequest mapRow(ResultSet rs) throws SQLException {
        CustodyTransferRequest r = new CustodyTransferRequest();
        r.setId(rs.getLong("id"));
        r.setEvidenceItemId(rs.getLong("evidence_item_id"));
        r.setFromCustodianId(rs.getString("from_custodian_id"));
        r.setToCustodianId(rs.getString("to_custodian_id"));
        r.setReason(rs.getString("reason"));
        r.setStatus(rs.getString("status"));
        r.setReleasedAt(rs.getTimestamp("released_at"));
        r.setAcceptedAt(rs.getTimestamp("accepted_at"));
        r.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            r.setEvidenceTag(rs.getString("evidence_tag"));
            r.setEvidenceDescription(rs.getString("description"));
        } catch (SQLException ignored) {}

        return r;
    }

    private static final String BASE_SELECT =
            "SELECT r.*, e.evidence_tag, e.description FROM custody_transfer_requests r " +
            "LEFT JOIN evidence_items e ON r.evidence_item_id = e.id ";

    @Override
    public Long insert(CustodyTransferRequest request) {
        String sql = "INSERT INTO custody_transfer_requests (evidence_item_id, from_custodian_id, to_custodian_id, " +
                "reason, status, released_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, request.getEvidenceItemId());
            st.setString(2, request.getFromCustodianId());
            st.setString(3, request.getToCustodianId());
            st.setString(4, request.getReason());
            st.setString(5, request.getStatus() != null ? request.getStatus() : "PENDING");
            st.setTimestamp(6, request.getReleasedAt() != null ? request.getReleasedAt() : new Timestamp(System.currentTimeMillis()));
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting custody transfer request", e);
        }
        return null;
    }

    @Override
    public CustodyTransferRequest findById(Long id) {
        String sql = BASE_SELECT + "WHERE r.id = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding transfer request " + id, e);
        }
        return null;
    }

    @Override
    public List<CustodyTransferRequest> findByEvidenceItem(Long evidenceItemId) {
        List<CustodyTransferRequest> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE r.evidence_item_id = ? ORDER BY r.created_at DESC";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, evidenceItemId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding transfer requests for evidence " + evidenceItemId, e);
        }
        return list;
    }

    @Override
    public List<CustodyTransferRequest> findPendingForCustodian(String custodianEmail) {
        List<CustodyTransferRequest> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE (r.to_custodian_id = ? OR r.from_custodian_id = ?) AND r.status = 'PENDING' ORDER BY r.created_at DESC";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, custodianEmail);
            st.setString(2, custodianEmail);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding pending requests for " + custodianEmail, e);
        }
        return list;
    }

    @Override
    public List<CustodyTransferRequest> getAllPending() {
        List<CustodyTransferRequest> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE r.status = 'PENDING' ORDER BY r.created_at DESC";
        try (PreparedStatement st = getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting all pending transfer requests", e);
        }
        return list;
    }

    @Override
    public List<CustodyTransferRequest> getAll() {
        List<CustodyTransferRequest> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY r.created_at DESC";
        try (PreparedStatement st = getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting all transfer requests", e);
        }
        return list;
    }

    @Override
    public boolean updateStatus(Long id, String status, Timestamp timestamp, boolean isAccepted) {
        String sql;
        if (isAccepted) {
            sql = "UPDATE custody_transfer_requests SET status = ?, accepted_at = ? WHERE id = ?";
        } else {
            sql = "UPDATE custody_transfer_requests SET status = ? WHERE id = ?";
        }
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, status);
            if (isAccepted) {
                st.setTimestamp(2, timestamp != null ? timestamp : new Timestamp(System.currentTimeMillis()));
                st.setLong(3, id);
            } else {
                st.setLong(2, id);
            }
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating transfer request status", e);
        }
    }
}
