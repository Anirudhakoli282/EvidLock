package com.evidlock.persistence.dao.postgres;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.dao.CaseDao;
import com.evidlock.persistence.model.Case;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CaseDaoPostgres implements CaseDao {

    private static Connection conn = null;
    private static CaseDaoPostgres instance = null;

    public static synchronized CaseDaoPostgres getInstance() {
        if (instance == null) {
            instance = new CaseDaoPostgres();
            conn = DBManager.getInstance().getConnection();
        }
        return instance;
    }

    private Case mapRow(ResultSet rs) throws SQLException {
        Case c = new Case();
        c.setId(rs.getLong("id"));
        c.setCaseNumber(rs.getString("case_number"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setStatus(rs.getString("status"));
        c.setAgencyCode(rs.getString("agency_code"));
        c.setCreatedBy(rs.getString("created_by"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }

    @Override
    public List<Case> getAll() {
        List<Case> list = new ArrayList<>();
        String sql = "SELECT * FROM cases ORDER BY created_at DESC";
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching cases", e);
        }
        return list;
    }

    @Override
    public List<Case> findByAgency(String agencyCode) {
        List<Case> list = new ArrayList<>();
        String sql = "SELECT * FROM cases WHERE agency_code = ? ORDER BY created_at DESC";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, agencyCode);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching cases by agency " + agencyCode, e);
        }
        return list;
    }

    @Override
    public Case findById(Long id) {
        String sql = "SELECT * FROM cases WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching case by id " + id, e);
        }
        return null;
    }

    @Override
    public Case findByCaseNumber(String caseNumber) {
        String sql = "SELECT * FROM cases WHERE case_number = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, caseNumber);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching case by case_number " + caseNumber, e);
        }
        return null;
    }

    @Override
    public Long insert(Case c) {
        String sql = "INSERT INTO cases (case_number, title, description, status, agency_code, created_by) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, c.getCaseNumber());
            st.setString(2, c.getTitle());
            st.setString(3, c.getDescription());
            st.setString(4, c.getStatus() != null ? c.getStatus() : "Open");
            st.setString(5, c.getAgencyCode());
            st.setString(6, c.getCreatedBy());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting case", e);
        }
        return null;
    }

    @Override
    public boolean update(Case c) {
        String sql = "UPDATE cases SET title = ?, description = ?, status = ?, agency_code = ? WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, c.getTitle());
            st.setString(2, c.getDescription());
            st.setString(3, c.getStatus());
            st.setString(4, c.getAgencyCode());
            st.setLong(5, c.getId());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating case " + c.getId(), e);
        }
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        String sql = "UPDATE cases SET status = ? WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, status);
            st.setLong(2, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating status for case " + id, e);
        }
    }

    @Override
    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM cases WHERE status = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, status);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting cases by status", e);
        }
        return 0;
    }
}
