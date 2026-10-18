package com.evidlock.persistence;

import com.evidlock.persistence.dao.*;
import com.evidlock.persistence.dao.postgres.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBManager {
    private static DBManager instance = null;
    private Connection conn = null;

    private DBManager() {}

    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ignored) {
        }
    }

    public synchronized Connection getConnection() {
        try {
            boolean isDead = (conn == null);
            if (!isDead) {
                try {
                    isDead = conn.isClosed() || !conn.isValid(3);
                } catch (Exception e) {
                    isDead = true;
                }
            }

            if (isDead) {
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                } catch (Exception ignored) {}

                Properties properties = loadProperties();
                String url = getPropertyOrEnv(properties, "db.url", "DB_URL");
                String user = getPropertyOrEnv(properties, "db.user", "DB_USER");
                String password = getPropertyOrEnv(properties, "db.password", "DB_PASSWORD");
                conn = DriverManager.getConnection(url, user, password);

                // Run a non-blocking check to ensure seed users exist
                ensureSeedUsers(conn);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish PostgreSQL connection: " + e.getMessage(), e);
        }
        return conn;
    }

    public synchronized void forceReconnect() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (Exception ignored) {}
        conn = null;
        getConnection();
    }

    private void ensureSeedUsers(Connection c) {
        try (Statement st = c.createStatement()) {
            st.executeUpdate(
                "INSERT INTO agencies (agency_code, name, email, phone, address, badge_prefix, logo) VALUES " +
                "('MPD-01', 'Mumbai Police - Cyber & Economic Offences Wing', 'cybercrimes@mumbaipolice.gov.in', '+91-22-2262-1855', 'Crawford Market, D.N. Road, Mumbai, Maharashtra 400001', 'MPD', 'assets/img/agency-mpd.png'), " +
                "('SFL-02', 'Forensic Science Laboratory - Maharashtra', 'fsl.lab@maharashtra.gov.in', '+91-20-2612-4900', 'Plot No. 17, Aundh, Pune, Maharashtra 411007', 'SFL', 'assets/img/agency-sfl.png'), " +
                "('DAO-03', 'Central Bureau of Investigation - Mumbai Branch', 'cbi.mumbai@cbi.gov.in', '+91-22-2262-3535', 'Aayakar Bhavan, M.K. Road, Mumbai, Maharashtra 400020', 'DAO', 'assets/img/agency-dao.png'), " +
                "('OIG-04', 'National Investigation Agency - Evidence Audit Cell', 'nia.audit@nia.gov.in', '+91-11-2436-5729', 'CGO Complex, Lodhi Road, New Delhi 110003', 'OIG', 'assets/img/agency-oig.png') " +
                "ON CONFLICT (agency_code) DO NOTHING;"
            );

            st.executeUpdate(
                "INSERT INTO users (email, password, name, badge_number, role, agency_code, phone, piva) VALUES " +
                "('supervisor@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'ACP Rajesh Patil', 'MPD-SUP-101', 'SUPERVISOR', 'MPD-01', '+91-98201-10001', '36710385611'), " +
                "('investigator@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'PI Priya Sharma', 'MPD-INV-402', 'INVESTIGATOR', 'MPD-01', '+91-98202-10002', '23267419900'), " +
                "('custodian@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'Dr. Vikram Desai', 'SFL-CST-881', 'CUSTODIAN', 'SFL-02', '+91-98203-10003', '19833552187'), " +
                "('auditor@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'Ananya Mehta', 'OIG-AUD-909', 'AUDITOR', 'OIG-04', '+91-98204-10004', '98714353456') " +
                "ON CONFLICT (email) DO UPDATE SET password = EXCLUDED.password, role = EXCLUDED.role, name = EXCLUDED.name, agency_code = EXCLUDED.agency_code;"
            );
        } catch (Exception ignored) {
            // Table structure might differ slightly or already populated; continue safely
        }
    }

    private String getPropertyOrEnv(Properties props, String propKey, String envKey) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) {
            return envVal.trim();
        }
        String val = props.getProperty(propKey);
        if (val != null) {
            val = val.trim();
            if (val.startsWith("${") && val.endsWith("}")) {
                String inner = val.substring(2, val.length() - 1);
                int colonIdx = inner.indexOf(':');
                if (colonIdx != -1) {
                    String envName = inner.substring(0, colonIdx);
                    String defVal = inner.substring(colonIdx + 1);
                    String sysEnv = System.getenv(envName);
                    return (sysEnv != null && !sysEnv.isBlank()) ? sysEnv.trim() : defVal.trim();
                } else {
                    String sysEnv = System.getenv(inner);
                    return (sysEnv != null && !sysEnv.isBlank()) ? sysEnv.trim() : "";
                }
            }
            return val;
        }
        return "";
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading application.properties", e);
        }
        return properties;
    }

    public AgencyDao getAgencyDao() {
        return AgencyDaoPostgres.getInstance();
    }

    public UserDao getUserDao() {
        return UserDaoPostgres.getInstance();
    }

    public CaseDao getCaseDao() {
        return CaseDaoPostgres.getInstance();
    }

    public EvidenceItemDao getEvidenceItemDao() {
        return EvidenceItemDaoPostgres.getInstance();
    }

    public CustodyEventDao getCustodyEventDao() {
        return CustodyEventDaoPostgres.getInstance();
    }

    public CustodyTransferRequestDao getCustodyTransferRequestDao() {
        return CustodyTransferRequestDaoPostgres.getInstance();
    }

    public CertificationTrainingDao getCertificationTrainingDao() {
        return CertificationTrainingDaoPostgres.getInstance();
    }
}
