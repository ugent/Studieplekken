package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.daos.ILocationDao;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.date.Time;
import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.exceptions.AlreadyExistsException;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservables.Locker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.*;

@Profile("!dummy")
@Service
public class DBLocationDao extends ADB implements ILocationDao {

    IAccountDao accountDao;

    public DBLocationDao(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public List<Location> getAllLocations() {
        try (Connection conn = getConnection()) {
            List<Location> locations = new ArrayList<>();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(resourceBundle.getString("all_locations"));
            while (rs.next()) {
                Location location = createLocation(rs);
                location.setCalendar(getCalendar(location.getName(), conn));
                location.setLockers(getLockers(location.getName(), conn));
                locations.add(location);
            }
            return locations;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public List<Location> getAllLocationsWithoutLockersAndCalendar() {
        try (Connection conn = getConnection()) {
            List<Location> locations = new ArrayList<Location>();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(resourceBundle.getString("all_locations"));
            while (rs.next()) {
                Location location = createLocation(rs);
                locations.add(location);
            }
            return locations;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public List<Location> getAllLocationsWithoutLockers() {
        try (Connection conn = getConnection()) {
            List<Location> locations = new ArrayList<Location>();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(resourceBundle.getString("all_locations"));
            while (rs.next()) {
                Location location = createLocation(rs);
                location.setCalendar(getCalendar(location.getName(), conn));
                locations.add(location);
            }
            return locations;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public List<Location> getAllLocationsWithoutCalendar() {
        try (Connection conn = getConnection()) {
            List<Location> locations = new ArrayList<Location>();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(resourceBundle.getString("all_locations"));
            while (rs.next()) {
                Location location = createLocation(rs);
                location.setLockers(getLockers(location.getName(), conn));
                locations.add(location);
            }
            return locations;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public Location addLocation(Location location) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                PreparedStatement st = conn.prepareStatement(resourceBundle.getString("insert_location"));
                st.setString(1, location.getName());
                st.setInt(2, location.getNumberOfSeats());
                st.setInt(3, location.getNumberOfLockers());
                st.setString(4, location.getMapsFrame());
                st.setString(5, location.getImageUrl());
                st.setString(6, location.getAddress());
                st.executeUpdate();

                for(Language lang: location.getDescriptions().keySet()){
                    st = conn.prepareStatement(resourceBundle.getString("insert_location_descriptions"));
                    st.setString(1,location.getName());
                    st.setString(2,lang.toString());
                    st.setString(3, location.getDescriptions().get(lang));
                    st.executeUpdate();
                }

                //if the location has a calendar, these days also need to be added to the database
                if (location.getCalendar() != null) {
                    for (Day day : location.getCalendar()) {
                        insertCalendarDay(location.getName(), day, conn);
                    }
                }

                //when adding a location, lockers need to be added to the database too
                for (int i = 0; i < location.getNumberOfLockers(); i++) {
                    int studentLimit = 2;
                    insertLocker(location.getName(), i, studentLimit, conn);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());

            if (e.getSQLState().equals(resourceBundle.getString("sql_state_duplicate_key")))
                throw new AlreadyExistsException("A location with name " + location.getName() + " already exists. " +
                        "Use updateLocation() instead.");
        }
        return null;
    }

    @Override
    public Location getLocation(String name) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_location"));
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Location location = createLocation(rs);
                location.setCalendar(getCalendar(name, conn));
                location.setLockers(getLockers(name, conn));
                return location;
            }
            return null;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public Location getLocationWithoutCalendar(String name) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_location"));
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Location location = createLocation(rs);
                location.setLockers(getLockers(name, conn));
                return location;
            }
            return null;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public Location getLocationWithoutLockers(String name) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_location"));
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Location location = createLocation(rs);
                location.setCalendar(getCalendar(name, conn));
                return location;
            }
            return null;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public Location getLocationWithoutLockersAndCalendar(String name) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_location"));
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Location loc = createLocation(rs);
                return loc;
            }
            return null;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void changeLocation(String name, Location location) {
        boolean nameChanged = !name.equalsIgnoreCase(location.getName());
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            // Delete location descriptions
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("delete_location_descriptions"));
            st.setString(1, name);
            st.executeUpdate();

            // Remove scanners
            List<String> scanners = getScannersFromLocation(location.getName());
            st = conn.prepareStatement(resourceBundle.getString("delete_scanners_of_location"));
            st.setString(1, location.getName());
            st.executeUpdate();

            if (!nameChanged) {
                st = conn.prepareStatement(resourceBundle.getString("update_location"));
                st.setString(1, location.getName());
                st.setInt(2, location.getNumberOfSeats());
                st.setInt(3, location.getNumberOfLockers());
                st.setString(4, location.getMapsFrame());
                st.setString(5, location.getImageUrl());
                st.setString(6, location.getAddress());
                if (location.getStartPeriodLockers() != null) {
                    st.setString(7, location.getStartPeriodLockers().toString());
                } else {
                    st.setString(7, null);
                }
                if (location.getEndPeriodLockers() != null) {
                    st.setString(8, location.getEndPeriodLockers().toString());
                } else {
                    st.setString(8, null);
                }
                st.setString(9, name);
                st.executeUpdate();
            }
            else{
                // nieuwe locatie invoegen
                st = conn.prepareStatement(resourceBundle.getString("insert_location"));
                st.setString(1, location.getName());
                st.setInt(2, location.getNumberOfSeats());
                st.setInt(3, location.getNumberOfLockers());
                st.setString(4, location.getMapsFrame());
                st.setString(5, location.getImageUrl());
                st.setString(6, location.getAddress());
                st.executeUpdate();

                // kalender wijzigen
                st = conn.prepareStatement(resourceBundle.getString("update_location_calendar"));
                st.setString(1,location.getName());
                st.setString(2,name);
                st.executeUpdate();

                // lockers wijzigen
                st = conn.prepareStatement(resourceBundle.getString("change_locker_location"));
                st.setString(1,location.getName());
                st.setString(2, name);
                st.executeUpdate();

                // oude locatie verwijderen
                st = conn.prepareStatement(resourceBundle.getString("delete_location"));
                st.setString(1,name);
                st.executeUpdate();
            }
            //lockers eventueel toevoegen of verwijderen

            // Add scanners
            for (String s : scanners) {
                st = conn.prepareStatement(resourceBundle.getString("insert_scanner_and_location"));
                st.setString(1, s.split(" ")[0]);
                st.setString(2, location.getName());
                st.executeUpdate();
            }

            // Add location descriptions
            for(Language lang: location.getDescriptions().keySet()){
                st = conn.prepareStatement(resourceBundle.getString("insert_location_descriptions"));
                st.setString(1,location.getName());
                st.setString(2,lang.toString());
                st.setString(3, location.getDescriptions().get(lang));
                st.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            if (e.getSQLState().equals(resourceBundle.getString("sql_state_duplicate_key")))
                throw new AlreadyExistsException("A location with name " + location.getName() + " already exists. " +
                        "Use updateLocation() instead.");
        }
    }

    @Override
    public void addLockers(String locationName, int count, int startNumber) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int studentLimit = 2;
                for (int i = startNumber; i < count + startNumber; i++) {
                    PreparedStatement st = conn.prepareStatement(resourceBundle.getString("insert_locker"));
                    st.setInt(1, i);
                    st.setString(2, locationName);
                    st.setInt(3, studentLimit);
                    st.execute();
                }
                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                conn.rollback();
                conn.setAutoCommit(true);
                System.out.println(ex.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        }
    }

    @Override
    public void deleteLockers(String locationName, int startNumber) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("delete_lockers_of_location_from_number"));
            st.setString(1, locationName);
            st.setInt(2, startNumber);
            st.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteLocation(String name) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                //Delete scanners
                PreparedStatement st = conn.prepareStatement(resourceBundle.getString("delete_scanners_of_location"));
                st.setString(1, name);
                st.execute();

