package blok2.database;

import blok2.model.reservations.LocationReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class DBLocationReservationDao {

    private final ConnectionProvider connectionProvider;

    @Autowired
    public DBLocationReservationDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public boolean addLocationReservationIfStillRoomAtomically(LocationReservation reservation) throws SQLException {
        try (Connection conn = connectionProvider.getConnection()) {
            try {
                // Try to take a lock (or block) on the database by performing a SELECT FOR UPDATE.
                // A lock must be taken within a transaction, therefore disabling auto commit.
                conn.setAutoCommit(false);

                String query = "update timeslots " +
                        "set reservation_count = reservation_count + 1 " +
                        "where sequence_number = ?;";

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, reservation.getTimeslot().getTimeslotSeqnr());
                int change = stmt.executeUpdate();

                // Double-checking the change for insurance (and also compiler check)
                if(change != 1) {
                    return false;
                }

                // If this operation succeeds, the reservation can proceed
                query = "insert into public.location_reservations (user_id, timeslot_sequence_number, state) " +
                        "values (?, ?, ?);";

                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, reservation.getUser().getUserId());
                pstmt.setInt(2, reservation.getTimeslot().getTimeslotSeqnr());
                pstmt.setString(3, reservation.getState());
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

}
