package com.evidlock.persistence.dao.postgres;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.dao.AgencyDao;
import com.evidlock.persistence.model.Agency;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AgencyDaoPostgres implements AgencyDao {

    private static Connection conn = null;
    private static AgencyDaoPostgres instance = null;

    public static synchronized AgencyDaoPostgres getInstance() {
        if (instance == null) {
            instance = new AgencyDaoPostgres();
            conn = DBManager.getInstance().getConnection();
        }
        return instance;
    }

    private Agency mapRow(ResultSet rs) throws SQLException {
        Agency a = new Agency();
        a.setAgencyCode(rs.getString("agency_code"));
        a.setName(rs.getString("name"));
        a.setEmail(rs.getString("email"));
        a.setPhone(rs.getString("phone"));
        a.setAddress(rs.getString("address"));
        a.setBadgePrefix(rs.getString("badge_prefix"));
        a.setLogo(rs.getString("logo"));
        return a;
    }

    @Override
    public List<Agency> getAll() {
        List<Agency> list = new ArrayList<>();
        String sql = "SELECT * FROM agencies ORDER BY name ASC";
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching agencies", e);
        }
        return list;
    }

    @Override
    public Agency findByCode(String agencyCode) {
        String sql = "SELECT * FROM agencies WHERE agency_code = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, agencyCode);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching agency by code " + agencyCode, e);
        }
        return null;
    }

    @Override
    public Agency findByEmail(String email) {
        String sql = "SELECT * FROM agencies WHERE email = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching agency by email " + email, e);
        }
        return null;
    }

    @Override
    public boolean insert(Agency agency) {
        String sql = "INSERT INTO agencies (agency_code, name, email, phone, address, badge_prefix, logo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, agency.getAgencyCode());
            st.setString(2, agency.getName());
            st.setString(3, agency.getEmail());
            st.setString(4, agency.getPhone());
            st.setString(5, agency.getAddress());
            st.setString(6, agency.getBadgePrefix());
            st.setString(7, agency.getLogo());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting agency", e);
        }
    }

    @Override
    public boolean update(Agency agency) {
        String sql = "UPDATE agencies SET name = ?, email = ?, phone = ?, address = ?, badge_prefix = ?, logo = ? WHERE agency_code = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, agency.getName());
            st.setString(2, agency.getEmail());
            st.setString(3, agency.getPhone());
            st.setString(4, agency.getAddress());
            st.setString(5, agency.getBadgePrefix());
            st.setString(6, agency.getLogo());
            st.setString(7, agency.getAgencyCode());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating agency", e);
        }
    }
}