                //Delete descriptions
                st = conn.prepareStatement(resourceBundle.getString("delete_location_descriptions"));
                st.setString(1, name);
                st.execute();

                //Delete location reservations for this location
                st = conn.prepareStatement(resourceBundle.getString("delete_location_reservations_of_location"));
                st.setString(1, name);
                st.execute();

                //Delete rows in calendar table that have a foreign key for this location
                st = conn.prepareStatement(resourceBundle.getString("delete_calendar_of_location"));
                st.setString(1, name);
                st.execute();

                //Delete locker reservations of lockers of this location
                List<Integer> locker_ids = new ArrayList<Integer>();
                st = conn.prepareStatement(resourceBundle.getString("get_locker_ids_of_location"));
                st.setString(1, name);
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt(resourceBundle.getString("locker_id"));
                    locker_ids.add(id);
                }
                for (int id : locker_ids) {
                    st = conn.prepareStatement(resourceBundle.getString("delete_locker_reservation_of_locker"));
                    st.setInt(1, id);
                    st.execute();
                }

                //Delete lockers of location
                st = conn.prepareStatement(resourceBundle.getString("delete_lockers_of_location"));
                st.setString(1, name);
                st.execute();

                //Delete the location
                st = conn.prepareStatement(resourceBundle.getString("delete_location"));
                st.setString(1, name);
                st.execute();
                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(true);
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //helper method for fetching locations,
    //returns the calendar of a certain location
    private Collection<Day> getCalendar(String locationName, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_calendar_of_location"));
        st.setString(1, locationName);
        ResultSet rs = st.executeQuery();
        Collection<Day> calendar = new ArrayList<Day>();
        while (rs.next()) {
            try {
                CustomDate date = CustomDate.parseString(rs.getString(resourceBundle.getString("cal_date")));
                java.sql.Time sqlOpeningHour = rs.getTime(resourceBundle.getString("cal_opening_hour"));
                Time openingHour = new Time(sqlOpeningHour.getHours(), sqlOpeningHour.getMinutes(), sqlOpeningHour.getSeconds());
                java.sql.Time sqlClosingHour = rs.getTime(resourceBundle.getString("cal_closing_hour"));
                Time closingHour = new Time(sqlClosingHour.getHours(), sqlClosingHour.getMinutes(), sqlClosingHour.getSeconds());
                CustomDate openReservationDate = CustomDate.parseString(rs.getString(resourceBundle.getString("cal_open_reservation_date")));
                Day day = new Day(date, openingHour, closingHour, openReservationDate);
                calendar.add(day);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return calendar;
    }

    @Override
    public void addCalendarDays(String locationName, Calendar calendar) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                for (Day day : calendar.getDays()) {
                    insertCalendarDay(locationName, day, conn);
                }
                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(true);
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void setScannersForLocation(String name, List<User> sc) {
        try (Connection connection = getConnection()) {

            //first delete
            String query = resourceBundle.getString("delete_scanners_of_location");
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.executeUpdate();

            for (User u : sc) {
                String query2 = resourceBundle.getString("insert_scanner_and_location");
                PreparedStatement statement2 = connection.prepareStatement(query2);
                statement2.setString(1, u.getAugentID());
                statement2.setString(2, name);
                statement2.execute();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<String> getScannersFromLocation(String name) {
        ArrayList<String> scanners = new ArrayList<>();
        try (Connection connection = getConnection()) {

            String query = resourceBundle.getString("get_scanners_of_location");
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String augentId = resultSet.getString(resourceBundle.getString("augent_id"));
                User u = accountDao.getUserById(augentId);
                scanners.add(u.shortString());
            }
            return scanners;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, Integer> getCountOfReservations(CustomDate date){
        HashMap<String, Integer> count = new HashMap<>();
        try (Connection conn = getConnection()){
            PreparedStatement st = conn.prepareStatement(resourceBundle.getString("todays_reservations"));
            st.setString(1, date.toString());
            ResultSet rs = st.executeQuery();
            while (rs.next()){
                String name =rs.getString(1);
                int c = rs.getInt(2);
                count.put(name,c);
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return count;
    }

    //helper method to add a calendar day to the calendar table
    private void insertCalendarDay(String locationName, Day day, Connection conn) throws SQLException {

        //check if there is already a row in the database for this location and date
        PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_calendar_day_count"));
        st.setString(1, day.getDate().toString());
        st.setString(2,locationName);
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            int count = rs.getInt(1);

            //if count = 0, insert the calendar day
            if (count == 0) {
                st = conn.prepareStatement(resourceBundle.getString("insert_calendar_day"));
                st.setString(1, day.getDate().toString());
                st.setString(2, locationName);
                st.setTime(3, new java.sql.Time(day.getOpeningHour().getHours(), day.getOpeningHour().getMinutes(), day.getOpeningHour().getSeconds()));
                st.setTime(4, new java.sql.Time(day.getClosingHour().getHours(), day.getClosingHour().getMinutes(), day.getClosingHour().getSeconds()));
                st.setString(5, day.getOpenForReservationDate().toString());
                st.execute();
            }

            //if count != 0, update the row containing this combination of location and date
            else {
                changeCalendarDay(locationName, day, conn);
            }
        }
    }

    //helper method to update a row in the calendar table
    private void changeCalendarDay(String locationName, Day day, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(resourceBundle.getString("update_calendar_day_of_location"));
        st.setTime(1, new java.sql.Time(day.getOpeningHour().getHours(), day.getOpeningHour().getMinutes(), day.getOpeningHour().getSeconds()));
        st.setTime(2, new java.sql.Time(day.getClosingHour().getHours(), day.getClosingHour().getMinutes(), day.getClosingHour().getSeconds()));
        st.setString(3, day.getOpenForReservationDate().toString());
        st.setString(4, locationName);
        st.setString(5, day.getDate().toString());
        st.execute();
    }

    @Override
    public void deleteCalendarDays(String locationName, String startdate, String enddate) {
        try {
            CustomDate start = CustomDate.parseString(startdate);
            CustomDate end = CustomDate.parseString(enddate);
            int s = start.getYear() * 404 + start.getMonth() * 31 + start.getDay();
            int e = end.getYear() * 404 + end.getMonth() * 31 + end.getDay();
            try (Connection conn = getConnection()) {
                try {
                    conn.setAutoCommit(false);

                    //Delete location reservations of this location for these dates
                    PreparedStatement st = conn.prepareStatement(resourceBundle.getString("delete_location_reservations_of_location_between_dates"));
                    st.setString(1, locationName);
                    st.setInt(2, s);
                    st.setInt(3, e);
                    st.execute();

                    st = conn.prepareStatement(resourceBundle.getString("delete_calendar_days_between_dates"));
                    st.setString(1, locationName);
                    st.setInt(2, s);
                    st.setInt(3, e);
                    st.execute();
                    conn.commit();
                    conn.setAutoCommit(true);
                } catch (SQLException exc) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                    System.out.println(exc.getMessage());
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //helper method when fetching locations
    //returns the lockers of a location
    private Collection<Locker> getLockers(String locationName, Connection conn) throws SQLException {
        Collection<Locker> lockers = new ArrayList<Locker>();
        PreparedStatement st = conn.prepareStatement(resourceBundle.getString("get_lockers_of_location"));
        st.setString(1, locationName);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int lockerID = rs.getInt(resourceBundle.getString("locker_id"));
            int number = rs.getInt(resourceBundle.getString("locker_number"));
            int studentLimit = rs.getInt(resourceBundle.getString("locker_student_limit"));

            Locker locker = new Locker();
            locker.setId(lockerID);
            locker.setNumber(number);
            locker.setLocation(locationName);
            locker.setStudentLimit(studentLimit);

            lockers.add(locker);
        }
        return lockers;
    }

    //helper method for AddLocation
    //inserts lockers in the locker table
    private void insertLocker(String locationName, int number, int studentLimit, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(resourceBundle.getString("insert_locker"));
        st.setInt(1, number);
        st.setString(2, locationName);
        st.setInt(3, studentLimit);
        st.execute();
    }


    //this method prevents a lot of duplicate code by creating a location out of a row in the resultset
    private Location createLocation(ResultSet rs) throws SQLException {
        String name = rs.getString(resourceBundle.getString("loc_name"));
        int numberOfSeats = rs.getInt(resourceBundle.getString("loc_number_of_seats"));
        int numberOfLockers = rs.getInt(resourceBundle.getString("loc_number_of_lockers"));
        String mapsFrame = rs.getString(resourceBundle.getString("loc_maps_frame"));
        String imageUrl = rs.getString(resourceBundle.getString("loc_image_url"));
        String address = rs.getString(resourceBundle.getString("loc_address"));
        CustomDate startPeriodLockers = CustomDate.parseString(rs.getString(resourceBundle.getString("loc_start_period_lockers")));
        CustomDate endPeriodLockers = CustomDate.parseString(rs.getString(resourceBundle.getString("loc_end_period_lockers")));

        Location location = new Location(name, address, numberOfSeats, numberOfLockers, mapsFrame, new HashMap<>(), imageUrl);
        location.setStartPeriodLockers(startPeriodLockers);
        location.setEndPeriodLockers(endPeriodLockers);

        Language lang = Language.valueOf(rs.getString(resourceBundle.getString("loc_desc_lang")));
        String desc = rs.getString(resourceBundle.getString("loc_desc_description"));
        location.getDescriptions().put(lang, desc);

        int i = 1;
        while (i < Language.values().length && rs.next()) {
            lang = Language.valueOf(rs.getString(resourceBundle.getString("loc_desc_lang")));
            desc = rs.getString(resourceBundle.getString("loc_desc_description"));
            location.getDescriptions().put(lang, desc);
            i++;
        }

        return location;
    }
}

