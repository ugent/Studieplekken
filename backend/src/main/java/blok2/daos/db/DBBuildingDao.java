package blok2.daos.db;

import blok2.daos.IBuildingDao;
import blok2.helpers.Resources;
import blok2.model.Building;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class DBBuildingDao extends DAO implements IBuildingDao {

    private static final Logger logger = Logger.getLogger(DBBuildingDao.class.getSimpleName());

    // *************************************
    // *   CRUD operations for BUILDING   *
    // *************************************/

    @Override
    public List<Building> getAllBuildings() throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<Building> buildings = new ArrayList<>();
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(Resources.databaseProperties.getString("all_buildings"));

            while (rs.next()) {
                Building building = createBuilding(rs);
                buildings.add(building);
            }

            return buildings;
        }
    }

    @Override
    public Building getBuildingById(int buildingId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_building_by_id"));
            pstmt.setInt(1, buildingId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createBuilding(rs);
            }
            return null;
        }
    }

    @Override
    public Building addBuilding(Building building) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("add_building"));
            pstmt.setString(1, building.getName());
            pstmt.setString(2, building.getAddress());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                building.setBuildingId(rs.getInt(Resources.databaseProperties.getString("buildings_building_id")));
                return building;
            }
            return null;
        }
    }

    @Override
    public void updateBuilding(Building updatedBuilding) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("update_building"));
            pstmt.setString(1, updatedBuilding.getName());
            pstmt.setString(2, updatedBuilding.getAddress());
            pstmt.setInt(3, updatedBuilding.getBuildingId());
            pstmt.execute();
        }
    }

    @Override
    public void deleteBuilding(int buildingId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                deleteBuildings(buildingId, conn);

                PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_building"));
                pstmt.setInt(1, buildingId);
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

    public static Building createBuilding(ResultSet rs) throws SQLException {
        int buildingId = rs.getInt(Resources.databaseProperties.getString("buildings_building_id"));
        String name = rs.getString(Resources.databaseProperties.getString("buildings_name"));
        String address = rs.getString(Resources.databaseProperties.getString("buildings_address"));
        return new Building(buildingId, name, address);
    }

    private void deleteBuildings(int buildingId, Connection conn) throws SQLException {
        // location has its own FK to delete, get all locations and use LocationDao to delete
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_locations_in_building"));
        pstmt.setInt(1, buildingId);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            String locationName = rs.getString(Resources.databaseProperties.getString("location_name"));
            DBLocationDao.deleteLocationWithCascade(locationName, conn);
        }
    }
}
