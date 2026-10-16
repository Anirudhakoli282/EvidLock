package com.evidlock.persistence;

import com.evidlock.persistence.dao.*;
import com.evidlock.persistence.dao.postgres.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
            if (conn == null || conn.isClosed()) {
                Properties properties = loadProperties();
                String url = getPropertyOrEnv(properties, "db.url", "DB_URL");
                String user = getPropertyOrEnv(properties, "db.user", "DB_USER");
                String password = getPropertyOrEnv(properties, "db.password", "DB_PASSWORD");
                conn = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish PostgreSQL connection: " + e.getMessage(), e);
        }
        return conn;
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
