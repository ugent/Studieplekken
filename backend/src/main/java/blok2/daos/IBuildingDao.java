package blok2.daos;

import blok2.model.Building;

import java.sql.SQLException;
import java.util.List;

public interface IBuildingDao {

    // *************************************
    // *   CRUD operations for BUILDING   *
    // *************************************/

    /**
     * get all authorities
     */
    List<Building> getAllBuildings() throws SQLException;

    /**
     * get building by its id.
     */
    Building getBuildingById(int buildingId) throws SQLException;

    /**
     * Add an Building to the database. BuildingId is ignored.
     *
     * @return the added building with updated buildingId
     */
    Building addBuilding(Building buildingId) throws SQLException;

    /**
     * Updates the building given by the buildingId
     *
     * @param updatedBuilding Building with new values, with Building.buildingId the building to update
     */
    void updateBuilding(Building updatedBuilding) throws SQLException;

    /**
     * delete building by its id, including Locations that are located in this building
     */
    void deleteBuilding(int buildingId) throws SQLException;
}
