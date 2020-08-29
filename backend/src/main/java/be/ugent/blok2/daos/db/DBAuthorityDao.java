package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.daos.IAuthorityDao;
import be.ugent.blok2.daos.ILocationDao;
import be.ugent.blok2.model.Authority;
import be.ugent.blok2.model.users.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBAuthorityDao extends ADB implements IAuthorityDao {
    IAccountDao accountDao;
    ILocationDao locationDao;

    public DBAuthorityDao(IAccountDao iAccountDao) {
        this.accountDao = iAccountDao;
    }

    @Override
    public List<Authority> getAllAuthorities() throws SQLException {
        try (Connection conn = getConnection()) {
            List<Authority> authorities = new ArrayList<>();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(databaseProperties.getString("all_authorities"));

            while (rs.next()) {
                Authority authority = createAuthority(rs);
                authorities.add(authority);
            }

            return authorities;
        }
    }

    @Override
    public List<Authority> getAuthoritiesFromUser(String augentId) throws SQLException {
        try (Connection conn = getConnection()) {
            List<Authority> authorities = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("authorities_from_user"));
            pstmt.setString(1, augentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Authority authority = createAuthority(rs);
                authorities.add(authority);
            }

            return authorities;
        }
    }

    @Override
    public List<User> getUsersFromAuthority(int authorityId) throws SQLException {
        try (Connection conn = getConnection()) {
            List<User> users = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("authority_get_users"));
            pstmt.setInt(1, authorityId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = DBAccountDao.createUser(rs, false);
                users.add(user);
            }

            return users;

        }
    }

    @Override
    public Authority getAuthorityByName(String name) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("authority_from_name"));
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            return createAuthority(rs);
        }
    }

    @Override
    public Authority getAuthorityByAuthorityId(int authorityId) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("authority_from_authority_id"));
            pstmt.setInt(1, authorityId);
            ResultSet rs = pstmt.executeQuery();
            return createAuthority(rs);
        }
    }

    @Override
    public void addAuthority(Authority authority) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_authority"));
            preparedAuthorityInsertOrUpdate(authority, pstmt);
            pstmt.execute();
        }
    }

    @Override
    public void updateAuthority(Authority updatedAuthority) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("update_authority"));
            preparedAuthorityInsertOrUpdate(updatedAuthority, pstmt);
            pstmt.setInt(3, updatedAuthority.getAuthorityId());
            pstmt.execute();
        }
    }

    @Override
    public void deleteAuthority(int authorityId) throws SQLException {
        try (Connection conn = getConnection()) {
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

    private void preparedAuthorityInsertOrUpdate(Authority authority, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, authority.getName());
        pstmt.setString(2, authority.getDescription());
    }

    private void deleteRolesUserAuthority(int authorityId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn
                .prepareStatement(databaseProperties.getString("delete_roles_user_authority_of_authority"));
        pstmt.setInt(1, authorityId);
        pstmt.execute();
    }

    private void deleteLocations(int authorityId, Connection conn) throws SQLException {
        //location has its own FK to delete, get all locations and use LocationDao to delete
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_locations_from_authority"));
        pstmt.setInt(1, authorityId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            locationDao.deleteLocation(rs.getString(databaseProperties.getString("location_name")));
        }
    }

    private void deleteAuthority(int authorityId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn
                .prepareStatement(databaseProperties.getString("delete_authority"));
        pstmt.setInt(1, authorityId);
        pstmt.execute();
    }

    public static Authority createAuthority(ResultSet rs) throws SQLException {
        int authorityId = rs.getInt(databaseProperties.getString("authority_authority_id"));
        String name = rs.getString(databaseProperties.getString("authority_name"));
        String description = rs.getString(databaseProperties.getString("authority_description"));
        return new Authority(authorityId, name, description);
    }
}
