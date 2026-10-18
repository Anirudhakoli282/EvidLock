package com.evidlock.persistence.dao.postgres;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.dao.CustodyEventDao;
import com.evidlock.persistence.model.CustodyEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * APPEND-ONLY implementation of CustodyEventDao.
 * Modifying or deleting custody events is blocked both here and by database triggers.
 */
public class CustodyEventDaoPostgres implements CustodyEventDao {

    private static CustodyEventDaoPostgres instance = null;

    public static synchronized CustodyEventDaoPostgres getInstance() {
        if (instance == null) {
            instance = new CustodyEventDaoPostgres();
        }
        return instance;
    }

    private Connection getConnection() {
        return DBManager.getInstance().getConnection();
    }

    private CustodyEvent mapRow(ResultSet rs) throws SQLException {
        CustodyEvent e = new CustodyEvent();
        e.setId(rs.getLong("id"));
        e.setEvidenceItemId(rs.getLong("evidence_item_id"));
        e.setActorId(rs.getString("actor_id"));
        e.setAction(rs.getString("action"));
        e.setTimestamp(rs.getTimestamp("timestamp"));
        e.setFromCustodianId(rs.getString("from_custodian_id"));
        e.setToCustodianId(rs.getString("to_custodian_id"));
        e.setNotes(rs.getString("notes"));
        e.setPreviousEventHash(rs.getString("previous_event_hash"));
        e.setEventHash(rs.getString("event_hash"));
        return e;
    }

    @Override
    public Long insert(CustodyEvent event) {
        String sql = "INSERT INTO custody_events (evidence_item_id, actor_id, action, timestamp, " +
                "from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, event.getEvidenceItemId());
            st.setString(2, event.getActorId());
            st.setString(3, event.getAction());
            st.setTimestamp(4, event.getTimestamp());
            st.setString(5, event.getFromCustodianId());
            st.setString(6, event.getToCustodianId());
            st.setString(7, event.getNotes());
            st.setString(8, event.getPreviousEventHash());
            st.setString(9, event.getEventHash());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error appending custody event to ledger", e);
        }
        return null;
    }

    @Override
    public CustodyEvent findById(Long id) {
        String sql = "SELECT * FROM custody_events WHERE id = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching custody event " + id, e);
        }
        return null;
    }

    @Override
    public List<CustodyEvent> findByEvidenceItem(Long evidenceItemId) {
        List<CustodyEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM custody_events WHERE evidence_item_id = ? ORDER BY id ASC";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, evidenceItemId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching custody chain for evidence " + evidenceItemId, e);
        }
        return list;
    }

    @Override
    public CustodyEvent findLatestByEvidenceItem(Long evidenceItemId) {
        String sql = "SELECT * FROM custody_events WHERE evidence_item_id = ? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setLong(1, evidenceItemId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching latest event for evidence " + evidenceItemId, e);
        }
        return null;
    }

    @Override
    public List<CustodyEvent> getAll() {
        List<CustodyEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM custody_events ORDER BY id DESC";
        try (PreparedStatement st = getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all custody events", e);
        }
        return list;
    }

    @Override
    public List<CustodyEvent> findRecent(int limit) {
        List<CustodyEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM custody_events ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setInt(1, limit);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching recent custody events", e);
        }
        return list;
    }
}
