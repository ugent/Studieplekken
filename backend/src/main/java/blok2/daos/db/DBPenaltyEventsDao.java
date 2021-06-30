package blok2.daos.db;

import blok2.daos.IPenaltyEventsDao;
import blok2.helpers.Language;
import blok2.helpers.Resources;
import blok2.model.penalty.PenaltyEvent;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DBPenaltyEventsDao extends DAO implements IPenaltyEventsDao {
    
    public List<PenaltyEvent> getPenaltyEvents() throws SQLException {
        try (Connection conn = adb.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(Resources.databaseProperties.getString("get_penalty_events"));

            List<PenaltyEvent> penaltyEvents = new ArrayList<>();

            while (rs.next()) {
                penaltyEvents.add(createPenaltyEvent(rs));
            }

            return penaltyEvents;
        }
    }
    
    public PenaltyEvent getPenaltyEvent(int code) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_penalty_event"));
            pstmt.setInt(1, code);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createPenaltyEvent(rs);
            }

            return null;
        }
    }
    
    public void addPenaltyEvent(PenaltyEvent event) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);
                addPenaltyEvent(event, conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void addPenaltyEvent(PenaltyEvent event, Connection conn) throws SQLException {
        // add penalty_event's record
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_penalty_event"));
        pstmt.setInt(1, event.getCode());
        pstmt.setInt(2, event.getPoints());
        pstmt.executeUpdate();

        // add the descriptions
        addDescriptions(event.getCode(), event.getDescriptions(), conn);
    }
    
    public void updatePenaltyEvent(int code, PenaltyEvent event) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // It is too complex to determine whether two maps are equal, so always remove all descriptions
                // and add new ones. The amount of work to delete and re-add them is small enough compared to
                // determining whether two keySets() and values() are equal.
                // Important: working with code, and not event.getCode() because the code itself may have been changed
                deletePenaltyEventDescriptions(code, conn);
                addDescriptions(code, event.getDescriptions(), conn);

                PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("update_penalty_event"));
                // set ...
                pstmt.setInt(1, event.getCode());
                pstmt.setInt(2, event.getPoints());
                // where ...
                pstmt.setInt(3, code);
                pstmt.execute();

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

    private void deletePenaltyEventDescriptions(int code, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("delete_penalty_descriptions_by_event_code"));
        pstmt.setInt(1, code);
        pstmt.execute();
    }
    
    public void deletePenaltyEvent(int code) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn
                    .prepareStatement(Resources.databaseProperties.getString("delete_penalty_event"));
            pstmt.setInt(1, code);
            pstmt.execute();
        }
    }
    
    public void addDescription(int code, Language language, String description) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            addDescription(code, language, description, conn);
        }
    }

    private void addDescription(int code, Language language, String description, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_penalty_description"));
        pstmt.setString(1, language.name());
        pstmt.setInt(2, code);
        pstmt.setString(3, description);
        pstmt.executeUpdate();
    }

    private void addDescriptions(int code, Map<Language, String> descriptions, Connection conn) throws SQLException {
        for (Language lang : descriptions.keySet()) {
            addDescription(code, lang, descriptions.get(lang), conn);
        }
    }

    public void deleteDescription(int code, Language language) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_penalty_description"));
            pstmt.setString(1, language.name());
            pstmt.setInt(2, code);
            pstmt.executeUpdate();
        }
    }

    public static PenaltyEvent createPenaltyEvent(ResultSet rs) throws SQLException {
        PenaltyEvent penaltyEvent = new PenaltyEvent();

        penaltyEvent.setCode(rs.getInt(Resources.databaseProperties.getString("penalty_event_code")));
        penaltyEvent.setPoints(rs.getInt(Resources.databaseProperties.getString("penalty_event_points")));

        Map<Language, String> descriptions = new HashMap<>();

        Language lang = Language.valueOf(rs.getString(Resources.databaseProperties.getString("penalty_description_lang_enum")));
        String description = rs.getString(Resources.databaseProperties.getString("penalty_description_description"));
        descriptions.put(lang, description);

        int i = 1;
        while (i < Language.values().length && rs.next()) {
            lang = Language.valueOf(rs.getString(Resources.databaseProperties.getString("penalty_description_lang_enum")));
            description = rs.getString(Resources.databaseProperties.getString("penalty_description_description"));
            descriptions.put(lang, description);
            i++;
        }

        penaltyEvent.setDescriptions(descriptions);

        return penaltyEvent;
    }

}
