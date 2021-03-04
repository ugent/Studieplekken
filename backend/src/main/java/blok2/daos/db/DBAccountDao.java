package blok2.daos.db;

import blok2.daos.IAccountDao;
import blok2.helpers.Resources;
import blok2.helpers.generators.IGenerator;
import blok2.helpers.generators.VerificationCodeGenerator;
import blok2.model.users.User;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
@EnableScheduling
public class DBAccountDao extends DAO implements IAccountDao {

    private final Logger logger = Logger.getLogger(DBAccountDao.class.getSimpleName());

    private final IGenerator<String> verificationCodeGenerator = new VerificationCodeGenerator();

    @Override
    public User getUserByEmail(String email) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return getUserByEmail(email, conn);
        }
    }

    private User getUserByEmail(String email, Connection conn) throws SQLException {
        String query = Resources.databaseProperties.getString("get_user_by_<?>")
                .replace("<?>", "LOWER(u.mail) = LOWER(?)");
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, email.toLowerCase());
        ResultSet rs = pstmt.executeQuery();

        if (rs.next())
            return createUser(rs, conn);

        return null;
    }

    @Override
    public User getUserById(String augentID) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return getUserById(augentID, conn);
        }
    }

    public static User getUserById(String augentID, Connection conn) throws SQLException {
        String query = Resources.databaseProperties.getString("get_user_by_<?>")
                .replace("<?>", "u.augentid = ?");
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, augentID);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return createUser(rs, conn);
        }

        return null;
    }

    @Override
    public List<User> getUsersByLastName(String lastName) throws SQLException {
        ArrayList<User> users = new ArrayList<>();

        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_user_by_<?>")
                    .replace("<?>", "LOWER(u.augentpreferredsn) LIKE CONCAT('%', LOWER(?), '%')");
            logger.info(String.format("Gebruikte query: %s", query));
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, lastName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(createUser(rs, conn));
            }
        }

        return users;
    }

    @Override
    public List<User> getUsersByFirstName(String firstName) throws SQLException {
        ArrayList<User> users = new ArrayList<>();

        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_user_by_<?>")
                    .replace("<?>", "LOWER(u.augentpreferredgivenname) LIKE CONCAT('%', LOWER(?), '%')");
            logger.info(String.format("Gebruikte query: %s", query));
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, firstName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(createUser(rs, conn));
            }
        }

        return users;
    }

    @Override
    public List<User> getUsersByFirstAndLastName(String firstName, String lastName) throws SQLException {
        ArrayList<User> users = new ArrayList<>();

        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_user_by_<?>")
                    .replace("<?>",
                            "LOWER(u.augentpreferredgivenname) LIKE CONCAT('%', LOWER(?), '%') and LOWER(u.augentpreferredsn) LIKE CONCAT('%', LOWER(?), '%')");
            logger.info(String.format("Gebruikte query: %s", query));
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(createUser(rs, conn));
            }
        }

        return users;
    }

    @Override
    public User getUserFromBarcode(String barcode) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            // The student number and barcode match exactly.
            // For example, when scanning the barcode page, the student number is encoded as Code 128.
            User u = getUserById(barcode, conn);
            if (u != null)
                return u;

            // The barcode is a UPC-A encoded student number.
            // Example student number: 000140462060
            // Example barcode:        001404620603
            String augentid = "0" + barcode.substring(0, barcode.length() - 1);
            u = getUserById(augentid);
            if (u != null)
                return u;

            // The barcode is EAN13.
            // Example student number: 114637753611
            // Example barcode:        1146377536113
            augentid = barcode.substring(0, barcode.length() - 1);
            u = getUserById(augentid);
            if (u != null)
                return u;

            // Other?
            if (barcode.charAt(0) == '0') {
                u = getUserById(barcode.substring(1));
                if (u != null)
                    return u;
            }
        }

        return null;
    }

    @Override
    public String addUserToBeVerified(User u) throws SQLException {
        int count = 0;
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_user_to_be_verified_by_id"));
            pstmt.setString(1, u.getAugentID());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        }

        if (accountExistsByEmail(u.getMail()) || count > 0) {
            return null;
        }

        String verificationCode = verificationCodeGenerator.generate();
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_user_to_be_verified"));
            prepareInsertUserToVerify(u, verificationCode, pstmt);
            pstmt.executeUpdate();
        }

        return verificationCode;
    }

    @Override
    public boolean verifyNewUser(String verificationCode) throws SQLException {
        if (verificationCode == null || verificationCode.isEmpty())
            return false;

        try (Connection conn = adb.getConnection()) {
            // get the user in table USERS_TO_VERIFY with given verification code
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_user_to_be_verfied_by_verification_code"));
            pstmt.setString(1, verificationCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // there was an entry found in the table USERS_TO_VERIFY with the given verification code
                // the user must be added to the table USERS and removed from the table USERS_TO_VERIFY.
                User u = createUserFromUsersToVerify(rs);

                // add user to USERS
                pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_user"));
                prepareUpdateOrInsertUser(u, pstmt);
                pstmt.executeUpdate();

                // delete entry in the table USERS_TO_VERIFY
                PreparedStatement pstmtDeleteUserToVerify = conn.prepareStatement(Resources.databaseProperties.getString("delete_user_to_be_verfied"));
                pstmtDeleteUserToVerify.setString(1, verificationCode);
                pstmtDeleteUserToVerify.executeUpdate();

                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public User directlyAddUser(User u) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            addUser(u, conn);
            return u;
        }
    }

    @Override
    public void updateUserById(String augentid, User u) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            updateUserById(augentid, u, conn);
        }
    }

    private void updateUserById(String augentid, User u, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("update_user"));
        // set ...
        prepareUpdateOrInsertUser(u, pstmt);
        // where ...
        pstmt.setString(9, augentid);
        pstmt.execute();
    }

    @Override
    public void updateUserByMail(String mail, User u) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            User _u = getUserByEmail(mail, conn);
            // don't use u.getAugentID() because that may be the update!
            if (_u != null)
                updateUserById(_u.getAugentID(), u, conn);
        }
    }

    @Override
    public void deleteUser(String augentid) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_user"));
            pstmt.setString(1, augentid);
            pstmt.execute();
        }
    }

    @Override
    public boolean accountExistsByEmail(String email) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_accounts_with_email"));
            pstmt.setString(1, email.toLowerCase());
            ResultSet resultSet = pstmt.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) == 1;
        }
    }

    @Override
    public List<User> getAdmins() throws SQLException {
        ArrayList<User> users = new ArrayList<>();

        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_admins"));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(createUser(rs, conn));
            }
        }

        return users;
    }


    public static User createUser(ResultSet rs, Connection conn) throws SQLException {
        return createUser(rs, conn,true);
    }

    public static User createUser(ResultSet rs, Connection conn, boolean password) throws SQLException {
        User u = equalPartForCreatingUserOrUserToVerify(rs, password);

        if (u.getAugentID() == null)
            return null;

        u.setPenaltyPoints(rs.getInt(Resources.databaseProperties.getString("user_penalty_points")));
        u.setUserAuthorities(DBAuthorityDao.getAuthoritiesFromUser(u.getAugentID(), conn));

        PreparedStatement stmt = conn.prepareStatement(Resources.databaseProperties.getString("get_user_volunteer_locations"));
        stmt.setString(1, u.getAugentID());
        List<Integer> locationIds = new ArrayList<>();
        ResultSet set = stmt.executeQuery();
        while(set.next()) {
            locationIds.add(set.getInt("location_id"));
        }
        u.setUserVolunteer(locationIds);
        return u;
    }

    private static User createUserFromUsersToVerify(ResultSet rs) throws SQLException {
        return equalPartForCreatingUserOrUserToVerify(rs);
    }

    private static User equalPartForCreatingUserOrUserToVerify(ResultSet rs) throws SQLException {
        return equalPartForCreatingUserOrUserToVerify(rs, true);
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
        u.setAugentID(rs.getString(Resources.databaseProperties.getString("user_augentid")));
        u.setAdmin(rs.getBoolean(Resources.databaseProperties.getString("user_admin")));
        return u;
    }

    private void prepareUpdateOrInsertUser(User u, PreparedStatement pstmt) throws SQLException {
        equalPreparationForUserAndUserToVerify(u, pstmt);
        pstmt.setInt(8, u.getPenaltyPoints());
    }

    private void prepareInsertUserToVerify(User u, String verificationCode, PreparedStatement pstmt)
            throws SQLException {
        equalPreparationForUserAndUserToVerify(u, pstmt);

        pstmt.setString(8, verificationCode);

        LocalDateTime localDate = LocalDateTime.now();
        pstmt.setString(9, localDate.toString());
    }

    private void equalPreparationForUserAndUserToVerify(User u, PreparedStatement pstmt)
            throws SQLException {
        pstmt.setString(1, u.getMail().toLowerCase());
        pstmt.setString(2, u.getLastName());
        pstmt.setString(3, u.getFirstName());
        pstmt.setString(4, u.getPassword());
        pstmt.setString(5, u.getInstitution());
        pstmt.setString(6, u.getAugentID());
        pstmt.setBoolean(7, u.isAdmin());
    }

    private void addUser(User u, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_user"));
        prepareUpdateOrInsertUser(u, pstmt);
        pstmt.execute();
    }

}
