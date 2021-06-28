package blok2.daos.db;

import blok2.daos.services.VolunteerService;
import blok2.helpers.Resources;
import blok2.model.users.User;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Service
@EnableScheduling
public class DBUserDao extends DAO {

    public static User createUser(ResultSet rs, Connection conn) throws SQLException {
        return createUser(rs, conn,true);
    }

    public static User createUser(ResultSet rs, Connection conn, boolean password) throws SQLException {
        User u = equalPartForCreatingUserOrUserToVerify(rs, password);

        if (u.getUserId() == null)
            return null;

        u.setPenaltyPoints(rs.getInt(Resources.databaseProperties.getString("user_penalty_points")));
        u.setUserAuthorities(new HashSet<>(DBAuthorityDao.getAuthoritiesFromUser(u.getUserId(), conn)));
        u.setUserVolunteer(new HashSet<>(VolunteerService.getLocationsOfVolunteer(u.getUserId(), conn)));

        return u;
    }

    private static User equalPartForCreatingUserOrUserToVerify(ResultSet rs, boolean password) throws SQLException {
        User u = new User();
        u.setMail(rs.getString(Resources.databaseProperties.getString("user_mail")));
        u.setLastName(rs.getString(Resources.databaseProperties.getString("user_surname")));
        u.setFirstName(rs.getString(Resources.databaseProperties.getString("user_name")));
        if (password) {
            u.setPassword(rs.getString(Resources.databaseProperties.getString("user_password")));
        }
        u.setInstitution(rs.getString(Resources.databaseProperties.getString("user_institution")));
        u.setUserId(rs.getString(Resources.databaseProperties.getString("user_augentid")));
        u.setAdmin(rs.getBoolean(Resources.databaseProperties.getString("user_admin")));
        return u;
    }

}
