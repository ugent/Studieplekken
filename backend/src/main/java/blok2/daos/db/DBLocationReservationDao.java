package blok2.daos.db;

import blok2.daos.ILocationReservationDao;
import blok2.helpers.Pair;
import blok2.helpers.Resources;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DBLocationReservationDao extends DAO implements ILocationReservationDao {

    @Override
    public List<LocationReservation> getAllLocationReservationsOfUser(String augentID) throws SQLException {
        String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
        query = query.replace("<?>", "u.augentid = ?");
        return getAllLocationsFromQueryWithOneParameter(augentID, query);
    }

    private List<LocationReservation> getAllLocationsFromQueryWithOneParameter(String parameterOne, String query)
            throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<LocationReservation> reservations = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, parameterOne);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocationReservation locationReservation = createLocationReservation(rs, conn);
                reservations.add(locationReservation);
            }

            return reservations;
        }
    }


    @Override
    public LocationReservation getLocationReservation(String augentID, Timeslot timeslot) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.user_augentid = ? and lr.timeslot_sequence_number = ? and lr.calendar_id = ?");

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, augentID);
            pstmt.setInt(2, timeslot.getTimeslotSeqnr());
            pstmt.setInt(3, timeslot.getPeriod().getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createLocationReservation(rs, conn);
            } else {
                return null;
            }
        }
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfTimeslot(Timeslot timeslot) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.timeslot_sequence_number = ? and lr.calendar_id = ?");

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, timeslot.getTimeslotSeqnr());
            pstmt.setInt(2, timeslot.getPeriod().getId());
            ResultSet rs = pstmt.executeQuery();

            List<LocationReservation> rlist = new ArrayList<>();
            while (rs.next()) {
                rlist.add(createLocationReservation(rs, conn));
            }

            return Collections.unmodifiableList(rlist);
        }
    }

    @Override
    public boolean deleteLocationReservation(String augentID, Timeslot timeslot) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // delete the location reservation
                PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_location_reservation"));
                pstmt.setString(1, augentID);
                pstmt.setInt(2, timeslot.getTimeslotSeqnr());
                pstmt.setInt(3, timeslot.getPeriod().getId());
                int count = pstmt.executeUpdate();

                if(count != 1) {
                    return false;
                }

                // and subtract a count from the reservation count
                pstmt = conn.prepareStatement(Resources.databaseProperties.getString("subtract_one_to_reservation_count"));
                pstmt.setInt(1, timeslot.getPeriod().getId());
                pstmt.setInt(2, timeslot.getTimeslotSeqnr());
                pstmt.execute();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.commit();
            }
        }
    }

    @Override
    public void addLocationReservation(LocationReservation locationReservation) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            addLocationReservation(locationReservation, conn);
        }
    }

    private void addLocationReservation(LocationReservation locationReservation, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_location_reservation"));
        pstmt.setString(1, locationReservation.getUser().getAugentID());
        pstmt.setTimestamp(2, Timestamp.valueOf(locationReservation.getCreatedAt()));
        pstmt.setInt(3, locationReservation.getTimeslot().getTimeslotSeqnr());
        pstmt.setInt(4, locationReservation.getTimeslot().getPeriod().getId());
        pstmt.execute();
    }

    @Override
    public boolean addLocationReservationIfStillRoomAtomically(LocationReservation reservation) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                // Try to take a lock (or block) on the database by performing a SELECT FOR UPDATE.
                // A lock must be taken within a transaction, therefore disabling auto commit.
                conn.setAutoCommit(false);
                PreparedStatement stmt = conn.prepareStatement(Resources.databaseProperties.getString("add_one_to_reservation_count"));
                stmt.setInt(1, reservation.getTimeslot().getPeriod().getId());
                stmt.setInt(2, reservation.getTimeslot().getTimeslotSeqnr());
                int change = stmt.executeUpdate();

                // Double checking the change for insurance (and also compiler check)
                if(change != 1) {
                    return false;
                }

                // If this operation succeeds, the reservation can proceed

                addLocationReservation(reservation, conn);

                return true;

            } catch (SQLException e) {
                conn.rollback();
                // Error codes that start with "23" are constraint violations.
                // This means that the entry was probably not unique.
                if (e.getSQLState().startsWith("23")) {
                    return false;
                } else {
                    // This is a real db error. Rethrowing it.
                    throw e;
                }
            } finally {
                conn.commit();
            }
        }
    }

    @Override
    public int amountOfReservationsRightNow(int locationId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_reservations_now"));
            pstmt.setInt(1, locationId);
            ResultSet rs = pstmt.executeQuery();

            rs.next();
            if (rs.wasNull())
                return 0;
            if (rs.getInt(2) == 0)
                return -1;

            return rs.getInt(1);
        }
    }

    @Override
    public long countReservedSeatsOfTimeslot(Timeslot timeslot) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return getAmountOfReservationsOfTimeslot(timeslot, conn);
        }
    }

    @Override
    public boolean setReservationAttendance(String augentId, Timeslot timeslot, boolean attendance) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            LocationReservation lr = getLocationReservation(augentId, timeslot);
            if(lr == null) {
                return false;
            }

            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("set_location_reservation_attendance"));
            pstmt.setBoolean(1, attendance);
            pstmt.setInt(2, timeslot.getPeriod().getId());
            pstmt.setInt(3, timeslot.getTimeslotSeqnr());
            pstmt.setString(4, augentId);
            pstmt.execute();
        }
        return true;
    }

    // Seperated out for use in transaction
    public static long getAmountOfReservationsOfTimeslot(Timeslot timeslot, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_location_reservations_of_location_for_timeslot"));
        pstmt.setInt(1, timeslot.getPeriod().getId());
        pstmt.setInt(2, timeslot.getTimeslotSeqnr());
        ResultSet set = pstmt.executeQuery();

        if(set.next()) {
            return set.getLong(1);
        }

        return -1;
    }

    public static LocationReservation createLocationReservation(ResultSet rs, Connection conn) throws SQLException {
        Boolean attended = rs.getBoolean(Resources.databaseProperties.getString("location_reservation_attended"));
        if (rs.wasNull()) {
            attended = null;
        }

        User user = DBAccountDao.createUser(rs, conn);
        Timeslot timeslot = DBCalendarPeriodDao.createTimeslot(rs, conn);
        LocalDateTime createdAt = rs.getTimestamp(Resources.databaseProperties.getString("location_reservation_created_at")).toLocalDateTime();

        return new LocationReservation(user, createdAt, timeslot, attended);
    }

}
