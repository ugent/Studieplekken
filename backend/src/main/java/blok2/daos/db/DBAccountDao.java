package blok2.daos.db;

import blok2.daos.IAccountDao;
import blok2.helpers.date.CustomDate;
import blok2.helpers.generators.IGenerator;
import blok2.helpers.generators.VerificationCodeGenerator;
import blok2.model.users.Role;
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

@Service
@EnableScheduling
public class DBAccountDao extends ADB implements IAccountDao {

    private final IGenerator<String> verificationCodeGenerator = new VerificationCodeGenerator();

    public DBAccountDao() {
    }

    // executes daily
    // TODO: should be done in db, have a look at https://stackoverflow.com/a/9490521/9356123
    //  to track the timestamp of the locker_reservation record insertions. Based on xmin,
    //  delete certain entries. Probably best to do it as a Perl script which can be put into
    //  a one shot systemd service on the production server.
    /*@Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    public void scheduledCleanup() {
        try (Connection conn = getConnection()) {
            Statement statement = conn.createStatement();
            statement.executeQuery(databaseProperties.getString("daily_cleanup_user_to_be_verified"));
        } catch (SQLException e) {

        }
    }*/

    @Override
    public User getUserByEmail(String email) throws SQLException {
        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_user_by_<?>")
                    .replace("<?>", "LOWER(u.mail) = LOWER(?)");
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, email.toLowerCase());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
                return createUser(rs);

