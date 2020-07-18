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
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.reservables.Locker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalTime;
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
            ResultSet rs = st.executeQuery(databaseProperties.getString("all_locations"));
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
    public Location addLocation(Location location) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                // insert location into the database
                PreparedStatement st = conn.prepareStatement(databaseProperties.getString("insert_location"));
                prepareUpdateOrInsertLocationStatement(location, st);
                st.executeUpdate();

                // insert descriptions corresponding to the location into the database
                for (Language lang: location.getDescriptions().keySet()) {
                    st = conn.prepareStatement(databaseProperties.getString("insert_location_descriptions"));
                    prepareUpdateOrInsertLocationDescriptionStatement(
                            location.getName(),
                            lang,
                            location.getDescriptions().get(lang),
                            st
                    );
                    st.executeUpdate();
                }

                // insert the lockers corresponding to the location into the databse
                for (int i = 0; i < location.getNumberOfLockers(); i++) {
                    insertLocker(location.getName(), i, conn);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());

            if (e.getSQLState().equals(databaseProperties.getString("sql_state_duplicate_key")))
                throw new AlreadyExistsException("A location with name " + location.getName() + " already exists. " +
                        "Use updateLocation() instead.");
        }
        return null;
    }

    @Override
    public Location getLocation(String name) {
        try (Connection conn = getConnection()) {
            return getLocation(name, conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static Location getLocation(String locationName, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_location"));
        st.setString(1, locationName);
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            return createLocation(rs);
        }
        return null;
    }

    @Override
    public void changeLocation(String name, Location location) {
        boolean nameChanged = !name.equals(location.getName());

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Delete location descriptions
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("delete_location_descriptions"));
            st.setString(1, name);
            st.executeUpdate();

            // Remove scanners
            List<String> scanners = getScannersFromLocation(location.getName());
            st = conn.prepareStatement(databaseProperties.getString("delete_scanners_of_location"));
            st.setString(1, location.getName());
            st.executeUpdate();

            if (nameChanged) {
                // nieuwe locatie invoegen
                st = conn.prepareStatement(databaseProperties.getString("insert_location"));
                prepareUpdateOrInsertLocationStatement(location, st);
                st.executeUpdate();

                // kalender wijzigen
                st = conn.prepareStatement(databaseProperties.getString("update_location_calendar"));
                st.setString(1, location.getName());
                st.setString(2, name);
                st.executeUpdate();

                // lockers wijzigen
                st = conn.prepareStatement(databaseProperties.getString("change_locker_location"));
                st.setString(1, location.getName());
                st.setString(2, name);
                st.executeUpdate();

                // oude locatie verwijderen
                st = conn.prepareStatement(databaseProperties.getString("delete_location"));
                st.setString(1,name);
            } else {
                st = conn.prepareStatement(databaseProperties.getString("update_location"));
                prepareUpdateOrInsertLocationStatement(location, st);
                st.setString(9, name);
            }
            st.executeUpdate();

            // lockers eventueel toevoegen of verwijderen

            // Add scanners
            for (String s : scanners) {
                st = conn.prepareStatement(databaseProperties.getString("insert_scanner_and_location"));
                st.setString(1, s.split(" ")[0]);
                st.setString(2, location.getName());
                st.executeUpdate();
            }

            // Add location descriptions
            for (Language lang: location.getDescriptions().keySet()) {
                st = conn.prepareStatement(databaseProperties.getString("insert_location_descriptions"));
                st.setString(1,location.getName());
                st.setString(2,lang.toString());
                st.setString(3, location.getDescriptions().get(lang));
                st.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            if (e.getSQLState().equals(databaseProperties.getString("sql_state_duplicate_key")))
                throw new AlreadyExistsException("A location with name " + location.getName() + " already exists. " +
                        "Use updateLocation() instead.");
        }
    }

    @Override
    public Collection<Locker> getLockers(String locationName) {
        try (Connection conn = getConnection()) {
            return getLockers(locationName, conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void addLockers(String locationName, int count) {
        if (count == 0) return;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                Location location = getLocation(locationName, conn);

                if (location == null)
                    throw new SQLException();

                int n = location.getNumberOfLockers();

                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        insertLocker(locationName, n + i, conn);
                    }
                } else { // count < 0
                    // delete lockers will fail if any locker is reserved
                    // note: count is negative -> n + count < n
                    deleteLockers(locationName, n + count - 1, conn);
                    // example: n = 15, count -5  ->  startNumber = 10
                    // lockers 10, 11, 12, 13 and 14 will be removed
                }

                // update location: number_of_lockers has been updated
                location.setNumberOfLockers(n + count);
                updateLocation(location, conn);

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                System.out.println(ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteLockers(String locationName, int startNumber) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            deleteLockers(locationName, startNumber, conn);

            // update location: number_of_lockers has been updated
            Location location = getLocation(locationName, conn);

            if (location == null)
                throw new SQLException("Location " + locationName + " not found");

            int r = location.getNumberOfLockers() - startNumber;
            int n = location.getNumberOfLockers() - r;
            location.setNumberOfLockers(n);
            updateLocation(location, conn);

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteLockers(String locationName, int startNumber, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(databaseProperties.getString("delete_lockers_of_location_from_number"));
        st.setString(1, locationName);
        st.setInt(2, startNumber);
        st.execute();
    }

    @Override
    public void deleteLocation(String name) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                // Delete scanners
                PreparedStatement st = conn.prepareStatement(databaseProperties.getString("delete_scanners_of_location"));
                st.setString(1, name);
                st.execute();

                // Delete descriptions
                st = conn.prepareStatement(databaseProperties.getString("delete_location_descriptions"));
                st.setString(1, name);
                st.execute();

                // Delete location reservations for this location
                st = conn.prepareStatement(databaseProperties.getString("delete_location_reservations_of_location"));
                st.setString(1, name);
                st.execute();

                // Delete rows in calendar table that have a foreign key for this location
                st = conn.prepareStatement(databaseProperties.getString("delete_calendar_of_location"));
                st.setString(1, name);
                st.execute();

                // Delete locker reservations of lockers of this location
                List<Integer> locker_ids = new ArrayList<>();
                st = conn.prepareStatement(databaseProperties.getString("get_locker_ids_of_location"));
                st.setString(1, name);
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt(databaseProperties.getString("locker_id"));
                    locker_ids.add(id);
                }
                for (int id : locker_ids) {
                    st = conn.prepareStatement(databaseProperties.getString("delete_locker_reservation_of_locker"));
                    st.setInt(1, id);
                    st.execute();
                }

                // Delete lockers of location
                st = conn.prepareStatement(databaseProperties.getString("delete_lockers_of_location"));
                st.setString(1, name);
                st.execute();

                // Delete penalty events that occurred in this location
                st = conn.prepareStatement(databaseProperties.getString("delete_penalties_of_location"));
                st.setString(1, name);
                st.execute();

                // Delete the location
                st = conn.prepareStatement(databaseProperties.getString("delete_location"));
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

    @Override
    public Collection<Day> getCalendarDays(String locationName) {
        try (Connection conn = getConnection()) {
            return getCalendar(locationName, conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
        }
    }

    // helper method for fetching locations,
    // returns the calendar of a certain location
    // TODO: make public (instead of getLocation with calendar days and locations)
    private static Collection<Day> getCalendar(String locationName, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_calendar_of_location"));
        st.setString(1, locationName);
        ResultSet rs = st.executeQuery();
        Collection<Day> calendar = new ArrayList<>();
        while (rs.next()) {
            try {
                CustomDate date = CustomDate.parseString(rs.getString(databaseProperties.getString("calendar_date")));

                java.sql.Time sqlOpeningHour = rs.getTime(databaseProperties.getString("calendar_opening_hour"));
                LocalTime utilOpeningHour = sqlOpeningHour.toLocalTime();
                Time openingHour = new Time(utilOpeningHour.getHour(), utilOpeningHour.getMinute(), utilOpeningHour.getSecond());

                java.sql.Time sqlClosingHour = rs.getTime(databaseProperties.getString("calendar_closing_hour"));
                LocalTime utilClosingHour = sqlClosingHour.toLocalTime();
                Time closingHour = new Time(utilClosingHour.getHour(), utilClosingHour.getMinute(), utilClosingHour.getSecond());

                CustomDate openReservationDate = CustomDate.parseString(rs.getString(databaseProperties.getString("calendar_open_reservation_date")));
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
            String query = databaseProperties.getString("delete_scanners_of_location");
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.executeUpdate();

            for (User u : sc) {
                String query2 = databaseProperties.getString("insert_scanner_and_location");
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

            String query = databaseProperties.getString("get_scanners_of_location");
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String augentId = resultSet.getString(databaseProperties.getString("scanners_location_user_augentid"));
                scanners.add(augentId);
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
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("count_location_reservations_on_date"));
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

    // helper method to add a calendar day to the calendar table
    private void insertCalendarDay(String locationName, Day day, Connection conn) throws SQLException {

        //check if there is already a row in the database for this location and date
        PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_calendar_day_count"));
        st.setString(1, day.getDate().toString());
        st.setString(2,locationName);
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            int count = rs.getInt(1);

            //if count = 0, insert the calendar day
            if (count == 0) {
                st = conn.prepareStatement(databaseProperties.getString("insert_calendar_day"));
                st.setString(1, day.getDate().toString());
                st.setString(2, locationName);

                LocalTime openingTime = LocalTime.of(
                        day.getOpeningHour().getHours(),
                        day.getOpeningHour().getMinutes(),
                        day.getOpeningHour().getSeconds()
                );

                st.setTime(3, java.sql.Time.valueOf(openingTime));

                LocalTime closingTime = LocalTime.of(
                        day.getClosingHour().getHours(),
                        day.getClosingHour().getMinutes(),
                        day.getClosingHour().getSeconds()
                );

                st.setTime(4, java.sql.Time.valueOf(closingTime));
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
        PreparedStatement st = conn.prepareStatement(databaseProperties.getString("update_calendar_day_of_location"));

        LocalTime openingTime = LocalTime.of(
                day.getOpeningHour().getHours(),
                day.getOpeningHour().getMinutes(),
                day.getOpeningHour().getSeconds()
        );

        st.setTime(1, java.sql.Time.valueOf(openingTime));

        LocalTime closingTime = LocalTime.of(
                day.getClosingHour().getHours(),
                day.getClosingHour().getMinutes(),
                day.getClosingHour().getSeconds()
        );

        st.setTime(2, java.sql.Time.valueOf(closingTime));
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
                    PreparedStatement st = conn.prepareStatement(databaseProperties.getString("delete_location_reservations_of_location_between_dates"));
                    st.setString(1, locationName);
                    st.setInt(2, s);
                    st.setInt(3, e);
                    st.execute();

                    st = conn.prepareStatement(databaseProperties.getString("delete_calendar_days_between_dates"));
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

    // helper method when fetching locations
    // returns the lockers of a location
    // TODO: make public (instead of getLocation with calendar days and locations)
    private static Collection<Locker> getLockers(String locationName, Connection conn) throws SQLException {
        Collection<Locker> lockers = new ArrayList<>();
        PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_lockers_of_location"));
        st.setString(1, locationName);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int lockerID = rs.getInt(databaseProperties.getString("locker_id"));
            int number = rs.getInt(databaseProperties.getString("locker_number"));

            Locker locker = new Locker();
            locker.setId(lockerID);
            locker.setNumber(number);
            locker.setLocation(locationName);

            lockers.add(locker);
        }
        return lockers;
    }

    // this method prevents a lot of duplicate code by creating a location out of a row in the ResultSet
    public static Location createLocation(ResultSet rs) throws SQLException {
        String name = rs.getString(databaseProperties.getString("location_name"));
        int numberOfSeats = rs.getInt(databaseProperties.getString("location_number_of_seats"));
        int numberOfLockers = rs.getInt(databaseProperties.getString("location_number_of_lockers"));
        String mapsFrame = rs.getString(databaseProperties.getString("location_maps_frame"));
        String imageUrl = rs.getString(databaseProperties.getString("location_image_url"));
        String address = rs.getString(databaseProperties.getString("location_address"));
        CustomDate startPeriodLockers = CustomDate.parseString(rs.getString(databaseProperties.getString("location_start_period_lockers")));
        CustomDate endPeriodLockers = CustomDate.parseString(rs.getString(databaseProperties.getString("location_end_period_lockers")));

        Location location = new Location(name, address, numberOfSeats, numberOfLockers, mapsFrame
                , new HashMap<>(), imageUrl);
        location.setStartPeriodLockers(startPeriodLockers);
        location.setEndPeriodLockers(endPeriodLockers);

        Language lang = Language.valueOf(rs.getString(databaseProperties.getString("location_description_lang_enum")));
        String desc = rs.getString(databaseProperties.getString("location_description_description"));
        location.getDescriptions().put(lang, desc);

        // iterator to make sure that only descriptions corresponding to one location
        // are put into the descriptions' Map
        int i = 0;
        while (i < Language.values().length && rs.next()) {
            lang = Language.valueOf(rs.getString(databaseProperties.getString("location_description_lang_enum")));
            desc = rs.getString(databaseProperties.getString("location_description_description"));
            location.getDescriptions().put(lang, desc);
            i++;
        }

        return location;
    }

    public static Locker createLocker(ResultSet rs) throws SQLException {
        Locker l = new Locker();
        l.setId(rs.getInt(databaseProperties.getString("locker_id")));
        l.setNumber(rs.getInt(databaseProperties.getString("locker_number")));
        l.setLocation(rs.getString(databaseProperties.getString("locker_location")));
        return l;
    }

    // helper method for AddLocation
    // inserts lockers in the locker table
    private void insertLocker(String locationName, int number, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(databaseProperties.getString("insert_locker"));
        st.setInt(1, number);
        st.setString(2, locationName);
        st.execute();
    }

    private void updateLocation(Location location, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("update_location"));
        // set ...
        prepareUpdateOrInsertLocationStatement(location, pstmt);
        // where ...
        pstmt.setString(9, location.getName());
        pstmt.execute();
    }

    private void prepareUpdateOrInsertLocationStatement(Location location, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, location.getName());
        pstmt.setInt(2, location.getNumberOfSeats());
        pstmt.setInt(3, location.getNumberOfLockers());
        pstmt.setString(4, location.getMapsFrame());
        pstmt.setString(5, location.getImageUrl());
        pstmt.setString(6, location.getAddress());
        pstmt.setString(7, location.getStartPeriodLockers() == null ? "" : location.getStartPeriodLockers().toString());
        pstmt.setString(8, location.getEndPeriodLockers() == null ? "" : location.getEndPeriodLockers().toString());
    }

    private void prepareUpdateOrInsertLocationDescriptionStatement(String locationName, Language lang
            , String description, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1,locationName);
        pstmt.setString(2,lang.toString());
        pstmt.setString(3, description);
    }
}

