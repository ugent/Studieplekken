package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.ILockerReservationDao;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Locker;
import be.ugent.blok2.reservations.LockerReservation;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Profile("db")
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
    public List<LockerReservation> getAllLockerReservationsOfUser(String augentID) throws NoSuchUserException{
        List<LockerReservation> reservations = new ArrayList<>();
        try(Connection conn = getConnection()){

            String queryUser = databaseProperties.getString("get_user_by_<?>").replace("<?>", "u.augentID = ?");
            PreparedStatement statementUser = conn.prepareStatement(queryUser);
            statementUser.setString(1, augentID);
            ResultSet resultSetUser = statementUser.executeQuery();

            if (!resultSetUser.next()) {
                throw new NoSuchUserException("No user with id = " + augentID);
            }

            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_locker_reservations_of_user_by_id"));
            st.setString(1, augentID);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                LockerReservation lockerReservation = createLockerReservation(rs);
                reservations.add(lockerReservation);
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return reservations;
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfUserByName(String name) {
        List<LockerReservation> res = new ArrayList<>();
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_locker_reservation_of_soundex_user_by_complete_name"));
            st.setString(1, name);
            st.setString(2, name);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                LockerReservation lockerReservation = createLockerReservation(rs);
                res.add(lockerReservation);
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }

        /*
            If there have not been found lockerreservations with owners that have a similar complete name (first + last name). Then
            there will be checked if there are lockerreservations with a owner that have a similar first of last name.
         */
        if(res.size()==0){
            try (Connection conn = getConnection()) {
                PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_locker_reservation_of_soundex_user_by_name"));
                st.setString(1, name);
                st.setString(2, name);
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    LockerReservation lockerReservation = createLockerReservation(rs);
                    res.add(lockerReservation);
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return res;
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfLocation(String name) {
        List<LockerReservation> reservations = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_locker_reservations_of_location"));
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                LockerReservation lockerReservation = createLockerReservation(rs);
                reservations.add(lockerReservation);
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reservations;
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfLocationWithoutKeyBroughtBack(String name) {
        List<LockerReservation> reservations = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_locker_reservations_of_location_without_key_brought_back"));
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                LockerReservation lockerReservation = createLockerReservation(rs);
                reservations.add(lockerReservation);
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reservations;
    }

    @Override
    public int getNumberOfLockersInUseOfLocation(String locationName) {
        int count = 0;
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("count_lockers_in_use_of_location"));
            st.setString(1, locationName);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                count = rs.getInt(1);
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return count;
    }

    @Override
    public LockerReservation getLockerReservation(String augentID, int lockerID) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_locker_reservation"));
            st.setInt(1, lockerID);
            st.setString(2, augentID);
            ResultSet rs = st.executeQuery();
            if (rs.next())
                return createLockerReservation(rs);
            return null;
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteLockerReservation(String augentID, int lockerID) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("delete_locker_reservation"));
            st.setString(1, augentID);
            st.setInt(2, lockerID);
            st.execute();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void addLockerReservation(LockerReservation lockerReservation) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("insert_locker_reservation"));
            setupInsertLockerReservationPstmt(lockerReservation, st);
            st.execute();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void changeLockerReservation(LockerReservation lockerReservation) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("update_locker_reservation"));
            setupUpdateLockerReservationPstmt(lockerReservation, lockerReservation.getLocker().getId(),
                    lockerReservation.getOwner().getAugentID(), st);
            st.execute();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static LockerReservation createLockerReservation(ResultSet rs) throws SQLException {
        LockerReservation lr = new LockerReservation();
        User u = DBAccountDao.createUser(rs);
        Locker l = DBLocationDao.createLocker(rs);

        lr.setLocker(l);
        lr.setOwner(u);
        lr.setKeyPickupDate(CustomDate.parseString(rs.getString(databaseProperties.getString("locker_reservation_key_pickup_date"))));
        lr.setKeyReturnedDate(CustomDate.parseString(rs.getString(databaseProperties.getString("locker_reservation_key_return_date"))));

        return lr;
    }

    private void setupInsertLockerReservationPstmt(LockerReservation lr
            , PreparedStatement pstmt) throws SQLException {
        pstmt.setInt(1, lr.getLocker().getId());
        pstmt.setString(2, lr.getOwner().getAugentID());
        pstmt.setString(3, lr.getKeyPickupDate() == null ? "" : lr.getKeyPickupDate().toString());
        pstmt.setString(4, lr.getKeyReturnedDate() == null ? "" : lr.getKeyReturnedDate().toString());
    }

    private void setupUpdateLockerReservationPstmt(LockerReservation lr
            , int lockerId, String augentId, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, lr.getKeyPickupDate() == null ? "" : lr.getKeyPickupDate().toString());
        pstmt.setString(2, lr.getKeyReturnedDate() == null ? "" : lr.getKeyReturnedDate().toString());
        pstmt.setInt(3, lockerId);
        pstmt.setString(4, augentId);
    }
}
