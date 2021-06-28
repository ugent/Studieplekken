package blok2.daos;

import blok2.model.Building;
import blok2.model.reservables.Location;

import java.util.List;

public interface IBuildingDao {

    // *************************************
    // *   CRUD operations for BUILDING   *
    // *************************************/

    /**
     * get all authorities
     */
    List<Building> getAllBuildings();

    /**
     * get building by its id.
     */
    Building getBuildingById(int buildingId);

    /**
     * get locations linked to a building
     */
    List<Location> getLocationsInBuilding(int buildingId);

    /**
     * Add an Building to the database. BuildingId is ignored.
     *
     * @return the added building with updated buildingId
     */
    Building addBuilding(Building building);

    /**
     * Updates the building given by the buildingId
     *
     * @param updatedBuilding Building with new values, with Building.buildingId the building to update
     */
    void updateBuilding(Building building);

    /**
     * delete building by its id, including Locations that are located in this building
     */
    void deleteBuilding(int buildingId);
}
