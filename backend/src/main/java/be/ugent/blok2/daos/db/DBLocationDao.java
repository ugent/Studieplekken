package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.daos.ILocationDao;
import be.ugent.blok2.daos.IScannerLocationDao;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.reservables.Locker;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DBLocationDao extends ADB implements ILocationDao {

    IAccountDao accountDao;
    IScannerLocationDao scannerLocationDao;

    public DBLocationDao(IAccountDao accountDao, IScannerLocationDao scannerLocationDao) {
        this.accountDao = accountDao;
        this.scannerLocationDao = scannerLocationDao;
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

                addLocation(location, conn);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void addLocation(Location location, Connection conn) throws SQLException {
        // insert location into the database
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_location"));
        prepareUpdateOrInsertLocationStatement(location, pstmt);
        pstmt.executeUpdate();

        // insert the lockers corresponding to the location into the database
        for (int i = 0; i < location.getNumberOfLockers(); i++) {
            insertLocker(location.getName(), i, conn);
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
    public void updateLocation(String locationName, Location location) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                if (!locationName.equals(location.getName())) {
                    // add location and lockers so, we have added
                    // the 'updated' location and there are new lockers
                    // with FK to the new location
                    addLocation(location, conn);

                    // update the remaining tables with FK to location:
                    // calendar, scanner_locations, location_reservations,
                    // locker_reservations and penalty_book
                    updateForeignKeysToLocation(locationName, location.getName(), conn);

                    // delete lockers with FK to the old location,
                    // and eventually delete the old location as well
                    deleteLockers(locationName, conn);
                    deleteLocation(locationName, conn);
                } else {
                    updateLocation(location, conn);
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
    public void deleteLocation(String locationName) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                // delete calendar
                deleteCalendarPeriods(locationName, conn);

                // delete scanners_location
                DBScannerLocationDao.deleteAllScannersOfLocation(locationName, conn);

                // delete location_reservations
                deleteLocationReservations(locationName, conn);

                // delete penalty_book entries
                deletePenaltyBookEntries(locationName, conn);

                // delete locker_reservations
                deleteLockers(locationName, conn);

                // and finally, delete the location
                deleteLocation(locationName, conn);

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
    public List<Locker> getLockers(String locationName) throws SQLException {
        try (Connection conn = getConnection()) {
            List<Locker> lockers = new ArrayList<>();
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
    public Map<String, Integer> getCountOfReservations(CustomDate date) throws SQLException {
        HashMap<String, Integer> count = new HashMap<>();

        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("count_location_reservations_on_date"));
            pstmt.setString(1, date.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString(1);
                int c = rs.getInt(2);
                count.put(name, c);
            }

            return count;
        }
    }

    // this method prevents a lot of duplicate code by creating a location out of a row in the ResultSet
    public static Location createLocation(ResultSet rs) throws SQLException {
        String name = rs.getString(databaseProperties.getString("location_name"));
        int numberOfSeats = rs.getInt(databaseProperties.getString("location_number_of_seats"));
        int numberOfLockers = rs.getInt(databaseProperties.getString("location_number_of_lockers"));
        String imageUrl = rs.getString(databaseProperties.getString("location_image_url"));
        String address = rs.getString(databaseProperties.getString("location_address"));
        CustomDate startPeriodLockers = CustomDate.parseString(rs.getString(databaseProperties.getString("location_start_period_lockers")));
        CustomDate endPeriodLockers = CustomDate.parseString(rs.getString(databaseProperties.getString("location_end_period_lockers")));

        return new Location(name, address, numberOfSeats, numberOfLockers, imageUrl,
                startPeriodLockers, endPeriodLockers);
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

    private void prepareUpdateOrInsertLocationStatement(Location location, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, location.getName());
        pstmt.setInt(2, location.getNumberOfSeats());
        pstmt.setInt(3, location.getNumberOfLockers());
        pstmt.setString(4, location.getImageUrl());
        pstmt.setString(5, location.getAddress());
        pstmt.setString(6, location.getStartPeriodLockers() == null ? "" : location.getStartPeriodLockers().toString());
        pstmt.setString(7, location.getEndPeriodLockers() == null ? "" : location.getEndPeriodLockers().toString());
    }

    private void deleteCalendarPeriods(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_calendar_periods_of_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    private void deleteLocationReservations(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn
                .prepareStatement(databaseProperties.getString("delete_location_reservations_of_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    private void deletePenaltyBookEntries(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_penalties_of_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    private void deleteLockers(String locationName, Connection conn) throws SQLException {
        deleteLockerReservations(locationName, conn);

        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_lockers_of_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    private void deleteLockerReservations(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn
                .prepareStatement(databaseProperties.getString("delete_locker_reservations_in_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    private void deleteLocation(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    /**
     * update the FK of calendar, scanner_locations, location_reservations,
     * penalty_book and locker_reservations
     */
    private void updateForeignKeysToLocation(String oldLocationName, String newLocationName, Connection conn) throws SQLException {
        // update calendar
        updateForeignKeyOfCalendarPeriods(oldLocationName, newLocationName, conn);

        // update scanner_locations
        updateForeignKeyOfScannerLocations(oldLocationName, newLocationName, conn);

        // update location_reservations
        updateForeignKeyOfLocationReservations(oldLocationName, newLocationName, conn);

        // update locker_reservations
        updateForeignKeyOfLockerReservations(oldLocationName, newLocationName, conn);

        // update penalty_book
        updateForeignKeyOfPenaltyBook(oldLocationName, newLocationName, conn);
    }

    private void updateForeignKeyOfCalendarPeriods(String oldLocationName, String newLocationName, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties
                .getString("update_fk_location_name_in_calendar_periods"));
        pstmt.setString(1, newLocationName);
        pstmt.setString(2, oldLocationName);
        pstmt.execute();
    }

    private void updateForeignKeyOfScannerLocations(String oldLocationName, String newLocationName, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties
                .getString("update_fk_scanners_location_to_locations"));
        pstmt.setString(1, newLocationName);
        pstmt.setString(2, oldLocationName);
        pstmt.execute();
    }

    private void updateForeignKeyOfLocationReservations(String oldLocationName, String newLocationName, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties
                .getString("update_fk_location_reservations_to_location"));
        pstmt.setString(1, newLocationName);
        pstmt.setString(2, oldLocationName);
        pstmt.execute();
    }

    private void updateForeignKeyOfLockerReservations(String oldLocationName, String newLocationName, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties
                .getString("update_fk_locker_reservations_to_location"));
        pstmt.setString(1, newLocationName);
        pstmt.setString(2, oldLocationName);
        pstmt.execute();
    }

    private void updateForeignKeyOfPenaltyBook(String oldLocationName, String newLocationName, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties
                .getString("update_fk_penalty_book_to_locations"));
        pstmt.setString(1, newLocationName);
        pstmt.setString(2, oldLocationName);
        pstmt.execute();
    }

    private void updateLocation(Location location, Connection conn) throws SQLException {
        Location oldLocation = getLocation(location.getName(), conn);

        if (oldLocation == null)
            return;

        if (oldLocation.getNumberOfLockers() != location.getNumberOfLockers()) {
            updateNumberOfLockers(location.getName(), oldLocation.getNumberOfLockers()
                    , location.getNumberOfLockers(), conn);
        }

        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("update_location"));
        // set ...
        prepareUpdateOrInsertLocationStatement(location, pstmt);
        // where ...
        pstmt.setString(8, location.getName());
        pstmt.execute();
    }

    private void updateNumberOfLockers(String locationName, int from, int to, Connection conn) throws SQLException {
        if (from > to) {
            decreaseNumberOfLockers(locationName, from, to, conn);
        } else {
            increaseNumberOfLockers(locationName, from, to, conn);
        }
    }

    private void increaseNumberOfLockers(String locationName, int from, int to, Connection conn) throws SQLException {
        for (int i = from; i < to; i++) {
            insertLocker(locationName, i, conn);
        }
    }

    private void decreaseNumberOfLockers(String locationName, int from, int to, Connection conn) throws SQLException {
        for (int i = from - 1; i >= to; i--) {
            deleteLocker(locationName, i, conn);
        }
    }

    private void deleteLocker(String locationName, int number, Connection conn) throws SQLException {
        deleteLockerReservation(locationName, number, conn);

        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_locker"));
        pstmt.setString(1, locationName);
        pstmt.setInt(2, number);
        pstmt.execute();
    }

    private void deleteLockerReservation(String locationName, int number, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_locker_reservation"));
        pstmt.setString(1, locationName);
        pstmt.setInt(2, number);
        pstmt.execute();
    }
}

