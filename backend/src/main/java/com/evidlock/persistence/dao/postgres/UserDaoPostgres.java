package com.evidlock.persistence.dao.postgres;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.dao.UserDao;
import com.evidlock.persistence.model.*;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDaoPostgres implements UserDao {

    private static Connection conn = null;
    private static UserDaoPostgres instance = null;

    public static synchronized UserDaoPostgres getInstance() {
        if (instance == null) {
            instance = new UserDaoPostgres();
            conn = DBManager.getInstance().getConnection();
        }
        return instance;
    }

    private User createProxyInstance(String role) {
        if (role == null) return new User();
        switch (role.toUpperCase()) {
            case "SUPERVISOR":
            case "A":
                return new UserSupervisor();
            case "INVESTIGATOR":
                return new UserInvestigator();
            case "CUSTODIAN":
            case "C":
                return new UserCustodian();
            case "AUDITOR":
                return new UserAuditor();
            default:
                return new User();
        }
    }

    private User mapRow(ResultSet rs, boolean asProxy) throws SQLException {
        String role = rs.getString("role");
        User u = asProxy ? createProxyInstance(role) : new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setName(rs.getString("name"));
        u.setBadgeNumber(rs.getString("badge_number"));
        u.setRole(role);
        u.setAgencyCode(rs.getString("agency_code"));
        u.setPhone(rs.getString("phone"));
        u.setPIva(rs.getString("piva"));
        return u;
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs, false);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email " + email, e);
        }
        return null;
    }

    @Override
    public User findByToken(String token) {
        if (token == null || token.isBlank()) return null;
        String email = Utility.getTokenEmail(token);
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs, true); // Instantiate concrete Security Proxy
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by token", e);
        }
        return null;
    }

    @Override
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs, false);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id " + id, e);
        }
        return null;
    }

    @Override
    public List<User> findByAgency(String agencyCode) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE agency_code = ? ORDER BY name ASC";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, agencyCode);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, false));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding users by agency " + agencyCode, e);
        }
        return list;
    }

    @Override
    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY name ASC";
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs, false));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting all users", e);
        }
        return list;
    }

    @Override
    public boolean insert(User user) {
        String sql = "INSERT INTO users (email, password, name, badge_number, role, agency_code, phone, piva) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, user.getEmail());
            st.setString(2, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10)));
            st.setString(3, user.getName());
            st.setString(4, user.getBadgeNumber());
            st.setString(5, user.getRole());
            st.setString(6, user.getAgencyCode());
            st.setString(7, user.getPhone());
            st.setString(8, user.getPIva());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting user", e);
        }
    }

    @Override
    public boolean updatePassword(User user) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10)));
            st.setString(2, user.getEmail());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating password for user " + user.getEmail(), e);
        }
    }

    @Override
    public boolean updateEmail(User user) {
        String sql = "UPDATE users SET email = ? WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, user.getEmail());
            st.setLong(2, user.getId());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating email", e);
        }
    }
}
