package blok2.database.repositories;

import blok2.extensions.orm.LocationNameAndNextReservableFrom;
import blok2.model.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Integer> {

    /**
     * Get a list of all active locations (i.e. those that are already approved).
     */
    @Query("select l from Location l where l.approved = true and l.hidden = false")
    List<Location> findAllActiveLocations();

    /**
     * Gets a location with the given name.
     */
    Optional<Location> findLocationByName(String locationName);

    /**
     * Return all locations that are yet to be approved
     */
    List<Location> findAllByApprovedFalse();

    /**
     * Return all locations that are within an authority
     */
    @Query("select l from Location l where l.authority.authorityId = ?1")
    List<Location> findAllByAuthorityId(int authorityId);

    /**
     * Return all locations that are within a building
     */
    @Query("select l from Location l where l.building.buildingId = ?1")
    List<Location> findAllByBuildingId(int buildingId);

    /**
     * Note: this is a named query (cfr. orm.xml)
     *
     * Returns an array of 7 strings for each location that is opened in the week specified by the given
     * week number in the given year.
     *
     * Each string is in the form of 'HH24:MI - HH24:MI' to indicate the opening and closing hour at
     * monday, tuesday, ..., sunday but can also be null to indicate that the location is not open that day.
     */
    List<String[]> getOpeningHoursOverview(LocalDate monday, LocalDate sunday);

    /**
     * Note: this is a named query (cfr. orm.xml)
     *
     * Get a list of objects that tell for each location what the next reservable from is.
     */
    List<LocationNameAndNextReservableFrom> getNextReservationMomentsOfAllLocations();

}
