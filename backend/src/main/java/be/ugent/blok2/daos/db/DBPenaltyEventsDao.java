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
            ResultSet rs = stmt.executeQuery(resourceBundle.getString("get_penalty_events"));
            /*
            select e.code, e.points, e.public_accessible, d.lang_enum, d.description
            from penalty_events e
                join penalty_descriptions d
                    on e.code = d.event_code
             */
            Map<Integer, PenaltyEvent> events = new HashMap<>();
            while (rs.next()) {
                int code = rs.getInt(resourceBundle.getString("event_code"));
                PenaltyEvent e = events.get(code);
                if (e == null) {
                    e = new PenaltyEvent();
                    e.setCode(code);
                    e.setPoints(rs.getInt(resourceBundle.getString("event_points")));
                    e.setPublicAccessible(rs.getBoolean(resourceBundle.getString("event_public_accessible")));
                    e.setDescriptions(new HashMap<>());
                    events.put(code, e);
                }

                Language lang = Language.valueOf(rs.getString(resourceBundle.getString("desc_lang_enum")));
                e.getDescriptions().put(lang, rs.getString(resourceBundle.getString("desc_desc")));
            }

            ret.addAll(events.values());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return ret;
    }

    @Override
    public PenaltyEvent getPenaltyEvent(int code) throws NoSuchPenaltyEventException {
        PenaltyEvent ret = null;
        try (Connection conn = getConnection()) {
            /*
            select e.code, e.points, e.public_accessible, d.lang_enum, d.description
            from penalty_events e
                join penalty_descriptions d
                    on e.code = d.event_code
             where e.code = ? ;
             */
            PreparedStatement pstmt = conn.prepareStatement(resourceBundle.getString("get_penalty_event"));
            pstmt.setInt(1, code);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (ret == null) {
                    ret = new PenaltyEvent();
                    ret.setCode(code);
                    ret.setPoints(rs.getInt(resourceBundle.getString("event_points")));
                    ret.setPublicAccessible(rs.getBoolean(resourceBundle.getString("event_public_accessible")));
                    ret.setDescriptions(new HashMap<>());
                }
                Language lang = Language.valueOf(rs.getString(resourceBundle.getString("desc_lang_enum")));
                ret.getDescriptions().put(lang, rs.getString(resourceBundle.getString("desc_desc")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return ret;
    }

    @Override
    public List<Penalty> getPenalties(String augentId) throws NoSuchUserException {
        List<Penalty> ret = new ArrayList<>();
        try (Connection conn = getConnection()) {
            /*
            select b.user_augentid, b.event_code, b.timestamp, b.reservation_date, b.received_points, b.reservation_location
            from public.penalty_book b
            where b.user_augentid = ?;
             */
            PreparedStatement pstmt = conn.prepareStatement(resourceBundle.getString("get_penalties"));
            pstmt.setString(1, augentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Penalty p = new Penalty();
                p.setAugentID(rs.getString(resourceBundle.getString("book_user_augentid")));
                p.setEventCode(rs.getInt(resourceBundle.getString("book_event_code")));
                p.setTimestamp(CustomDate.parseString(rs.getString(resourceBundle.getString("book_timestamp"))));
                p.setReservationDate(CustomDate.parseString(rs.getString(resourceBundle.getString("book_reservation_date"))));
                p.setReservationLocation(rs.getString(resourceBundle.getString("book_reservation_location")));
                p.setReceivedPoints(rs.getInt(resourceBundle.getString("book_received_points")));
                ret.add(p);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return ret;
    }

    @Override
    public void addPenaltyEvent(PenaltyEvent event) throws AlreadyExistsException {
        try (Connection conn = getConnection()) {
            // 1. add penalty_events record
            // 2. add the descriptions
            conn.setAutoCommit(false);

            /*
            INSERT INTO public.penalty_events(code, points, public_accessible)
            VALUES (?, ?, ?);
             */
            PreparedStatement pstmt = conn.prepareStatement(resourceBundle.getString("insert_penalty_event"));
            pstmt.setInt(1, event.getCode());
            pstmt.setInt(2, event.getPoints());
            pstmt.setBoolean(3, event.getPublicAccessible());
            pstmt.executeUpdate();

            /*
            INSERT INTO public.penalty_descriptions(lang_enum, event_code, description)
            VALUES (?, ?, ?);
             */
            PreparedStatement pstmt2 = conn.prepareStatement(resourceBundle.getString("insert_penalty_description"));
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
            // code 23505 -> duplicate key value violates unique constraint "penalty_events_pkey"
            if (e.getSQLState().equals(resourceBundle.getString("sql_state_duplicate_key")))
                throw new AlreadyExistsException("A penalty event with code " + event.getCode() + " already exists. " +
                        "Use updatePenaltyEvent() instead.");
        }
    }

    @Override
    public void addDescription(int code, Language language, String description) throws NoSuchPenaltyEventException, AlreadyExistsException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(resourceBundle.getString("count_penalty_events_with_code"));
            /*
            select count(1)
            from penalty_events
            where code = ?;
             */
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count != 1)
                throw new NoSuchPenaltyEventException("Penalty event with code " + code + " doesn't exist.");

            /*
            INSERT INTO public.penalty_descriptions(lang_enum, event_code, description)
            VALUES (?, ?, ?);
             */
            pstmt = conn.prepareStatement(resourceBundle.getString("insert_penalty_description"));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            // code 23505 -> duplicate key value violates unique constraint "penalty_events_pkey"
            if (e.getSQLState().equals(resourceBundle.getString("sql_state_duplicate_key")))
                throw new AlreadyExistsException("A penalty event with code " + code + " already exists. " +
                        "Use updatePenaltyEvent() instead.");
        }
    }

    @Override
    public void addPenalty(Penalty penalty) {
        try (Connection conn = getConnection()) {
            /*
            insert into penalty_book(user_augentid, event_code, timestamp, reservation_date, reservation_location, received_points)
            values (?, ?, ?, ?, ?, ?);
             */
            PreparedStatement pstmt = conn.prepareStatement(resourceBundle.getString("insert_penalty"));
            setPreparedStatementWithPenalty(pstmt, penalty);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updatePenaltyEvent(int code, PenaltyEvent event) throws NoSuchPenaltyEventException {
        try (Connection conn = getConnection()) {
            // 1. update penalty_events' record with given code
            // 2. update all the descriptions
            // Note: it is better to always update, even if the record' corresponding to 'code' holds
            // the same data as 'event' (and actually nothing has to be updated) because when you check for equality,
            // you'll always need 2 queries to update in stead of only one.
            conn.setAutoCommit(false);

            PreparedStatement pstmt = conn.prepareStatement(resourceBundle.getString("update_penalty_event"));
            int count = updatePenaltyEventsEvent(pstmt, event);
            if (count == 0)
                throw new NoSuchPenaltyEventException("No PenaltyEvent with code " + event.getCode() + " exists. ");

            pstmt = conn.prepareStatement(resourceBundle.getString("update_penalty_description"));
            for (Language lang : event.getDescriptions().keySet())
                updatePenaltyEventsDescription(pstmt, event.getCode(), lang, event.getDescriptions().get(lang));

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updatePenalties(String augentID, List<Penalty> remove, List<Penalty> add) throws NoSuchUserException, NoSuchPenaltyEventException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // first delete all Penalties in 'remove'
            /*
            DELETE
            FROM public.penalty_book b
            WHERE b.user_augentid = ? AND b.event_code = ? AND b.timestamp = ?
                AND b.reservation_date = ? AND b.reservation_location = ? AND b.received_points = ?;
             */
            PreparedStatement pstmt = conn.prepareStatement(resourceBundle.getString("delete_penalty"));
            for (Penalty p : remove) {
                setPreparedStatementWithPenalty(pstmt, p);
                pstmt.executeUpdate();
            }

            // then, add all Penalties in 'add'
            /*
            insert into penalty_book(user_augentid, event_code, timestamp, reservation_date, reservation_location, received_points)
            values (?, ?, ?, ?, ?, ?);
             */
            pstmt = conn.prepareStatement(resourceBundle.getString("insert_penalty"));
            for (Penalty p : add) {
                if(p.getReceivedPoints() < 0){
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

    private int updatePenaltyEventsEvent(PreparedStatement pstmt, PenaltyEvent event) throws SQLException, NoSuchPenaltyEventException {
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
    public void deletePenaltyEvent(int code) throws NoSuchPenaltyEventException {
        // 1. delete all descriptions for this event (otherwise we won't be able to delete this event, due to FK constraints
        // 2. delete this event
        PenaltyEvent event = getPenaltyEvent(code);
        if (event != null) {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                /*
                DELETE FROM public.penalty_descriptions
                WHERE lang_enum=? AND event_code=?;
                 */
                PreparedStatement pstmt = conn.prepareStatement(resourceBundle.getString("delete_penalty_description"));
                for (Language lang : event.getDescriptions().keySet()) {
                    pstmt.setString(1, lang.name());
                    pstmt.setInt(2, event.getCode());
                    pstmt.executeUpdate();
                }

                /*
                DELETE FROM public.penalty_events
                WHERE code=?;
                 */
                PreparedStatement pstmt2 = conn.prepareStatement(resourceBundle.getString("delete_penalty_event"));
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
    public void deleteDescription(int code, Language language) throws NoSuchPenaltyEventException {
        try (Connection conn = getConnection()) {
            /*
            DELETE FROM public.penalty_descriptions
            WHERE lang_enum=? AND event_code=?;
             */
            PreparedStatement pstmt = conn.prepareStatement(resourceBundle.getString("delete_penalty_description"));
            pstmt.setString(1, language.name());
            pstmt.setInt(2, code);
            int count = pstmt.executeUpdate();
            if (count == 0)
                throw new NoSuchPenaltyEventException("No description to be deleted in " + language + " for PenaltyEvent with code " + code + ".");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deletePenalty(Penalty penalty) throws NoSuchUserException, NoSuchPenaltyEventException {
        try (Connection conn = getConnection()) {
            /*
            DELETE
            FROM public.penalty_book b
            WHERE b.user_augentid = ? AND b.event_code = ? AND b.timestamp = ?
                AND b.reservation_date = ? AND b.reservation_location = ? AND b.received_points = ?;
             */
            PreparedStatement pstmt = conn.prepareStatement(resourceBundle.getString("delete_penalty"));
            setPreparedStatementWithPenalty(pstmt, penalty);
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
