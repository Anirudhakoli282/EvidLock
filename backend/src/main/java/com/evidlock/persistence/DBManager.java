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

    public synchronized Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                Properties properties = loadProperties();
                conn = DriverManager.getConnection(
                        properties.getProperty("db.url"),
                        properties.getProperty("db.user"),
                        properties.getProperty("db.password")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish PostgreSQL connection", e);
        }
        return conn;
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
