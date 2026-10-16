package com.evidlock.controller;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://*.vercel.app", "https://*.onrender.com"}, allowCredentials = "true")
public class Auth {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "UP");
        map.put("timestamp", System.currentTimeMillis());
        try {
            Connection conn = DBManager.getInstance().getConnection();
            map.put("dbConnected", conn != null && !conn.isClosed());
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT count(*) FROM users")) {
                if (rs.next()) {
                    map.put("usersTableExists", true);
                    map.put("usersCount", rs.getInt(1));
                }
            } catch (Exception ex) {
                map.put("usersTableError", ex.getMessage());
            }
        } catch (Exception e) {
            map.put("dbError", e.getMessage());
        }
        return ResponseEntity.ok(map);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User credentials, HttpServletRequest req) {
        if (credentials == null || credentials.getEmail() == null || credentials.getPassword() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            User storedUser = DBManager.getInstance().getUserDao().findByEmail(credentials.getEmail().trim());
            if (storedUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            if (!Utility.checkPassword(credentials.getPassword(), storedUser)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("user", storedUser);

            AuthToken auth = generateToken(storedUser);
            return new ResponseEntity<>(auth, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> err = new HashMap<>();
            err.put("error", "Database error: " + e.getMessage());
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cambio-password")
    public ResponseEntity<String> cambioPassword(HttpServletRequest req, @RequestBody Password password) {
        User user = Utility.getRequestUser(req);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return user.cambioPassword(password.getOldPw(), password.getNewPw());
    }

    private AuthToken generateToken(User user) {
        String role = user.getRole() != null ? user.getRole().toUpperCase() : "INVESTIGATOR";
        String token = Utility.encodeBase64(user.getEmail() + ":tkn:" +
                BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10)) + ":tkn:" + role);

        AuthToken auth = new AuthToken();
        auth.setToken(token);
        auth.setRole(role);
        auth.setName(user.getName());
        auth.setAgencyCode(user.getAgencyCode());
        return auth;
    }
}
