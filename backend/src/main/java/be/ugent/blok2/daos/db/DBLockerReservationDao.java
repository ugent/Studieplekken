package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.ILockerReservationDao;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Locker;
import be.ugent.blok2.reservations.LockerReservation;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Profile("!dummy")
@Service
@EnableScheduling
public class DBLockerReservationDao extends ADB implements ILockerReservationDao {

    // executes daily
    @Scheduled(fixedRate = 1000*60*60*24)
    public void scheduledCleanup(){
        try (Connection connection = getConnection()){
            Statement statement = connection.createStatement();
            statement.executeQuery(resourceBundle.getString("daily_cleanup_reservation_of_locker"));
        } catch (SQLException e) {
        }
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfUser(String augentID) throws NoSuchUserException{
        List<LockerReservation> reservations = new ArrayList<>();
        try(Connection conn = getConnection()){

            String queryUser = resourceBundle.getString("get_user_by_<?>").replace("<?>", "u.augentID = ?");
            PreparedStatement statementUser = conn.prepareStatement(queryUser);
            statementUser.setString(1, augentID);
            ResultSet resultSetUser = statementUser.executeQuery();

            if (!resultSetUser.next()) {
                throw new NoSuchUserException("No user with id = " + augentID);
            }

            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_locker_reservations_of_user_by_id"));
            st.setString(1, augentID);
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                CustomDate startDate = CustomDate.parseString(rs.getString(resourceBundle.getString("lockerres_start")));
                CustomDate endDate = CustomDate.parseString(rs.getString(resourceBundle.getString("lockerres_end")));
                User user = getUser(rs.getString(resourceBundle.getString("lockerres_user")), conn);
                Locker locker = getLocker(rs.getInt(resourceBundle.getString("lockerres_locker_id")), conn);
                boolean keyPickedUp = rs.getBoolean(resourceBundle.getString("lockerres_key_picked_up"));
                boolean keyBroughtBack = rs.getBoolean(resourceBundle.getString("lockerres_key_brought_back"));

                LockerReservation lockerReservation = new LockerReservation();
                lockerReservation.setStartDate(startDate);
                lockerReservation.setEndDate(endDate);
                lockerReservation.setOwner(user);
                lockerReservation.setLocker(locker);
                lockerReservation.setKeyPickedUp(keyPickedUp);
                lockerReservation.setKeyBroughtBack(keyBroughtBack);

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
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_locker_reservation_of_soundex_user_by_complete_name"));
            st.setString(1, name);
            st.setString(2, name);
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                CustomDate startDate = CustomDate.parseString(rs.getString(resourceBundle.getString("lockerres_start")));
                CustomDate endDate = CustomDate.parseString(rs.getString(resourceBundle.getString("lockerres_end")));
                User user = getUser(rs.getString(resourceBundle.getString("lockerres_user")), conn);

                String location = rs.getString(resourceBundle.getString("locker_location"));
                int number = rs.getInt(resourceBundle.getString("locker_number"));
                int studentLimit = rs.getInt(resourceBundle.getString("locker_student_limit"));

                Locker locker = new Locker();
                locker.setId(rs.getInt(resourceBundle.getString("lockerres_locker_id")));
                locker.setNumber(number);
                locker.setLocation(location);
                locker.setStudentLimit(studentLimit);

                boolean keyPickedUp = rs.getBoolean(resourceBundle.getString("lockerres_key_picked_up"));
                boolean keyBroughtBack = rs.getBoolean(resourceBundle.getString("lockerres_key_brought_back"));

                LockerReservation lockerReservation = new LockerReservation();
                lockerReservation.setStartDate(startDate);
                lockerReservation.setEndDate(endDate);
                lockerReservation.setOwner(user);
                lockerReservation.setLocker(locker);
                lockerReservation.setKeyPickedUp(keyPickedUp);
                lockerReservation.setKeyBroughtBack(keyBroughtBack);

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
            try(Connection conn = getConnection()){
                PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_locker_reservation_of_soundex_user_by_name"));
                st.setString(1, name);
                st.setString(2, name);
                ResultSet rs = st.executeQuery();
                while(rs.next()){
                    CustomDate startDate = CustomDate.parseString(rs.getString(resourceBundle.getString("lockerres_start")));
                    CustomDate endDate = CustomDate.parseString(rs.getString(resourceBundle.getString("lockerres_end")));
                    User user = getUser(rs.getString(resourceBundle.getString("lockerres_user")), conn);

                    String location = rs.getString(resourceBundle.getString("locker_location"));
                    int number = rs.getInt(resourceBundle.getString("locker_number"));
                    int studentLimit = rs.getInt(resourceBundle.getString("locker_student_limit"));

                    Locker locker = new Locker();
                    locker.setId(rs.getInt(resourceBundle.getString("lockerres_locker_id")));
                    locker.setNumber(number);
                    locker.setLocation(location);
                    locker.setStudentLimit(studentLimit);

                    boolean keyPickedUp = rs.getBoolean(resourceBundle.getString("lockerres_key_picked_up"));
                    boolean keyBroughtBack = rs.getBoolean(resourceBundle.getString("lockerres_key_brought_back"));

                    LockerReservation lockerReservation = new LockerReservation();
                    lockerReservation.setStartDate(startDate);
                    lockerReservation.setEndDate(endDate);
                    lockerReservation.setOwner(user);
                    lockerReservation.setLocker(locker);
                    lockerReservation.setKeyPickedUp(keyPickedUp);
                    lockerReservation.setKeyBroughtBack(keyBroughtBack);

                    res.add(lockerReservation);
                }
            }
            catch (SQLException e){
                System.out.println(e.getMessage());
            }
        }
        return res;
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfLocation(String name) {
        List<LockerReservation> reservations = new ArrayList<>();
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_locker_reservations_of_location"));
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                CustomDate startDate = CustomDate.parseString(rs.getString(resourceBundle.getString("lockerres_start")));
                CustomDate endDate = CustomDate.parseString(rs.getString(resourceBundle.getString("lockerres_end")));
                User user = getUser(rs.getString(resourceBundle.getString("lockerres_user")), conn);
                Locker locker = getLocker(rs.getInt(resourceBundle.getString("lockerres_locker_id")), conn);
                boolean keyPickedUp = rs.getBoolean(resourceBundle.getString("lockerres_key_picked_up"));
                boolean keyBroughtBack = rs.getBoolean(resourceBundle.getString("lockerres_key_brought_back"));

                LockerReservation lockerReservation = new LockerReservation();
                lockerReservation.setStartDate(startDate);
                lockerReservation.setEndDate(endDate);
                lockerReservation.setOwner(user);
                lockerReservation.setLocker(locker);
                lockerReservation.setKeyPickedUp(keyPickedUp);
                lockerReservation.setKeyBroughtBack(keyBroughtBack);

                reservations.add(lockerReservation);
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return reservations;
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfLocationWithoutKeyBroughtBack(String name) {
        List<LockerReservation> reservations = new ArrayList<>();
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_locker_reservations_of_location_without_key_brought_back"));
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                CustomDate startDate = CustomDate.parseString(rs.getString(resourceBundle.getString("lockerres_start")));
                CustomDate endDate = CustomDate.parseString(rs.getString(resourceBundle.getString("lockerres_end")));
                User user = getUser(rs.getString(resourceBundle.getString("lockerres_user")), conn);
                Locker locker = getLocker(rs.getInt(resourceBundle.getString("lockerres_locker_id")), conn);
                boolean keyPickedUp = rs.getBoolean(resourceBundle.getString("lockerres_key_picked_up"));
                boolean keyBroughtBack = rs.getBoolean(resourceBundle.getString("lockerres_key_brought_back"));

                LockerReservation lockerReservation = new LockerReservation();
                lockerReservation.setStartDate(startDate);
                lockerReservation.setEndDate(endDate);
                lockerReservation.setOwner(user);
                lockerReservation.setLocker(locker);
                lockerReservation.setKeyPickedUp(keyPickedUp);
                lockerReservation.setKeyBroughtBack(keyBroughtBack);

                reservations.add(lockerReservation);
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return reservations;
    }

    @Override
    public int getNumberOfLockersInUseOfLocation(String locationName){
        int count = 0;
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("count_lockers_in_use_of_location"));
            st.setString(1, locationName);
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                count = rs.getInt(1);
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return count;
    }

    @Override
    public LockerReservation getLockerReservation(String augentID, int lockerID, CustomDate startDate, CustomDate endDate) {
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_locker_reservation"));
            st.setString(1, augentID);
            st.setInt(2, lockerID);
            st.setString(3, startDate.toString());
            st.setString(4, endDate.toString());
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                User user = new User();
                user.setAugentID(augentID);
                user.setFirstName(rs.getString(resourceBundle.getString("user_surname")));
                user.setLastName(rs.getString(resourceBundle.getString("user_name")));
                Locker locker = getLocker(lockerID, conn);

                boolean keyPickedUp = rs.getBoolean(resourceBundle.getString("lockerres_key_picked_up"));
                boolean keyBroughtBack = rs.getBoolean(resourceBundle.getString("lockerres_key_brought_back"));

                LockerReservation lockerReservation = new LockerReservation();
                lockerReservation.setStartDate(startDate);
                lockerReservation.setEndDate(endDate);
                lockerReservation.setOwner(user);
                lockerReservation.setLocker(locker);
                lockerReservation.setKeyPickedUp(keyPickedUp);
                lockerReservation.setKeyBroughtBack(keyBroughtBack);

                return lockerReservation;
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteLockerReservation(String augentID, int lockerID, CustomDate startDate, CustomDate endDate) {
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("delete_locker_reservation"));
            st.setString(1, augentID);
            st.setInt(2, lockerID);
            st.setString(3, startDate.toString());
            st.setString(4, endDate.toString());
            st.execute();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public LockerReservation addLockerReservation(LockerReservation lockerReservation) {
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("insert_locker_reservation"));
            st.setString(1, lockerReservation.getOwner().getAugentID());
            st.setInt(2, lockerReservation.getLocker().getId());
            st.setString(3, lockerReservation.getStartDate().toString());
            st.setString(4, lockerReservation.getEndDate().toString());
            st.setBoolean(5, lockerReservation.getKeyPickedUp());
            st.setBoolean(6, lockerReservation.getKeyBroughtBack());
            st.execute();
            return lockerReservation;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void changeLockerReservation(LockerReservation lockerReservation) {
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("update_locker_reservation"));
            st.setBoolean(1, lockerReservation.getKeyPickedUp());
            st.setBoolean(2, lockerReservation.getKeyBroughtBack());
            st.setString(3, lockerReservation.getOwner().getAugentID());
            st.setInt(4, lockerReservation.getLocker().getId());
            st.setString(5, lockerReservation.getStartDate().toString());
            st.setString(6, lockerReservation.getEndDate().toString());
            st.execute();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private User getUser(String augentID, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_user_by_<?>").replace("<?>", "u.augentid = ?"));
        st.setString(1, augentID);
        ResultSet rs = st.executeQuery();
        while(rs.next()){
            User user = new User();
            user.setAugentID(augentID);
            user.setFirstName(rs.getString(resourceBundle.getString("user_surname")));
            user.setLastName(rs.getString(resourceBundle.getString("user_name")));
            return user;
        }
        return null;
    }

    private Locker getLocker(int lockerID, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_locker"));
        st.setInt(1, lockerID);
        ResultSet rs = st.executeQuery();
        while(rs.next()){
            String location = rs.getString(resourceBundle.getString("locker_location"));
            int number = rs.getInt(resourceBundle.getString("locker_number"));
            int studentLimit = rs.getInt(resourceBundle.getString("locker_student_limit"));

            Locker locker = new Locker();
            locker.setId(lockerID);
            locker.setNumber(number);
            locker.setLocation(location);
            locker.setStudentLimit(studentLimit);
            return locker;
        }
        return null;
    }
}
