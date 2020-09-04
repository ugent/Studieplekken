package blok2.daos.db;

import blok2.daos.IAccountDao;
import blok2.daos.ILocationDao;
import blok2.daos.IScannerLocationDao;
import blok2.helpers.Resources;
import blok2.helpers.date.CustomDate;
import blok2.model.LocationTag;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DBLocationDao extends DAO implements ILocationDao {

    IAccountDao accountDao;
    IScannerLocationDao scannerLocationDao;

    public DBLocationDao(IAccountDao accountDao, IScannerLocationDao scannerLocationDao) {
        this.accountDao = accountDao;
        this.scannerLocationDao = scannerLocationDao;
    }

    @Override
    public List<Location> getAllLocations() throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<Location> locations = new ArrayList<>();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(Resources.databaseProperties.getString("all_locations"));

            while (rs.next()) {
                Location location = createLocation(rs,conn);
                locations.add(location);
            }

            return locations;
        }
    }

    @Override
    public void addLocation(Location location) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            addLocationAsTransaction(location, conn);
        }
    }
    private void addLocation(Location location, Connection conn) throws SQLException{

        // insert location into the database
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_location"));
        prepareUpdateOrInsertLocationStatement(location, pstmt);
        pstmt.executeUpdate();

        insertTags(location.getName(), location.getTags(), conn);

        // insert the lockers corresponding to the location into the database
        for (int i = 0; i < location.getNumberOfLockers(); i++) {
            insertLocker(location.getName(), i, conn);
        }
    }
    private void addLocationAsTransaction(Location location, Connection conn) throws SQLException {
        try {
            conn.setAutoCommit(false);
            addLocation(location,conn);
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public Location getLocation(String name) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return getLocation(name, conn);
        }
    }

    public static Location getLocation(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_location"));
        pstmt.setString(1, locationName);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return createLocation(rs, conn);
        }
        return null;
    }

    @Override
    public void updateLocation(String locationName, Location location) throws SQLException {
        try (Connection conn = adb.getConnection()) {
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
                    deleteTagsFromLocation(locationName, conn);
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

    private void deleteTagsFromLocation(String locationId, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("remove_tags_from_location"));
        st.setString(1, locationId);
        st.execute();
    }

    @Override
    public void deleteLocation(String locationName) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // delete calendar periods
                deleteCalendarPeriods(locationName, conn);

                // delete calendar periods for lockers
                deleteCalendarPeriodsForLockers(locationName, conn);

                // delete scanners_location
                DBScannerLocationDao.deleteAllScannersOfLocation(locationName, conn);

                // delete location_reservations
                deleteLocationReservations(locationName, conn);

                // delete penalty_book entries
                deletePenaltyBookEntries(locationName, conn);

                // delete locker_reservations
                deleteLockers(locationName, conn);

                // delete tags from location
                deleteTagsFromLocation(locationName,conn);

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
        try (Connection conn = adb.getConnection()) {
            List<Locker> lockers = new ArrayList<>();
            String query = Resources.databaseProperties.getString("get_lockers_where_<?>");
            query = query.replace("<?>", "l.location_name = ?");
            PreparedStatement st = conn.prepareStatement(query);
            st.setString(1, locationName);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                //int lockerID = rs.getInt(Resources.databaseProperties.getString("locker_id"));
                int number = rs.getInt(Resources.databaseProperties.getString("locker_number"));

                Locker locker = new Locker();
                //locker.setId(lockerID);
                locker.setNumber(number);

                Location location = DBLocationDao.createLocation(rs,conn);
                locker.setLocation(location);

                lockers.add(locker);
            }
            return lockers;
        }
    }

    private static ResultSet getTagsFromLocation(String locationName, Connection conn) throws SQLException {
        PreparedStatement pst = conn.prepareStatement(Resources.databaseProperties.getString("tags_from_location"));
        pst.setString(1, locationName);
        return pst.executeQuery();

    }

    @Override
    public Map<String, Integer> getCountOfReservations(CustomDate date) throws SQLException {
        HashMap<String, Integer> count = new HashMap<>();

        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_location_reservations_on_date"));
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
    public static Location createLocation(ResultSet rs, ResultSet rsTags) throws SQLException {
        String name = rs.getString(Resources.databaseProperties.getString("location_name"));
        int numberOfSeats = rs.getInt(Resources.databaseProperties.getString("location_number_of_seats"));
        int numberOfLockers = rs.getInt(Resources.databaseProperties.getString("location_number_of_lockers"));
        String imageUrl = rs.getString(Resources.databaseProperties.getString("location_image_url"));
        String address = rs.getString(Resources.databaseProperties.getString("location_address"));
        int authorityId = rs.getInt(Resources.databaseProperties.getString("location_authority_id"));
        String descriptionDutch = rs.getString(Resources.databaseProperties.getString("location_description_dutch"));
        String descriptionEnglish = rs.getString(Resources.databaseProperties.getString("location_description_english"));
        ArrayList<LocationTag> tags = new ArrayList<>();
        while (rsTags.next()) {
            tags.add(new LocationTag(
                    rsTags.getInt(Resources.databaseProperties.getString("tags_tag_id")),
                    rsTags.getString(Resources.databaseProperties.getString("tags_dutch")),
                    rsTags.getString(Resources.databaseProperties.getString("tags_english"))
            ));
        }
        return new Location(name, address, numberOfSeats, numberOfLockers, imageUrl, authorityId,descriptionDutch,descriptionEnglish , tags);
    }

    /**
     * create a location from the resulset, where tags are automatically fetched too
     */
    public static Location createLocation(ResultSet rs, Connection conn) throws SQLException{
        ResultSet rsTags = getTagsFromLocation(rs.getString(Resources.databaseProperties.getString("location_name")),conn);
        return createLocation(rs, rsTags);
    }


    public static Locker createLocker(ResultSet rs, Connection conn) throws SQLException {
        Locker l = new Locker();
        l.setNumber(rs.getInt(Resources.databaseProperties.getString("locker_number")));
        Location location = DBLocationDao.createLocation(rs, conn);
        l.setLocation(location);
        return l;
    }

    // helper method for AddLocation
    // inserts lockers in the locker table
    private void insertLocker(String locationName, int number, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("insert_locker"));
        st.setInt(1, number);
        st.setString(2, locationName);
        st.execute();
    }

    private void insertTags(String locationName, List<LocationTag> tags, Connection conn) throws SQLException {
        if(tags != null) {
            for (LocationTag tag : tags) {
                insertTag(locationName, tag, conn);
            }
        }
    }

    private void insertTag(String locationName, LocationTag tag, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("add_tag_to_location"));
        st.setString(1, locationName);
        st.setInt(2, tag.getTagId());
        st.execute();
    }

    private void prepareUpdateOrInsertLocationStatement(Location location, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, location.getName());
        pstmt.setInt(2, location.getNumberOfSeats());
        pstmt.setInt(3, location.getNumberOfLockers());
        pstmt.setString(4, location.getImageUrl());
        pstmt.setString(5, location.getAddress());
        pstmt.setInt(6, location.getAuthorityId());
        pstmt.setString(7, location.getDescriptionDutch());
        pstmt.setString(8,location.getDescriptionEnglish());
    }

    private void deleteCalendarPeriods(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("delete_calendar_periods_of_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    private void deleteCalendarPeriodsForLockers(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("delete_calendar_periods_for_lockers_of_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    private void deleteLocationReservations(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn
                .prepareStatement(Resources.databaseProperties.getString("delete_location_reservations_of_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    private void deletePenaltyBookEntries(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_penalties_of_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    private void deleteLockers(String locationName, Connection conn) throws SQLException {
        deleteLockerReservations(locationName, conn);

        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_lockers_of_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    private void deleteLockerReservations(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn
                .prepareStatement(Resources.databaseProperties.getString("delete_locker_reservations_in_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    private void deleteLocation(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_location"));
        pstmt.setString(1, locationName);
        pstmt.execute();
    }

    /**
     * update the FK of calendar, scanner_locations, location_reservations,
     * penalty_book and locker_reservations
     */
    private void updateForeignKeysToLocation(String oldLocationName, String newLocationName, Connection conn) throws SQLException {
        // update calendar periods
        updateForeignKeyOfCalendarPeriods(oldLocationName, newLocationName, conn);

        // update calendar periods for lockers
        updateForeignKeyOfCalendarPeriodsForLockers(oldLocationName, newLocationName, conn);

        // update scanner_locations
        updateForeignKeyOfScannerLocations(oldLocationName, newLocationName, conn);

        // update location_reservations
        updateForeignKeyOfLocationReservations(oldLocationName, newLocationName, conn);

        // update locker_reservations
        updateForeignKeyOfLockerReservations(oldLocationName, newLocationName, conn);

        // update penalty_book
        updateForeignKeyOfPenaltyBook(oldLocationName, newLocationName, conn);

        //update location_tags
        updateForeignKeyOfLocationTags(oldLocationName, newLocationName, conn);

    }

    private void updateForeignKeyOfCalendarPeriods(String oldLocationName, String newLocationName, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("update_fk_location_name_in_calendar_periods"));
        pstmt.setString(1, newLocationName);
        pstmt.setString(2, oldLocationName);
        pstmt.execute();
    }

    private void updateForeignKeyOfCalendarPeriodsForLockers(String oldLocationName, String newLocationName,
                                                             Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("update_fk_location_name_in_calendar_periods_for_lockers"));
        pstmt.setString(1, newLocationName);
        pstmt.setString(2, oldLocationName);
        pstmt.execute();
    }

    private void updateForeignKeyOfScannerLocations(String oldLocationName, String newLocationName, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("update_fk_scanners_location_to_locations"));
        pstmt.setString(1, newLocationName);
        pstmt.setString(2, oldLocationName);
        pstmt.execute();
    }

    private void updateForeignKeyOfLocationReservations(String oldLocationName, String newLocationName, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("update_fk_location_reservations_to_location"));
        pstmt.setString(1, newLocationName);
        pstmt.setString(2, oldLocationName);
        pstmt.execute();
    }

    private void updateForeignKeyOfLockerReservations(String oldLocationName, String newLocationName, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("update_fk_locker_reservations_to_location"));
        pstmt.setString(1, newLocationName);
        pstmt.setString(2, oldLocationName);
        pstmt.execute();
    }

    private void updateForeignKeyOfPenaltyBook(String oldLocationName, String newLocationName, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("update_fk_penalty_book_to_locations"));
        pstmt.setString(1, newLocationName);
        pstmt.setString(2, oldLocationName);
        pstmt.execute();
    }

    private void updateForeignKeyOfLocationTags(String oldLocationName, String newLocationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("update_fk_location_tags_to_location"));
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

        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("update_location"));
        // set ...
        prepareUpdateOrInsertLocationStatement(location, pstmt);
        // where ...
        pstmt.setString(9, location.getName());
        pstmt.execute();
        //update tags by first removing them and then adding them
        deleteTagsFromLocation(location.getName(),conn);
        insertTags(location.getName(),location.getTags(),conn);
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

        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_locker"));
        pstmt.setString(1, locationName);
        pstmt.setInt(2, number);
        pstmt.execute();
    }

    private void deleteLockerReservation(String locationName, int number, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_locker_reservation"));
        pstmt.setString(1, locationName);
        pstmt.setInt(2, number);
        pstmt.execute();
    }
}

