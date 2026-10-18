package com.evidlock.persistence.dao.postgres;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.dao.CertificationTrainingDao;
import com.evidlock.persistence.model.CertificationTraining;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CertificationTrainingDaoPostgres implements CertificationTrainingDao {

    private static CertificationTrainingDaoPostgres instance = null;

    public static synchronized CertificationTrainingDaoPostgres getInstance() {
        if (instance == null) {
            instance = new CertificationTrainingDaoPostgres();
        }
        return instance;
    }

    private Connection getConnection() {
        return DBManager.getInstance().getConnection();
    }

    private CertificationTraining mapRow(ResultSet rs) throws SQLException {
        CertificationTraining t = new CertificationTraining();
        t.setId(rs.getLong("id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setDurationHours(rs.getInt("duration_hours"));
        t.setCategory(rs.getString("category"));
        t.setRequiredRole(rs.getString("required_role"));
        t.setAgencyCode(rs.getString("agency_code"));
        return t;
    }

    @Override
    public List<CertificationTraining> getAll() {
        List<CertificationTraining> list = new ArrayList<>();
        String sql = "SELECT * FROM certification_trainings ORDER BY title ASC";
        try (PreparedStatement st = getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting certification trainings", e);
        }
        return list;
    }

    @Override
    public CertificationTraining findById(Long id) {
        String sql = "SELECT * FROM certification_trainings WHERE id = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting training by id " + id, e);
        }
        return null;
    }

    @Override
    public List<CertificationTraining> findByAgency(String agencyCode) {
        List<CertificationTraining> list = new ArrayList<>();
        String sql = "SELECT * FROM certification_trainings WHERE agency_code = ? ORDER BY title ASC";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, agencyCode);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting trainings by agency " + agencyCode, e);
        }
        return list;
    }

    @Override
    public Long insert(CertificationTraining training) {
        String sql = "INSERT INTO certification_trainings (title, description, duration_hours, category, required_role, agency_code) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, training.getTitle());
            st.setString(2, training.getDescription());
            st.setInt(3, training.getDurationHours());
            st.setString(4, training.getCategory());
            st.setString(5, training.getRequiredRole());
            st.setString(6, training.getAgencyCode());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting training", e);
        }
        return null;
    }
}
