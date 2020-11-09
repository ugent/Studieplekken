package blok2.daos.db;

import blok2.daos.IAuthorityDao;
import blok2.helpers.Resources;
import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DBAuthorityDao extends DAO implements IAuthorityDao {

    // *************************************
    // *   CRUD operations for AUTHORITY   *
    // *************************************/

    @Override
    public List<Authority> getAllAuthorities() throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<Authority> authorities = new ArrayList<>();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(Resources.databaseProperties.getString("all_authorities"));

            while (rs.next()) {
                Authority authority = createAuthority(rs);
                authorities.add(authority);
            }

            return authorities;
        }
    }

    @Override
    public Authority getAuthorityByName(String name) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("authority_from_name"));
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createAuthority(rs);
            }
            return null;
        }
    }

    @Override
    public Authority getAuthorityByAuthorityId(int authorityId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("authority_from_authority_id"));
            pstmt.setInt(1, authorityId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createAuthority(rs);
            }
            return null;
        }
    }

    @Override
    public Authority addAuthority(Authority authority) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_authority"));
            preparedAuthorityInsertOrUpdate(authority, pstmt);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                authority.setAuthorityId(rs.getInt(Resources.databaseProperties.getString("authority_authority_id")));
                return authority;
            }
            return null;
        }
    }

    @Override
    public void updateAuthority(Authority updatedAuthority) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("update_authority"));
            preparedAuthorityInsertOrUpdate(updatedAuthority, pstmt);
            pstmt.setInt(3, updatedAuthority.getAuthorityId());
            pstmt.execute();
        }
    }

    @Override
    public void deleteAuthority(int authorityId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                deleteLocations(authorityId, conn);
                deleteRolesUserAuthority(authorityId, conn);
                deleteAuthority(authorityId, conn);

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

    // ************************************************
    // *   CRUD operations for ROLES_USER_AUTHORITY   *
    // ************************************************/

    @Override
    public List<Authority> getAuthoritiesFromUser(String augentId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return getAuthoritiesFromUser(augentId, conn);
        }
    }

    public static List<Authority> getAuthoritiesFromUser(String augentId, Connection conn) throws SQLException {

        List<Authority> authorities = new ArrayList<>();

        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("authorities_from_user"));
        pstmt.setString(1, augentId);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            Authority authority = createAuthority(rs);
            authorities.add(authority);
        }

        return authorities;
    }

    @Override
    public List<Location> getLocationsInAuthoritiesOfUser(String augentId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<Location> locations = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_locations_manageable_by_user"));
            pstmt.setString(1, augentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                locations.add(DBLocationDao.createLocation(rs, conn));
            }

            return locations;
        }
    }

    @Override
    public List<User> getUsersFromAuthority(int authorityId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<User> users = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("authority_get_users"));
            pstmt.setInt(1, authorityId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = DBAccountDao.createUser(rs, conn,false);
                users.add(user);
            }

            return users;

        }
    }

    @Override
    public boolean addUserToAuthority(String augentid, int authorityId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_role_user_authority"));
            pstmt.setString(1, augentid);
            pstmt.setInt(2, authorityId);
            pstmt.execute();
            return true;
        }
    }

    @Override
    public void deleteUserFromAuthority(String augentid, int authorityId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("remove_role_user_authority"));
            pstmt.setString(1, augentid);
            pstmt.setInt(2, authorityId);
            pstmt.execute();
        }
    }

    // *************************
    // *   Auxiliary methods   *
    // *************************/

    private void preparedAuthorityInsertOrUpdate(Authority authority, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, authority.getAuthorityName());
        pstmt.setString(2, authority.getDescription());
    }

    private void deleteRolesUserAuthority(int authorityId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn
                .prepareStatement(Resources.databaseProperties.getString("delete_roles_user_authority_of_authority"));
        pstmt.setInt(1, authorityId);
        pstmt.execute();
    }

    private void deleteLocations(int authorityId, Connection conn) throws SQLException {
        // location has its own FK to delete, get all locations and use LocationDao to delete
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_locations_from_authority"));
        pstmt.setInt(1, authorityId);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            String locationName = rs.getString(Resources.databaseProperties.getString("location_name"));
            DBLocationDao.deleteLocationWithCascade(locationName, conn);
        }
    }

    private void deleteAuthority(int authorityId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn
                .prepareStatement(Resources.databaseProperties.getString("delete_authority"));
        pstmt.setInt(1, authorityId);
        pstmt.execute();
    }

    public static Authority createAuthority(ResultSet rs) throws SQLException {
        int authorityId = rs.getInt(Resources.databaseProperties.getString("authority_authority_id"));
        String name = rs.getString(Resources.databaseProperties.getString("authority_name"));
        String description = rs.getString(Resources.databaseProperties.getString("authority_description"));
        return new Authority(authorityId, name, description);
    }
}
