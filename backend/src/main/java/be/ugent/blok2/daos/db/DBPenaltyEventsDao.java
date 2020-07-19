package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.IPenaltyEventsDao;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.AlreadyExistsException;
import be.ugent.blok2.helpers.exceptions.NoSuchPenaltyEventException;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
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
    public List<PenaltyEvent> getPenaltyEvents() {
        List<PenaltyEvent> ret = new ArrayList<>();
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(databaseProperties.getString("get_penalty_events"));
            /*
            select e.code, e.points, e.public_accessible, d.lang_enum, d.description
            from penalty_events e
                join penalty_descriptions d
                    on e.code = d.event_code
             */
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

            ret.addAll(events.values());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return ret;
    }

    @Override
    public PenaltyEvent getPenaltyEvent(int code) {
        PenaltyEvent ret = null;
        try (Connection conn = getConnection()) {
            /*
            select e.code, e.points, e.public_accessible, d.lang_enum, d.description
            from penalty_events e
                join penalty_descriptions d
                    on e.code = d.event_code
             where e.code = ? ;
             */
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_penalty_event"));
            pstmt.setInt(1, code);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (ret == null) {
                    ret = new PenaltyEvent();
                    ret.setCode(code);
                    ret.setPoints(rs.getInt(databaseProperties.getString("penalty_event_points")));
                    ret.setPublicAccessible(rs.getBoolean(databaseProperties.getString("penalty_event_public_accessible")));
                    ret.setDescriptions(new HashMap<>());
                }
                Language lang = Language.valueOf(rs.getString(databaseProperties.getString("penalty_description_lang_enum")));
                ret.getDescriptions().put(lang, rs.getString(databaseProperties.getString("penalty_description_description")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return ret;
    }

    @Override
    public List<Penalty> getPenalties(String augentId) {
        List<Penalty> ret = new ArrayList<>();
        try (Connection conn = getConnection()) {
            /*
            select b.user_augentid, b.event_code, b.timestamp, b.reservation_date, b.received_points, b.reservation_location
            from public.penalty_book b
            where b.user_augentid = ?;
             */
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
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return ret;
    }

    @Override
    public void addPenaltyEvent(PenaltyEvent event) {
        try (Connection conn = getConnection()) {
            // 1. add penalty_events record
            // 2. add the descriptions
            conn.setAutoCommit(false);

            /*
            INSERT INTO public.penalty_events(code, points, public_accessible)
            VALUES (?, ?, ?);
             */
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_penalty_event"));
            pstmt.setInt(1, event.getCode());
            pstmt.setInt(2, event.getPoints());
            pstmt.setBoolean(3, event.getPublicAccessible());
            pstmt.executeUpdate();

            /*
            INSERT INTO public.penalty_descriptions(lang_enum, event_code, description)
            VALUES (?, ?, ?);
             */
            PreparedStatement pstmt2 = conn.prepareStatement(databaseProperties.getString("insert_penalty_description"));
            for (Language lang : event.getDescriptions().keySet()) {
                pstmt2.setString(1, lang.name());
                pstmt2.setInt(2, event.getCode());
                pstmt2.setString(3, event.getDescriptions().get(lang));
                pstmt2.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void addDescription(int code, Language language, String description) {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_penalty_description"));
            pstmt.setString(1, language.name());
            pstmt.setInt(2, code);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void addPenalty(Penalty penalty) {
        try (Connection conn = getConnection()) {
            /*
            insert into penalty_book(user_augentid, event_code, timestamp, reservation_date, reservation_location, received_points)
            values (?, ?, ?, ?, ?, ?);
             */
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_penalty"));
            setPreparedStatementWithPenalty(pstmt, penalty);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updatePenaltyEvent(int code, PenaltyEvent event) {
        try (Connection conn = getConnection()) {
            // Note: it is better to always update, even if the record' corresponding to 'code' holds
            // the same data as 'event' (and actually nothing has to be updated) because when you check for equality,
            // you'll always need 2 queries to update in stead of only one.
            conn.setAutoCommit(false);

            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("update_penalty_description"));
            for (Language lang : event.getDescriptions().keySet())
                updatePenaltyEventsDescription(pstmt, event.getCode(), lang, event.getDescriptions().get(lang));

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updatePenalties(String augentID, List<Penalty> remove, List<Penalty> add) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // first delete all Penalties in 'remove'
            /*
            DELETE
            FROM public.penalty_book b
            WHERE b.user_augentid = ? AND b.event_code = ? AND b.timestamp = ?
                AND b.reservation_date = ? AND b.reservation_location = ? AND b.received_points = ?;
             */
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_penalty"));
            for (Penalty p : remove) {
                setPreparedStatementWithPenalty(pstmt, p);
                pstmt.executeUpdate();
            }

            // then, add all Penalties in 'add'
            /*
            insert into penalty_book(user_augentid, event_code, timestamp, reservation_date, reservation_location, received_points)
            values (?, ?, ?, ?, ?, ?);
             */
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
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private int updatePenaltyEventsEvent(PreparedStatement pstmt, PenaltyEvent event) throws SQLException {
        /*
        UPDATE public.penalty_events
        SET points=?, public_accessible=?
        WHERE code=?;
         */
        pstmt.setInt(1, event.getPoints());
        pstmt.setBoolean(2, event.getPublicAccessible());
        pstmt.setInt(3, event.getCode());
        return pstmt.executeUpdate();
    }

    private void updatePenaltyEventsDescription(PreparedStatement pstmt, int code, Language lang, String description) throws SQLException {
        /*
        UPDATE public.penalty_descriptions
        SET description=?
        WHERE lang_enum=? AND event_code=?;
         */
        pstmt.setString(1, description);
        pstmt.setString(2, lang.name());
        pstmt.setInt(3, code);
        pstmt.executeUpdate();
    }

    @Override
    public void deletePenaltyEvent(int code) {
        // 1. delete all descriptions for this event (FK constraint)
        // 2. delete all penalty book record using this event (FK constraint) // TODO
        // 2. delete this event
        PenaltyEvent event = getPenaltyEvent(code);
        if (event != null) {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                /*
                DELETE FROM public.penalty_descriptions
                WHERE lang_enum=? AND event_code=?;
                 */
                PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_penalty_description"));
                for (Language lang : event.getDescriptions().keySet()) {
                    pstmt.setString(1, lang.name());
                    pstmt.setInt(2, event.getCode());
                    pstmt.executeUpdate();
                }

                /*
                DELETE FROM public.penalty_events
                WHERE code=?;
                 */
                PreparedStatement pstmt2 = conn.prepareStatement(databaseProperties.getString("delete_penalty_event"));
                pstmt2.setInt(1, code);
                pstmt2.executeUpdate();

                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void deleteDescription(int code, Language language) {
        try (Connection conn = getConnection()) {
            /*
            DELETE FROM public.penalty_descriptions
            WHERE lang_enum=? AND event_code=?;
             */
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_penalty_description"));
            pstmt.setString(1, language.name());
            pstmt.setInt(2, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deletePenalty(Penalty penalty) {
        try (Connection conn = getConnection()) {
            /*
            DELETE
            FROM public.penalty_book b
            WHERE b.user_augentid = ? AND b.event_code = ? AND b.timestamp = ?;
             */
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_penalty"));
            pstmt.setString(1, penalty.getAugentID());
            pstmt.setInt(2, penalty.getEventCode());
            pstmt.setString(3, penalty.getTimestamp().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
