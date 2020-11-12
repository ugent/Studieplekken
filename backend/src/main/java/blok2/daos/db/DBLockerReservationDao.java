package blok2.daos.db;

import blok2.daos.ILockerReservationDao;
import blok2.helpers.Resources;
import blok2.model.reservables.Locker;
import blok2.model.reservations.LockerReservation;
import blok2.model.users.User;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
@EnableScheduling
public class DBLockerReservationDao extends DAO implements ILockerReservationDao {

    @Override
    public List<LockerReservation> getAllLockerReservationsOfUser(String augentID) throws SQLException {
        String query = Resources.databaseProperties.getString("get_locker_reservations_where_<?>");
        query = query.replace("<?>", "lr.user_augentid = ?");
        return getAllLockerReservationsFromQueryWithOneParameter(augentID, query);
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfUserByName(String name) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<LockerReservation> res = new ArrayList<>();

            String query = Resources.databaseProperties.getString("get_locker_reservations_where_<?>");
            query = query.replace("<?>", "UPPER(CONCAT(augentpreferredgivenname, ' ', augentpreferredsn)) LIKE '%' || UPPER(?) || '%'");

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LockerReservation lockerReservation = DBLockerReservationDao.createLockerReservation(rs, conn);
                res.add(lockerReservation);
            }

            // If there have not been found lockerreservations with owners that have a similar
            // complete name (first + last name). Then there will be checked if there are
            // lockerreservations with a owner that have a similar first of last name.

            if (res.size() == 0) {
                query = Resources.databaseProperties.getString("get_locker_reservations_where_<?>");
                query = query.replace("<?>", "UPPER(augentpreferredgivenname) LIKE UPPER(?) or UPPER(augentpreferredsn) LIKE '%' || UPPER(?) || '%'");

                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, name);
                pstmt.setString(2, name);
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    LockerReservation lockerReservation = createLockerReservation(rs);
                    res.add(lockerReservation);
                }
            }
            return res;
        }
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfLocation(String locationName, boolean includePastReservations) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_locker_reservations_where_<?>");

            String replacementString = "lr.location_name = ?";
            if (!includePastReservations) {
                replacementString += " and ((lr.key_pickup_date is null or lr.key_pickup_date = '') or (lr.key_return_date is null or lr.key_return_date = ''))";
            }
            query = query.replace("<?>", replacementString);

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, locationName);

            return executeQueryForLockerReservations(pstmt);
        }
    }

    private List<LockerReservation> executeQueryForLockerReservations(PreparedStatement pstmt) throws SQLException {
        ResultSet rs = pstmt.executeQuery();

        List<LockerReservation> reservations = new ArrayList<>();
        while (rs.next()) {
            LockerReservation locationReservation = createLockerReservation(rs);
            reservations.add(locationReservation);
        }

        return reservations;
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfLocationWithoutKeyBroughtBack(String name) throws SQLException {
        String query = Resources.databaseProperties.getString("get_locker_reservations_where_<?>");
        query = query.replace("<?>", "l.location_name = ? and lr.key_return_date = ''");
        return getAllLockerReservationsFromQueryWithOneParameter(name, query);
    }

    private List<LockerReservation> getAllLockerReservationsFromQueryWithOneParameter(String parameter, String query)
            throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<LockerReservation> reservations = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, parameter);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                LockerReservation lockerReservation = DBLockerReservationDao.createLockerReservation(rs, conn);
                reservations.add(lockerReservation);
            }

            return reservations;
        }
    }

    @Override
    public int getNumberOfLockersInUseOfLocation(String locationName) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_lockers_in_use_of_location"));
            pstmt.setString(1, locationName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
                return rs.getInt(1);

            return 0;
        }
    }

    @Override
    public LockerReservation getLockerReservation(String locationName, int lockerNumber) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_locker_reservations_where_<?>");
            query = query.replace("<?>", "l.location_name = ? and l.number = ?");

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, locationName);
            pstmt.setInt(2, lockerNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
                return createLockerReservation(rs);

            return null;
        }
    }

    @Override
    public void deleteLockerReservation(String locationName, int lockerNumber) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_locker_reservation"));
            pstmt.setString(1, locationName);
            pstmt.setInt(2, lockerNumber);
            pstmt.execute();
        }
    }

    @Override
    public void addLockerReservation(LockerReservation lockerReservation) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_locker_reservation"));
            setupInsertLockerReservationPstmt(lockerReservation, pstmt);
            pstmt.execute();
        }
    }

    @Override
    public void changeLockerReservation(LockerReservation lockerReservation) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("update_locker_reservation"));
            // set ...
            setupUpdateLockerReservationPstmt(lockerReservation, pstmt);
            // where ...
            pstmt.setString(3, lockerReservation.getLocker().getLocation().getName());
            pstmt.setInt(4, lockerReservation.getLocker().getNumber());

            pstmt.execute();
        }
    }
    public LockerReservation createLockerReservation(ResultSet rs) throws SQLException {
        Connection conn = adb.getConnection();
        return createLockerReservation(rs, conn);
    }

    public static LockerReservation createLockerReservation(ResultSet rs, Connection conn) throws SQLException {
        LockerReservation lr = new LockerReservation();

        Timestamp date = rs.getTimestamp(Resources.databaseProperties.getString("locker_reservation_key_pickup_date"));
        if(!rs.wasNull())
            lr.setKeyPickupDate(date.toLocalDateTime());
        date = rs.getTimestamp(Resources.databaseProperties.getString("locker_reservation_key_return_date"));
        if(!rs.wasNull())
            lr.setKeyReturnedDate(date.toLocalDateTime());

        User u = DBAccountDao.createUser(rs);
        Locker l = DBLocationDao.createLocker(rs,conn);

        lr.setLocker(l);
        lr.setOwner(u);

        return lr;
    }

    private void setupInsertLockerReservationPstmt(LockerReservation lr
            , PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, lr.getLocker().getLocation().getName());
        pstmt.setInt(2, lr.getLocker().getNumber());
        pstmt.setString(3, lr.getOwner().getAugentID());
        pstmt.setTimestamp(4, lr.getKeyPickupDate() == null ? null : Timestamp.valueOf(lr.getKeyPickupDate()));
        pstmt.setTimestamp(5, lr.getKeyReturnedDate() == null ? null : Timestamp.valueOf(lr.getKeyReturnedDate()));
    }

    private void setupUpdateLockerReservationPstmt(LockerReservation lr, PreparedStatement pstmt) throws SQLException {
        pstmt.setTimestamp(1, lr.getKeyPickupDate() == null ? null : Timestamp.valueOf(lr.getKeyPickupDate()));
        pstmt.setTimestamp(2, lr.getKeyReturnedDate() == null ? null : Timestamp.valueOf(lr.getKeyReturnedDate()));
    }
}
