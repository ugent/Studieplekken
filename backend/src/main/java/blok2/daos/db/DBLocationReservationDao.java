package blok2.daos.db;

import blok2.helpers.Resources;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.sql.Date;

@Service
public class DBLocationReservationDao extends DAO {

    public boolean addLocationReservationIfStillRoomAtomically(LocationReservation reservation) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                // Try to take a lock (or block) on the database by performing a SELECT FOR UPDATE.
                // A lock must be taken within a transaction, therefore disabling auto commit.
                conn.setAutoCommit(false);

                String query = "update timeslots " +
                        "set reservation_count = reservation_count + 1 " +
                        "where calendar_id = ? and timeslot_date = ? and timeslot_sequence_number= ?;";

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, reservation.getTimeslot().getCalendarId());
                stmt.setDate(2, Date.valueOf(reservation.getTimeslot().getTimeslotDate()));
                stmt.setInt(3, reservation.getTimeslot().getTimeslotSeqnr());
                int change = stmt.executeUpdate();

                // Double checking the change for insurance (and also compiler check)
                if(change != 1) {
                    return false;
                }

                // If this operation succeeds, the reservation can proceed
                query = "insert into public.location_reservations (user_id, timeslot_date, timeslot_seqnr, calendar_id, attended) " +
                        "values (?, ?, ?, ?, null);";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, reservation.getUser().getUserId());
                pstmt.setDate(2, java.sql.Date.valueOf(reservation.getTimeslot().getTimeslotDate()));
                pstmt.setInt(3, reservation.getTimeslot().getTimeslotSeqnr());
                pstmt.setInt(4, reservation.getTimeslot().getCalendarId());
                pstmt.execute();

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



    public void setNotScannedStudentsToUnattended(Timeslot timeslot) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);
                PreparedStatement pstmt = conn.prepareStatement(
                        Resources.databaseProperties.getString("set_not_scanned_students_as_not_attended"));
                pstmt.setInt(1, timeslot.getCalendarId());
                pstmt.setInt(2, timeslot.getTimeslotSeqnr());
                pstmt.setDate(3, Date.valueOf(timeslot.getTimeslotDate()));
                int changed = pstmt.executeUpdate();
                pstmt = conn.prepareStatement(Resources.databaseProperties.getString("subtract_x_to_reservation_count"));
                pstmt.setInt(1, changed);
                pstmt.setDate(3, java.sql.Date.valueOf(timeslot.getTimeslotDate()));
                pstmt.setInt(4, timeslot.getTimeslotSeqnr());
                pstmt.setInt(2, timeslot.getCalendarId());
                pstmt.execute();
                conn.commit();
                conn.setAutoCommit(true);
            } catch(SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Seperated out for use in transaction
    public static long getAmountOfReservationsOfTimeslot(Timeslot timeslot, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_location_reservations_of_location_for_timeslot"));
        pstmt.setInt(1, timeslot.getCalendarId());
        pstmt.setDate(2, java.sql.Date.valueOf(timeslot.getTimeslotDate()));
        pstmt.setInt(3, timeslot.getTimeslotSeqnr());
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

        User user = DBUserDao.createUser(rs, conn);
        Timeslot timeslot = DBCalendarPeriodDao.createTimeslot(rs, conn);

        return new LocationReservation(user, timeslot, attended);
    }

}
