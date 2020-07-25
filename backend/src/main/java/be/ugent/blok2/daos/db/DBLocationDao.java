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
    public List<Location> getAllLocations() throws SQLException {
        try (Connection conn = getConnection()) {
            List<Location> locations = new ArrayList<>();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(databaseProperties.getString("all_locations"));

            while (rs.next()) {
                Location location = createLocation(rs);
                locations.add(location);
            }

            return locations;
        }
    }

    @Override
    public void addLocation(Location location) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                // insert location into the database
                PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_location"));
                prepareUpdateOrInsertLocationStatement(location, pstmt);
                pstmt.executeUpdate();

                // insert descriptions corresponding to the location into the database
                for (Language lang : location.getDescriptions().keySet()) {
                    pstmt = conn.prepareStatement(databaseProperties.getString("insert_location_descriptions"));
                    prepareUpdateOrInsertLocationDescriptionStatement(
                            location.getName(),
                            lang,
                            location.getDescriptions().get(lang),
                            pstmt
                    );
                    pstmt.executeUpdate();
                }

                // insert the lockers corresponding to the location into the databse
                for (int i = 0; i < location.getNumberOfLockers(); i++) {
                    insertLocker(location.getName(), i, conn);
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public Location getLocation(String name) throws SQLException {
        try (Connection conn = getConnection()) {
            return getLocation(name, conn);
        }
    }

    public static Location getLocation(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_location"));
        pstmt.setString(1, locationName);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return createLocation(rs);
        }
        return null;
    }

    @Override
    public void changeLocation(String name, Location location) throws SQLException {
        boolean nameChanged = !name.equals(location.getName());

        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                // Delete location descriptions
                PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_location_descriptions"));
                pstmt.setString(1, name);
                pstmt.executeUpdate();

                // Remove scanners
                List<String> scanners = getScannersFromLocation(location.getName());
                pstmt = conn.prepareStatement(databaseProperties.getString("delete_scanners_of_location"));
                pstmt.setString(1, location.getName());
                pstmt.executeUpdate();

                if (nameChanged) {
                    // nieuwe locatie invoegen
                    pstmt = conn.prepareStatement(databaseProperties.getString("insert_location"));
                    prepareUpdateOrInsertLocationStatement(location, pstmt);
                    pstmt.executeUpdate();

                    // kalender wijzigen
                    pstmt = conn.prepareStatement(databaseProperties.getString("update_location_calendar"));
                    pstmt.setString(1, location.getName());
                    pstmt.setString(2, name);
                    pstmt.executeUpdate();

                    // lockers wijzigen
                    pstmt = conn.prepareStatement(databaseProperties.getString("change_locker_location"));
                    pstmt.setString(1, location.getName());
                    pstmt.setString(2, name);
                    pstmt.executeUpdate();

                    // oude locatie verwijderen
                    pstmt = conn.prepareStatement(databaseProperties.getString("delete_location"));
                    pstmt.setString(1, name);
                } else {
                    pstmt = conn.prepareStatement(databaseProperties.getString("update_location"));
                    prepareUpdateOrInsertLocationStatement(location, pstmt);
                    pstmt.setString(9, name);
                }
                pstmt.executeUpdate();

                // lockers eventueel toevoegen of verwijderen

                // Add scanners
                for (String s : scanners) {
                    pstmt = conn.prepareStatement(databaseProperties.getString("insert_scanner_and_location"));
                    pstmt.setString(1, s.split(" ")[0]);
                    pstmt.setString(2, location.getName());
                    pstmt.executeUpdate();
                }

                // Add location descriptions
                for (Language lang : location.getDescriptions().keySet()) {
                    pstmt = conn.prepareStatement(databaseProperties.getString("insert_location_descriptions"));
                    pstmt.setString(1, location.getName());
                    pstmt.setString(2, lang.toString());
                    pstmt.setString(3, location.getDescriptions().get(lang));
                    pstmt.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public Collection<Locker> getLockers(String locationName) throws SQLException {
        try (Connection conn = getConnection()) {
            Collection<Locker> lockers = new ArrayList<>();
            String query = databaseProperties.getString("get_lockers_where_<?>");
            query = query.replace("<?>", "l.location_name = ?");
            PreparedStatement st = conn.prepareStatement(query);
            st.setString(1, locationName);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                //int lockerID = rs.getInt(databaseProperties.getString("locker_id"));
                int number = rs.getInt(databaseProperties.getString("locker_number"));

                Locker locker = new Locker();
                //locker.setId(lockerID);
                locker.setNumber(number);

                Location location = DBLocationDao.createLocation(rs);
                locker.setLocation(location);

                lockers.add(locker);
            }
            return lockers;
        }
    }

    @Override
    public void addLockers(String locationName, int count) throws SQLException {
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
        }
    }

    @Override
    public void deleteLockers(String locationName, int startNumber) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            deleteLockers(locationName, startNumber, conn);

            // update location: number_of_lockers has been updated
            Location location = getLocation(locationName, conn);

            if (location == null)
                return;

            int r = location.getNumberOfLockers() - startNumber;
            int n = location.getNumberOfLockers() - r;
            location.setNumberOfLockers(n);

            updateLocation(location, conn);

            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    private void deleteLockers(String locationName, int startNumber, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(databaseProperties.getString("delete_lockers_of_location_from_number"));
        st.setString(1, locationName);
        st.setInt(2, startNumber);
        st.execute();
    }

    @Override
    public void deleteLocation(String name) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                // Delete scanners
                PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_scanners_of_location"));
                pstmt.setString(1, name);
                pstmt.execute();

                // Delete location reservations for this location
                pstmt = conn.prepareStatement(databaseProperties.getString("delete_location_reservations_of_location"));
                pstmt.setString(1, name);
                pstmt.execute();

                // Delete rows in calendar table that have a foreign key for this location
                pstmt = conn.prepareStatement(databaseProperties.getString("delete_calendar_of_location"));
                pstmt.setString(1, name);
                pstmt.execute();

                // Delete locker reservations of lockers of this location
                List<Locker> locker_ids = new ArrayList<>();
                String query = databaseProperties.getString("get_lockers_where_<?>");
                query = query.replace("<?>", "l.location_name = ?");
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, name);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Locker locker = createLocker(rs);
                    locker_ids.add(locker);
                }
                for (Locker l : locker_ids) {
                    pstmt = conn.prepareStatement(databaseProperties.getString("delete_locker_reservation"));
                    pstmt.setString(1, l.getLocation().getName());
                    pstmt.setInt(2, l.getNumber());
                    pstmt.execute();
                }

                // Delete descriptions
                pstmt = conn.prepareStatement(databaseProperties.getString("delete_location_descriptions"));
                pstmt.setString(1, name);
                pstmt.execute();

                // Delete lockers of location
                pstmt = conn.prepareStatement(databaseProperties.getString("delete_lockers_of_location"));
                pstmt.setString(1, name);
                pstmt.execute();

                // Delete penalty events that occurred in this location
                pstmt = conn.prepareStatement(databaseProperties.getString("delete_penalties_of_location"));
                pstmt.setString(1, name);
                pstmt.execute();

                // Delete the location
                pstmt = conn.prepareStatement(databaseProperties.getString("delete_location"));
                pstmt.setString(1, name);
                pstmt.execute();
                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public Collection<Day> getCalendarDays(String locationName) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_calendar_of_location"));
                pstmt.setString(1, locationName);
                ResultSet rs = pstmt.executeQuery();
                Collection<Day> calendar = new ArrayList<>();
                while (rs.next()) {
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
                }
                return calendar;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public void addCalendarDays(String locationName, Calendar calendar) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                for (Day day : calendar.getDays()) {
                    insertCalendarDay(locationName, day, conn);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public void setScannersForLocation(String name, List<User> scanners) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                // first delete
                PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_scanners_of_location"));
                pstmt.setString(1, name);
                pstmt.executeUpdate();

                for (User u : scanners) {
                    pstmt = conn.prepareStatement(databaseProperties.getString("insert_scanner_and_location"));
                    pstmt.setString(1, u.getAugentID());
                    pstmt.setString(2, name);
                    pstmt.execute();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public List<String> getScannersFromLocation(String locationName) throws SQLException {
        ArrayList<String> scanners = new ArrayList<>();

        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_scanners_of_location"));
            pstmt.setString(1, locationName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String augentId = rs.getString(databaseProperties.getString("scanners_location_user_augentid"));
                scanners.add(augentId);
            }

            return scanners;
        }
    }

    @Override
    public Map<String, Integer> getCountOfReservations(CustomDate date) throws SQLException {
        HashMap<String, Integer> count = new HashMap<>();

        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("count_location_reservations_on_date"));
            pstmt.setString(1, date.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String name =rs.getString(1);
                int c = rs.getInt(2);
                count.put(name,c);
            }

            return count;
        }
    }

    // helper method to add a calendar day to the calendar table
    private void insertCalendarDay(String locationName, Day day, Connection conn) throws SQLException {

        //check if there is already a row in the database for this location and date
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_calendar_day_count"));
        pstmt.setString(1, day.getDate().toString());
        pstmt.setString(2,locationName);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            int count = rs.getInt(1);

            //if count = 0, insert the calendar day
            if (count == 0) {
                pstmt = conn.prepareStatement(databaseProperties.getString("insert_calendar_day"));
                pstmt.setString(1, day.getDate().toString());
                pstmt.setString(2, locationName);

                LocalTime openingTime = LocalTime.of(
                        day.getOpeningHour().getHours(),
                        day.getOpeningHour().getMinutes(),
                        day.getOpeningHour().getSeconds()
                );

                pstmt.setTime(3, java.sql.Time.valueOf(openingTime));

                LocalTime closingTime = LocalTime.of(
                        day.getClosingHour().getHours(),
                        day.getClosingHour().getMinutes(),
                        day.getClosingHour().getSeconds()
                );

                pstmt.setTime(4, java.sql.Time.valueOf(closingTime));
                pstmt.setString(5, day.getOpenForReservationDate().toString());
                pstmt.execute();
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
    public void deleteCalendarDays(String locationName, String startdate, String enddate) throws SQLException {
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
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
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
        int i = 1;
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
        //l.setId(rs.getInt(databaseProperties.getString("locker_id")));
        l.setNumber(rs.getInt(databaseProperties.getString("locker_number")));
        Location location = DBLocationDao.createLocation(rs);
        l.setLocation(location);
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

