package blok2.database.dao;

import blok2.extensions.orm.LocationNameAndNextReservableFrom;
import blok2.model.location.Location;

import java.util.List;
import java.util.Map;

/**
 * Note that all add/update/delete operations on lockers will happen in cascade
 * upon add/update/delete operations on a location if the number of lockers change
 */
public interface ILocationDao {
    /**
     * Check if a location with the given name exists.
     */
    Boolean existsLocationByName(String locationName);

    /**
     * Gets a location with the given id
     */
    Location getLocationById(int locationId);

    /**
     * Gets a location with the given name.
     */
    Location getLocationByName(String locationName);

    /**
     * Get a list of all locations.
     *
     * @return a list of all locations in the database.
     */
    List<Location> getAllLocations();

    /**
     * Get a list of all available locations,
     * excluding unapproved and hidden locations.
     */
    List<Location> getAllActiveLocations();

    /**
     * Return all locations that are yet to be approved/denied
     */
    List<Location> getAllUnapprovedLocations();

    /**
     * Get a list of pairs which tell for each location what the next reservable from is.
     * The pair maps the location name to the reservable.
     */
    List<LocationNameAndNextReservableFrom> getNextReservationMomentsOfAllLocations();

    /**
     * Adds a location
     */
    Location addLocation(Location location);

    /**
     * Updates a location
     */
    void updateLocation(Location location);

    /**
     * Approve or deny a new location
     */
    void approveLocation(Location location, boolean approval);

    /**
     * Deletes a location
     */
    void deleteLocation(int locationId);

    /**
     * Returns an array of 7 strings for each location that is opened in the week specified by the given
     * week number in the given year.
     * <p>
     * Each string is in the form of 'HH24:MI - HH24:MI' to indicate the opening and closing hour at
     * monday, tuesday, ..., sunday but can also be null to indicate that the location is not open that day.
     */
    Map<String, String[]> getOpeningOverviewOfWeek(int year, int weekNr);

    /**
     * Initialize the fields of the location with all tags related data.
     */
    void initializeTags(Location location);
}
