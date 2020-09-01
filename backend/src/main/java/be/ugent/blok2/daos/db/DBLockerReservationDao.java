package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.ILockerReservationDao;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.reservables.Locker;
import be.ugent.blok2.model.reservations.LockerReservation;
import be.ugent.blok2.model.users.User;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@EnableScheduling
public class DBLockerReservationDao extends ADB implements ILockerReservationDao {

    // executes daily
    // TODO: should be done in db, have a look at https://stackoverflow.com/a/9490521/9356123
    //  to track the timestamp of the locker_reservation record insertions. Based on xmin,
    //  delete certain entries. Probably best to do it as a Perl script which can be put into
    //  a one shot systemd service on the production server.
    /*@Scheduled(fixedRate = 1000*60*60*24)
    public void scheduledCleanup(){
        try (Connection connection = getConnection()){
            Statement statement = connection.createStatement();
            statement.executeQuery(databaseProperties.getString("daily_cleanup_reservation_of_locker"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }*/

    @Override
    public List<LockerReservation> getAllLockerReservationsOfUser(String augentID) throws SQLException {
        String query = databaseProperties.getString("get_locker_reservations_where_<?>");
        query = query.replace("<?>", "lr.user_augentid = ?");
        return getAllLockerReservationsFromQueryWithOneParamter(augentID, query);
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfUserByName(String name) throws SQLException {
        try (Connection conn = getConnection()) {
            List<LockerReservation> res = new ArrayList<>();

            String query = databaseProperties.getString("get_locker_reservations_where_<?>");
            query = query.replace("<?>", "metaphone(CONCAT(augentpreferredgivenname, ' ', augentpreferredsn), 10) = metaphone(?, 10) or metaphone(CONCAT(augentpreferredgivenname, ' ', augentpreferredsn), 10) = metaphone(?, 10)");

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setString(2, name);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LockerReservation lockerReservation = createLockerReservation(rs);
                res.add(lockerReservation);
            }

            // If there have not been found lockerreservations with owners that have a similar
            // complete name (first + last name). Then there will be checked if there are
            // lockerreservations with a owner that have a similar first of last name.

            if (res.size() == 0) {
                query = databaseProperties.getString("get_locker_reservations_where_<?>");
                query = query.replace("<?>", "metaphone(augentpreferredgivenname, 10) = metaphone(? , 5) or metaphone(augentpreferredsn, 10) = metaphone(? , 5)");

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
        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_locker_reservations_where_<?>");

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
        String query = databaseProperties.getString("get_locker_reservations_where_<?>");
        query = query.replace("<?>", "l.location_name = ? and lr.key_return_date = ''");
        return getAllLockerReservationsFromQueryWithOneParamter(name, query);
    }

    private List<LockerReservation> getAllLockerReservationsFromQueryWithOneParamter(String parameter, String query)
            throws SQLException {
        try (Connection conn = getConnection()) {
            List<LockerReservation> reservations = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, parameter);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                LockerReservation lockerReservation = createLockerReservation(rs);
                reservations.add(lockerReservation);
            }

            return reservations;
        }
    }

    @Override
    public int getNumberOfLockersInUseOfLocation(String locationName) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("count_lockers_in_use_of_location"));
            pstmt.setString(1, locationName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
                return rs.getInt(1);

            return 0;
        }
    }

    @Override
    public LockerReservation getLockerReservation(String locationName, int lockerNumber) throws SQLException {
        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_locker_reservations_where_<?>");
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
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_locker_reservation"));
            pstmt.setString(1, locationName);
            pstmt.setInt(2, lockerNumber);
            pstmt.execute();
        }
    }

    @Override
    public void addLockerReservation(LockerReservation lockerReservation) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_locker_reservation"));
            setupInsertLockerReservationPstmt(lockerReservation, pstmt);
            pstmt.execute();
        }
    }

    @Override
    public void changeLockerReservation(LockerReservation lockerReservation) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("update_locker_reservation"));
            // set ...
            setupUpdateLockerReservationPstmt(lockerReservation, pstmt);
            // where ...
            pstmt.setString(3, lockerReservation.getLocker().getLocation().getName());
            pstmt.setInt(4, lockerReservation.getLocker().getNumber());

            pstmt.execute();
        }
    }

    public static LockerReservation createLockerReservation(ResultSet rs) throws SQLException {
        LockerReservation lr = new LockerReservation();
        lr.setKeyPickupDate(CustomDate.parseString(rs.getString(databaseProperties.getString("locker_reservation_key_pickup_date"))));
        lr.setKeyReturnedDate(CustomDate.parseString(rs.getString(databaseProperties.getString("locker_reservation_key_return_date"))));

        User u = DBAccountDao.createUser(rs);
        Locker l = DBLocationDao.createLocker(rs);

        lr.setLocker(l);
        lr.setOwner(u);

        return lr;
    }

    private void setupInsertLockerReservationPstmt(LockerReservation lr
            , PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, lr.getLocker().getLocation().getName());
        pstmt.setInt(2, lr.getLocker().getNumber());
        pstmt.setString(3, lr.getOwner().getAugentID());
        pstmt.setString(4, lr.getKeyPickupDate() == null ? "" : lr.getKeyPickupDate().toString());
        pstmt.setString(5, lr.getKeyReturnedDate() == null ? "" : lr.getKeyReturnedDate().toString());
    }

    private void setupUpdateLockerReservationPstmt(LockerReservation lr, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, lr.getKeyPickupDate() == null ? "" : lr.getKeyPickupDate().toString());
        pstmt.setString(2, lr.getKeyReturnedDate() == null ? "" : lr.getKeyReturnedDate().toString());
    }
}
