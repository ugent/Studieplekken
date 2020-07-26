package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.IPenaltyEventsDao;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.penalty.Penalty;
import be.ugent.blok2.model.penalty.PenaltyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("!dummy")
public class DBPenaltyEventsDao extends ADB implements IPenaltyEventsDao {

    @Override
    public List<PenaltyEvent> getPenaltyEvents() throws SQLException {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(databaseProperties.getString("get_penalty_events"));

            Map<Integer, PenaltyEvent> events = new HashMap<>();

            while (rs.next()) {
                int code = rs.getInt(databaseProperties.getString("penalty_event_code"));
                PenaltyEvent e = events.get(code);
                if (e == null) {
                    e = new PenaltyEvent();
                    e.setCode(code);
                    e.setPoints(rs.getInt(databaseProperties.getString("penalty_event_points")));
                    e.setPublicAccessible(rs.getBoolean(databaseProperties.getString("penalty_event_public_accessible")));
                    e.setDescriptions(new HashMap<>());
                    events.put(code, e);
                }

                Language lang = Language.valueOf(rs.getString(databaseProperties.getString("penalty_description_lang_enum")));
                e.getDescriptions().put(lang, rs.getString(databaseProperties.getString("penalty_description_description")));
            }

            return new ArrayList<>(events.values());
        }
    }

    @Override
    public PenaltyEvent getPenaltyEvent(int code) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_penalty_event"));
            pstmt.setInt(1, code);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                PenaltyEvent ret = new PenaltyEvent();
                ret.setCode(code);
                ret.setPoints(rs.getInt(databaseProperties.getString("penalty_event_points")));
                ret.setPublicAccessible(rs.getBoolean(databaseProperties.getString("penalty_event_public_accessible")));
                ret.setDescriptions(new HashMap<>());

                Language lang = Language.valueOf(rs.getString(databaseProperties.getString("penalty_description_lang_enum")));
                ret.getDescriptions().put(lang, rs.getString(databaseProperties.getString("penalty_description_description")));
                while (rs.next()) {
                    lang = Language.valueOf(rs.getString(databaseProperties.getString("penalty_description_lang_enum")));
                    ret.getDescriptions().put(lang, rs.getString(databaseProperties.getString("penalty_description_description")));
                }

                return ret;
            }

            return null;
        }
    }

    @Override
    public List<Penalty> getPenalties(String augentId) throws SQLException {
        try (Connection conn = getConnection()) {
            List<Penalty> ret = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_penalties"));
            pstmt.setString(1, augentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Penalty p = new Penalty();
                p.setAugentID(rs.getString(databaseProperties.getString("penalty_book_user_augentid")));
                p.setEventCode(rs.getInt(databaseProperties.getString("penalty_book_event_code")));
                p.setTimestamp(CustomDate.parseString(rs.getString(databaseProperties.getString("penalty_book_timestamp"))));
                p.setReservationDate(CustomDate.parseString(rs.getString(databaseProperties.getString("penalty_book_reservation_date"))));
                p.setReservationLocation(rs.getString(databaseProperties.getString("penalty_book_reservation_location")));
                p.setReceivedPoints(rs.getInt(databaseProperties.getString("penalty_book_received_points")));
                ret.add(p);
            }

            return  ret;
        }
    }

    @Override
    public void addPenaltyEvent(PenaltyEvent event) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                // 1. add penalty_event's record
                // 2. add the descriptions
                conn.setAutoCommit(false);

                // add penalty_event's record
                PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_penalty_event"));
                pstmt.setInt(1, event.getCode());
                pstmt.setInt(2, event.getPoints());
                pstmt.setBoolean(3, event.getPublicAccessible());
                pstmt.executeUpdate();

                // add the descriptions
                pstmt = conn.prepareStatement(databaseProperties.getString("insert_penalty_description"));
                for (Language lang : event.getDescriptions().keySet()) {
                    pstmt.setString(1, lang.name());
                    pstmt.setInt(2, event.getCode());
                    pstmt.setString(3, event.getDescriptions().get(lang));
                    pstmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public void addDescription(int code, Language language, String description) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_penalty_description"));
            pstmt.setString(1, language.name());
            pstmt.setInt(2, code);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void addPenalty(Penalty penalty) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_penalty"));
            setPreparedStatementWithPenalty(pstmt, penalty);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void updatePenaltyEvent(int code, PenaltyEvent event) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                // TODO

                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public void updatePenalties(String augentID, List<Penalty> remove, List<Penalty> add) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                // first delete all Penalties in 'remove'
                PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_penalty"));
                for (Penalty p : remove) {
                    setPreparedStatementWithPenalty(pstmt, p);
                    pstmt.executeUpdate();
                }

                // then, add all Penalties in 'add'
                pstmt = conn.prepareStatement(databaseProperties.getString("insert_penalty"));
                for (Penalty p : add) {
                    if (p.getReceivedPoints() < 0) {
                        PenaltyEvent e = getPenaltyEvent(p.getEventCode());
                        p.setReceivedPoints(e.getPoints());
                    }
                    setPreparedStatementWithPenalty(pstmt, p);

                    pstmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void updatePenaltyEventsDescription(PreparedStatement pstmt, int code, Language lang, String description)
            throws SQLException {
        pstmt.setString(1, description);
        pstmt.setString(2, lang.name());
        pstmt.setInt(3, code);
        pstmt.executeUpdate();
    }

    @Override
    public void deletePenaltyEvent(int code) throws SQLException {
        PenaltyEvent event = getPenaltyEvent(code);
        if (event != null) {
            try (Connection conn = getConnection()) {
                try {
                    conn.setAutoCommit(false);

                    PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_penalty_description"));
                    for (Language lang : event.getDescriptions().keySet()) {
                        pstmt.setString(1, lang.name());
                        pstmt.setInt(2, event.getCode());
                        pstmt.executeUpdate();
                    }

                    // TODO: delete penalty_book

                    pstmt = conn.prepareStatement(databaseProperties.getString("delete_penalty_event"));
                    pstmt.setInt(1, code);
                    pstmt.executeUpdate();

                    conn.commit();
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }
    }

    @Override
    public void deleteDescription(int code, Language language) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_penalty_description"));
            pstmt.setString(1, language.name());
            pstmt.setInt(2, code);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void deletePenalty(Penalty penalty) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_penalty"));
            pstmt.setString(1, penalty.getAugentID());
            pstmt.setInt(2, penalty.getEventCode());
            pstmt.setString(3, penalty.getTimestamp().toString());
            pstmt.executeUpdate();
        }
    }

    /**
     * Note the importance of the fact that the query that is loaded into 'pstmt', needs to have the same order
     * columns as is used here in the auxiliary method
     */
    private void setPreparedStatementWithPenalty(PreparedStatement pstmt, Penalty p) throws SQLException {
        pstmt.setString(1, p.getAugentID());
        pstmt.setInt(2, p.getEventCode());
        pstmt.setString(3, p.getTimestamp().toString());
        pstmt.setString(4, p.getReservationDate().toString());
        pstmt.setString(5, p.getReservationLocation());
        pstmt.setInt(6, p.getReceivedPoints());
    }
}