            return null;
        }
    }

    @Override
    public User getUserById(String augentID) throws SQLException {
        try (Connection conn = getConnection()) {
            return getUserById(augentID, conn);
        }
    }

    public static User getUserById(String augentID, Connection conn) throws SQLException {
        String query = databaseProperties.getString("get_user_by_<?>")
                .replace("<?>", "u.augentid = ?");
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, augentID);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return createUser(rs);
        }

        return null;
    }

    @Override
    public List<User> getUsersByLastName(String lastName) throws SQLException {
        ArrayList<User> users = new ArrayList<>();

        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_user_by_<?>")
                    .replace("<?>", "LOWER(u.augentpreferredsn) = LOWER(?)");
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, lastName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(createUser(rs));
            }
        }

        return users;
    }

    @Override
    public List<User> getUsersByFirstName(String firstName) throws SQLException {
        ArrayList<User> users = new ArrayList<>();

        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_user_by_<?>")
                    .replace("<?>", "LOWER(u.augentpreferredgivenname) = LOWER(?)");
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, firstName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(createUser(rs));
            }
        }

        return users;
    }

    @Override
    public List<User> getUsersByFirstAndLastName(String firstName, String lastName) throws SQLException {
        ArrayList<User> users = new ArrayList<>();

        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_user_by_<?>")
                    .replace("<?>",
                            "LOWER(u.augentpreferredgivenname) = LOWER(?) and LOWER(u.augentpreferredsn) = LOWER(?)");
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(createUser(rs));
            }
        }

        return users;
    }

    @Override
    public List<String> getUserNamesByRole(String role) throws SQLException {
        ArrayList<String> users = new ArrayList<>();

        try (Connection conn = getConnection()) {

            String query = databaseProperties.getString("get_user_by_<?>").replace("<?>", "u.role LIKE '%'||?||'%'");
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, role);
            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                String s1 = resultSet.getString(databaseProperties.getString("user_augentid"));
                String s2 = resultSet.getString(databaseProperties.getString("user_name"));
                String s3 = resultSet.getString(databaseProperties.getString("user_surname"));
                String s = s1 + ' ' + s3 + ' ' + s2;
                users.add(s);
            }

            return users;
        }
    }

    @Override
    public User getUserFromBarcode(String barcode) throws SQLException {
        try (Connection conn = getConnection()) {
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
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("count_user_to_be_verified_by_id"));
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
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_user_to_be_verified"));
            prepareInsertUserToVerify(u, verificationCode, pstmt);
            pstmt.executeUpdate();
        }

        return verificationCode;
    }

    @Override
    public boolean verifyNewUser(String verificationCode) throws SQLException {
        if (verificationCode == null || verificationCode.isEmpty())
            return false;

        try (Connection conn = getConnection()) {
            // get the user in table USERS_TO_VERIFY with given verification code
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_user_to_be_verfied_by_verification_code"));
            pstmt.setString(1, verificationCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // there was an entry found in the table USERS_TO_VERIFY with the given verification code
                // the user must be added to the table USERS and removed from the table USERS_TO_VERIFY.
                User u = createUserFromUsersToVerify(rs);

                // add user to USERS
                pstmt = conn.prepareStatement(databaseProperties.getString("insert_user"));
                prepareUpdateOrInsertUser(u, pstmt);
                pstmt.executeUpdate();

                // delete entry in the table USERS_TO_VERIFY
                PreparedStatement pstmtDeleteUserToVerify = conn.prepareStatement(databaseProperties.getString("delete_user_to_be_verfied"));
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
        try (Connection conn = getConnection()) {
            addUser(u, conn);
            return u;
        }
    }

    @Override
    public boolean updateUserById(String augentid, User u) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                if (!augentid.equals(u.getAugentID())) {
                    // First, add the new user so that the tables
                    // with a FK to the old user can be updated
                    addUser(u, conn);

                    // Now, update the tables with a FK pointing to
                    // the old user
                    updateForeignKeysToNewUser(augentid, u.getAugentID(), conn);

                    // Finally, remove the old user
                    deleteUser(augentid, conn);
                } else {
                    updateUserById(u, conn);
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public boolean updateUserByMail(String mail, User u) throws SQLException {
        User _u = getUserByEmail(mail);
        // don't use u.getAugentID() because that may be the update!
        return updateUserById(_u.getAugentID(), u);
    }

    @Override
    public void deleteUser(String augentid) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                // delete all entries of user in SCANNERS_LOCATION
                DBScannerLocationDao.deleteAllLocationsOfScanner(augentid, conn);

                // delete all entries of user in LOCATION_RESERVATIONS
                deleteLocationReservations(augentid, conn);

                // delete all entries of user in LOCKER_RESERVATIONS
                deleteLockerReservations(augentid, conn);

                // delete all entries of user in PENALTY_BOOK
                deletePenaltyBookEntries(augentid, conn);

                // delete all entries of user in ROLES_USER_AUTHORITY
                deleteRolesUserAuthorityEntries(augentid, conn);

                // and eventually, delete the user in USERS
                deleteUser(augentid, conn);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public boolean accountExistsByEmail(String email) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("count_accounts_with_email"));
            pstmt.setString(1, email.toLowerCase());
            ResultSet resultSet = pstmt.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) == 1;
        }
    }


    public static User createUser(ResultSet rs) throws SQLException {
        return createUser(rs, true);
    }

    public static User createUser(ResultSet rs, boolean password) throws SQLException {
        User u = equalPartForCreatingUserOrUserToVerify(rs, password);

        if (u.getAugentID() == null)
            return null;

        u.setPenaltyPoints(rs.getInt(databaseProperties.getString("user_penalty_points")));
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
        u.setMail(rs.getString(databaseProperties.getString("user_mail")));
        u.setLastName(rs.getString(databaseProperties.getString("user_surname")));
        u.setFirstName(rs.getString(databaseProperties.getString("user_name")));
        if (password) {
            u.setPassword(rs.getString(databaseProperties.getString("user_password")));
        }
        u.setInstitution(rs.getString(databaseProperties.getString("user_institution")));
        u.setAugentID(rs.getString(databaseProperties.getString("user_augentid")));
        u.setRoles(csvToRoles(rs.getString(databaseProperties.getString("user_role"))));
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
        CustomDate today = new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), localDate.getHour(), localDate.getMinute(), localDate.getSecond());
        pstmt.setString(9, today.toString());
    }

    private void equalPreparationForUserAndUserToVerify(User u, PreparedStatement pstmt)
            throws SQLException {
        pstmt.setString(1, u.getMail().toLowerCase());
        pstmt.setString(2, u.getLastName());
        pstmt.setString(3, u.getFirstName());
        pstmt.setString(4, u.getPassword());
        pstmt.setString(5, u.getInstitution());
        pstmt.setString(6, u.getAugentID());

        Role[] roles = u.getRoles();
        pstmt.setString(7, rolesToCsv(roles));
    }

    private static String rolesToCsv(Role[] roles) {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < roles.length - 1; i++) {
            csv.append(roles[i].toString());
            csv.append(";");
        }
        csv.append(roles[roles.length - 1].toString());
        return csv.toString();
    }

    private static Role[] csvToRoles(String csvRoles) {
        if (csvRoles == null)
            return null;

        String[] split = csvRoles.split(";");
        Role[] roles = new Role[split.length];
        for (int i = 0; i < roles.length; i++) {
            roles[i] = Role.valueOf(split[i]);
        }
        return roles;
    }

    private void addUser(User u, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_user"));
        prepareUpdateOrInsertUser(u, pstmt);
        pstmt.execute();
    }

    private void updateForeignKeysToNewUser(String oldAugentid, String newAugentid, Connection conn)
            throws SQLException {
        updateForeignKeyOfScannersLocation(oldAugentid, newAugentid, conn);
        updateForeignKeyOfLocationReservations(oldAugentid, newAugentid, conn);
        updateForeignKeyOfLockerReservations(oldAugentid, newAugentid, conn);
        updateForeignKeyOfPenaltyBook(oldAugentid, newAugentid, conn);
        updateForeignKeyOfRolesUserAuthorityEntries(oldAugentid, newAugentid, conn);
    }

    private void updateForeignKeyOfScannersLocation(String oldAugentid, String newAugentid, Connection conn)
            throws SQLException {
        String query = databaseProperties.getString("update_fk_scanners_location_to_user");
        updateForeignKeyGeneral(oldAugentid, newAugentid, query, conn);
    }

    private void updateForeignKeyOfLocationReservations(String oldAugentid, String newAugentid, Connection conn)
            throws SQLException {
        String query = databaseProperties.getString("update_fk_location_reservations_to_user");
        updateForeignKeyGeneral(oldAugentid, newAugentid, query, conn);
    }

    private void updateForeignKeyOfLockerReservations(String oldAugentid, String newAugentid, Connection conn)
            throws SQLException {
        String query = databaseProperties.getString("update_fk_locker_reservations_to_user");
        updateForeignKeyGeneral(oldAugentid, newAugentid, query, conn);
    }

    private void updateForeignKeyOfPenaltyBook(String oldAugentid, String newAugentid, Connection conn)
            throws SQLException {
        String query = databaseProperties.getString("update_fk_penalty_book_to_user");
        updateForeignKeyGeneral(oldAugentid, newAugentid, query, conn);
    }

    private void updateForeignKeyOfRolesUserAuthorityEntries(String oldAugentid, String newAugentid, Connection conn) throws SQLException {
        String query = databaseProperties.getString("update_fk_roles_user_authority_to_user");
        updateForeignKeyGeneral(oldAugentid, newAugentid, query, conn);
    }

    private void updateForeignKeyGeneral(String oldAugentid, String newAugentid, String query, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, newAugentid);
        pstmt.setString(2, oldAugentid);
        pstmt.execute();
    }

    private void updateUserById(User user, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("update_user"));
        // set ...
        prepareUpdateOrInsertUser(user, pstmt);
        // where ...
        pstmt.setString(9, user.getAugentID());
        pstmt.execute();
    }

    private void deleteLocationReservations(String augentid, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties
                .getString("delete_location_reservations_of_user"));
        pstmt.setString(1, augentid);
        pstmt.execute();
    }

    private void deleteLockerReservations(String augentid, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties
                .getString("delete_locker_reservations_of_user"));
        pstmt.setString(1, augentid);
        pstmt.execute();
    }

    private void deletePenaltyBookEntries(String augentid, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties
                .getString("delete_penalties_of_user"));
        pstmt.setString(1, augentid);
        pstmt.execute();
    }

    private void deleteRolesUserAuthorityEntries(String augentid, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_roles_user_authority_of_user"));
        pstmt.setString(1, augentid);
        pstmt.execute();
    }

    private void deleteUser(String augentid, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_user"));
        pstmt.setString(1, augentid);
        pstmt.execute();
    }

}
