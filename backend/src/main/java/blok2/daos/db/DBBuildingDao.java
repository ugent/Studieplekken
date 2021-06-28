package blok2.daos.db;

import blok2.helpers.Resources;
import blok2.model.Building;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class DBBuildingDao extends DAO {

    // *************************************
    // *   CRUD operations for BUILDING   *
    // *************************************/

    public static Building createBuilding(ResultSet rs) throws SQLException {
        int buildingId = rs.getInt(Resources.databaseProperties.getString("buildings_building_id"));
        String name = rs.getString(Resources.databaseProperties.getString("buildings_name"));
        String address = rs.getString(Resources.databaseProperties.getString("buildings_address"));
        return new Building(buildingId, name, address);
    }

}
