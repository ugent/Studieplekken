package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.ILocationReservationDao;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.exceptions.NoSuchReservationException;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservations.LocationReservation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Profile("!dummy")
@Service
public class DBLocationReservationDao extends ADB implements ILocationReservationDao {

    @Override
    public List<LocationReservation> getAllLocationReservationsOfUser(String augentID) throws NoSuchUserException {
        try(Connection conn = getConnection()){

            String queryUser = databaseProperties.getString("get_user_by_<?>").replace("<?>", "u.augentID = ?");
            PreparedStatement statementUser = conn.prepareStatement(queryUser);
            statementUser.setString(1, augentID);
            ResultSet resultSetUser = statementUser.executeQuery();

            if (!resultSetUser.next()) {
                throw new NoSuchUserException("No user with id = " + augentID);
            }

            List<LocationReservation> reservations = new ArrayList<LocationReservation>();
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_location_reservations_of_user_by_id"));
            st.setString(1, augentID);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                try{
                    CustomDate customDate = CustomDate.parseString(rs.getString(databaseProperties.getString("location_reservation_date")));
                    Location location = new Location(rs.getString(databaseProperties.getString("location_reservation_location_name")));
                    location.setCalendar(getCalendar(location.getName(), conn));
                    User user = new User(rs.getString(databaseProperties.getString("location_reservation_user_augentid")));
                    LocationReservation reservation = new LocationReservation(location, user, customDate);
                    reservations.add(reservation);
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
            return reservations;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfUserByName(String userName) {
        try(Connection conn = getConnection()){
            List<LocationReservation> reservations = new ArrayList<LocationReservation>();
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_location_reservations_of_user_by_name"));
            st.setString(1, userName);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                try{
                    CustomDate customDate = CustomDate.parseString(rs.getString(databaseProperties.getString("date")));
                    Location location = new Location(rs.getString(databaseProperties.getString("scanners_location_name")));
                    location.setCalendar(getCalendar(location.getName(), conn));
                    User user = new User(rs.getString(databaseProperties.getString("userId")));
                    LocationReservation reservation = new LocationReservation(location, user, customDate);
                    reservations.add(reservation);
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
            return reservations;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfLocation(String name) {
        try(Connection conn = getConnection()){
            List<LocationReservation> reservations = new ArrayList<LocationReservation>();
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_location_reservations_of_location"));
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                try{
                    CustomDate customDate = CustomDate.parseString(rs.getString(databaseProperties.getString("location_reservation_date")));
                    Location location = new Location(name);
                    User user = getUserForLocationReservation(rs.getString(databaseProperties.getString("location_reservation_user_augentid")));
                    LocationReservation reservation = new LocationReservation(location, user, customDate);
                    reservations.add(reservation);
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
            return reservations;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public LocationReservation getLocationReservation(String augentID, CustomDate date) {
        try(Connection conn = getConnection()){

            User u = getUserForLocationReservation(augentID);
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_location_reservation"));
            st.setString(1, augentID);
            st.setString(2, date.toString());
            ResultSet rs = st.executeQuery();
            if(rs.next()){
                try{
                    CustomDate customDate = CustomDate.parseString(rs.getString(databaseProperties.getString("location_reservation_date")));
                    Location location = new Location(rs.getString(databaseProperties.getString("location_reservation_location_name")));
                    location.setCalendar(getCalendar(location.getName(), conn));
                    User user = getUserForLocationReservation(augentID);
                    LocationReservation reservation = new LocationReservation(location, user, customDate);
                    return reservation;
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            } else{
                throw new NoSuchReservationException("User with id = " + augentID + " has no reservation on " + date.toString());
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteLocationReservation(String augentID, CustomDate date) {
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("delete_location_reservation"));
            st.setString(1, augentID);
            st.setString(2, date.toString());
            st.execute();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void addLocationReservation(LocationReservation locationReservation) {
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("insert_location_reservation"));
            st.setString(1, locationReservation.getDate().toString());
            st.setString(2, locationReservation.getLocation().getName());
            st.setString(3, locationReservation.getUser().getAugentID());
            st.execute();
        }/*
        catch (SQLIntegrityConstraintViolationException e){
            throw new AlreadyExistsException("User with id "+ locationReservation.getUser().getAugentID() +" already has a reservation on " + locationReservation.getDate().toString());
        }*/
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public LocationReservation scanStudent(String location, String barcode) {
        try{
            Connection conn = getConnection();
            Calendar c = Calendar.getInstance();
            CustomDate today = new CustomDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DATE));

            List<String> user_ids = new ArrayList<>();

            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_location_reservations_of_date"));
            st.setString(1, location);
            st.setString(2, today.toString());
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                user_ids.add(rs.getString(databaseProperties.getString("location_reservation_user_augentid")));
            }

            for(String id : user_ids){
                if(id.equals(barcode) || id.substring(1).equals(barcode.substring(0, barcode.length()-1)) ||
                        id.equals(barcode.substring(0, barcode.length()-1)) || id.equals(barcode.substring(1))) {
                    st = conn.prepareStatement(databaseProperties.getString("set_location_reservation_attended"));
                    st.setString(1, today.toString());
                    st.setString(2, id);
                    st.execute();
                    conn.close();
                    return getLocationReservation(id, today);
                }
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void setAllStudentsOfLocationToAttended(String location, CustomDate date) {
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("set_all_location_reservations_attended"));
            st.setString(1, location);
            st.setString(2, date.toString());
            st.execute();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public int countReservedSeatsOfLocationOnDate(String location, CustomDate date) {
        try(Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("count_location_reservations_of_location_for_date"));
            st.setString(1, location);
            st.setString(2, date.toString());
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                return rs.getInt(1);
            }
            return 0;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return 0;
    }

    @Override
    public List<LocationReservation> getAbsentStudents(String locationName, CustomDate date) {
        try(Connection conn = getConnection()){
            List<LocationReservation> reservations = new ArrayList<LocationReservation>();
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_absent_students"));
            st.setString(1, locationName);
            st.setString(2, date.toString());
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                CustomDate customDate = date;
                Location location = new Location(locationName);
                User user = getUserForLocationReservation(rs.getString(databaseProperties.getString("location_reservation_user_augentid")));
                LocationReservation reservation = new LocationReservation(location, user, customDate);
                reservations.add(reservation);
            }
            return reservations;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public List<LocationReservation> getPresentStudents(String locationName, CustomDate date) {
        try(Connection conn = getConnection()){
            List<LocationReservation> reservations = new ArrayList<LocationReservation>();
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_present_students"));
            st.setString(1, locationName);
            st.setString(2, date.toString());
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                CustomDate customDate = date;
                Location location = new Location(locationName);
                User user = getUserForLocationReservation(rs.getString(databaseProperties.getString("location_reservation_user_augentid")));
                LocationReservation reservation = new LocationReservation(location, user, customDate);
                reservations.add(reservation);
            }
            return reservations;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void setReservationToUnAttended(String augentId, CustomDate date) {
        try(Connection conn = getConnection()){
                PreparedStatement st = conn.prepareStatement(databaseProperties.getString("set_location_reservation_unattended"));
                st.setString(1, date.toString());
                st.setString(2, augentId);
                st.execute();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private Collection<Day> getCalendar(String locationName, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_calendar_of_location"));
        st.setString(1, locationName);
        ResultSet rs = st.executeQuery();
        Collection<Day> calendar = new ArrayList<Day>();
        while (rs.next()) {
            try {
                CustomDate date = CustomDate.parseString(rs.getString(databaseProperties.getString("calendar_date")));
                java.sql.Time sqlOpeningHour = rs.getTime(databaseProperties.getString("calendar_opening_hour"));
                be.ugent.blok2.helpers.date.Time openingHour = new be.ugent.blok2.helpers.date.Time(sqlOpeningHour.getHours(), sqlOpeningHour.getMinutes(), sqlOpeningHour.getSeconds());
                java.sql.Time sqlClosingHour = rs.getTime(databaseProperties.getString("calendar_closing_hour"));
                be.ugent.blok2.helpers.date.Time closingHour = new be.ugent.blok2.helpers.date.Time(sqlClosingHour.getHours(), sqlClosingHour.getMinutes(), sqlClosingHour.getSeconds());
                CustomDate openReservationDate = CustomDate.parseString(rs.getString(databaseProperties.getString("calendar_open_reservation_date")));
                Day day = new Day(date, openingHour, closingHour, openReservationDate);
                calendar.add(day);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return calendar;
    }

    private User getUserForLocationReservation(String augentID){
        try(Connection conn = getConnection()) {
            PreparedStatement stmforUser = conn.prepareStatement(databaseProperties.getString("get_user_for_location_reservation"));
            stmforUser.setString(1, augentID);
            ResultSet rsUser = stmforUser.executeQuery();
            if (rsUser.next()) {
                try {
                    User u = new User(augentID);
                    u.setFirstName(rsUser.getString(databaseProperties.getString("utv_surname")));
                    u.setLastName(rsUser.getString(databaseProperties.getString("utv_name")));
                    u.setMail(rsUser.getString(databaseProperties.getString("utv_mail")));
                    u.setBarcode(rsUser.getString(databaseProperties.getString("utv_barcode")));
                    return u;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
