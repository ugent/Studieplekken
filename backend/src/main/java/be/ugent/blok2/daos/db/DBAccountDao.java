package be.ugent.blok2.daos.db;

import be.ugent.blok2.controllers.BarcodeController;
import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.helpers.Institution;
import be.ugent.blok2.helpers.Resources;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.helpers.generators.IGenerator;
import be.ugent.blok2.helpers.generators.VerificationCodeGenerator;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.UserAlreadyExistsException;
import be.ugent.blok2.helpers.exceptions.WrongVerificationCodeException;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Profile("!dummy")
@Service
@EnableScheduling
public class DBAccountDao extends ADB implements IAccountDao {
    /*
    Roles will be saved in the database in one column as a csv with a semicolon as seperator. This has
    to be implemented universal through all daos that use this column.
     */
    private final IGenerator<String> verificationCodeGenerator = new VerificationCodeGenerator();

    public DBAccountDao() {
    }

    // executes daily
    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    public void scheduledCleanup() {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeQuery(databaseProperties.getString("daily_cleanup_user_to_be_verified"));
        } catch (SQLException e) {

        }
    }

    @Override
    public User getUserByEmail(String email) {
        return getUserByEmail(email, false);
    }

    @Override
    public User getUserByEmailWithPassword(String email) {
        User u = getUserByEmail(email, true);
        return u;
    }

    private User getUserByEmail(String email, boolean password) {
        try (Connection connection = getConnection()) {
            /* NOTE 1: if you want to change this query, do this in application.properties
               NOTE 2: the <?> literal string needs to be replaced by the correct condition, this is the first
                       thing done after this comment with the 'query' variable
            If you want to change the weekly percentage decrease, you must
            change the factor and amount of weeks used in this recursive query.
            Now, every week the amount is reduced with 20%, which means that points
            will remain in existence for 5 weeks.
            If you would like to change this to 10% (and an existence of 10 weeks)
            then you'll have to change "- 0.2 * (week + 1) + 1" to "- 0.1 * (week + 1) + 1"
            and "week + 1 <= 5" to "week + 1 <= 10".

            with recursive x as (
                    select 0 week, 1.0 perc
                        union all
                    select week + 1, - 0.2 * (week + 1) + 1
                    from x
                    where week + 1 <= 5
            )
            SELECT u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution, u.birthdate, u.barcode
                    , coalesce(floor(sum(case when b.event_code = 16662 then b.received_points
                                else b.received_points * x.perc end))) as "penalty_points"
            FROM public.user u
                LEFT JOIN public.penalty_book b
                    ON b.user_augentid = u.augentid
                LEFT JOIN x
                    ON floor(extract(days from (now() - to_timestamp(b.timestamp, 'YYYY-MM-DD HH24\:MI\:SS'))) / 7) = x.week
            WHERE <?>
            group by u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution, u.birthdate, u.barcode;
             */
            String query = databaseProperties.getString("get_user_by_<?>").replace("<?>", "u.mail = ?");
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email.toLowerCase());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User u = new User();
                u.setAugentID(resultSet.getString(databaseProperties.getString("user_augentid")));
                u.setLastName(resultSet.getString(databaseProperties.getString("user_surname")));
                u.setFirstName(resultSet.getString(databaseProperties.getString("user_name")));
                if (password) {
                    u.setPassword(resultSet.getString(databaseProperties.getString("user_password")));
                } else {
                    u.setPassword("");
                }

                u.setInstitution(resultSet.getString(databaseProperties.getString("user_institution")));
                u.setBarcode(resultSet.getString(databaseProperties.getString("user_barcode")));
                u.setPenaltyPoints(resultSet.getInt(databaseProperties.getString("user_penalty_points")));
                String csvRole = resultSet.getString(databaseProperties.getString("user_role"));
                u.setRoles(csvToRoles(csvRole));
                u.setMail(resultSet.getString(databaseProperties.getString("user_mail")));
                return u;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public User getUserById(String augentID) {
        try (Connection connection = getConnection()) {
            /* NOTE 1: if you want to change this query, do this in application.properties
               NOTE 2: the <?> literal string needs to be replaced by the correct condition, this is the first
                       thing done after this comment with the 'query' variable
            If you want to change the weekly percentage decrease, you must
            change the factor and amount of weeks used in this recursive query.
            Now, every week the amount is reduced with 20%, which means that points
            will remain in existence for 5 weeks.
            If you would like to change this to 10% (and an existence of 10 weeks)
            then you'll have to change "- 0.2 * (week + 1) + 1" to "- 0.1 * (week + 1) + 1"
            and "week + 1 <= 5" to "week + 1 <= 10".

            with recursive x as (
                    select 0 week, 1.0 perc
                        union all
                    select week + 1, - 0.2 * (week + 1) + 1
                    from x
                    where week + 1 <= 5
            )
            SELECT u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution, u.birthdate, u.barcode
                    , floor(sum(case when b.event_code = 16662 then b.received_points
                                else b.received_points * x.perc end)) as "penalty_points"
            FROM public.user u
                JOIN public.penalty_book b
                    ON b.user_augentid = u.augentid
                LEFT JOIN x
                    ON floor(extract(days from (now() - to_timestamp(b.timestamp, 'YYYY-MM-DD HH24\:MI\:SS'))) / 7) = x.week
            WHERE <?>
            group by u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution, u.birthdate, u.barcode;
             */
            return getUserById(augentID, connection);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static User getUserById(String augentID, Connection connection) throws SQLException {
        String query = databaseProperties.getString("get_user_by_<?>").replace("<?>", "u.augentid = ?");
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, augentID);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            User u = new User();
            u.setAugentID(resultSet.getString(databaseProperties.getString("user_augentid")));
            u.setLastName(resultSet.getString(databaseProperties.getString("user_surname")));
            u.setFirstName(resultSet.getString(databaseProperties.getString("user_name")));
            u.setPassword("");
            u.setBarcode(resultSet.getString(databaseProperties.getString("user_barcode")));
            u.setPenaltyPoints(resultSet.getInt(databaseProperties.getString("user_penalty_points")));
            String csvRole = resultSet.getString(databaseProperties.getString("user_role"));
            u.setRoles(csvToRoles(csvRole));
            u.setMail(resultSet.getString(databaseProperties.getString("user_mail")));
            u.setInstitution(resultSet.getString(databaseProperties.getString("user_institution")));
            return u;
        }

        return null;
    }


    @Override
    public List<User> getUsersByLastName(String lastName) {
        ArrayList<User> users = new ArrayList<>();
        try (Connection connection = getConnection()) {
            /* NOTE 1: if you want to change this query, do this in application.properties
               NOTE 2: the <?> literal string needs to be replaced by the correct condition, this is the first
                       thing done after this comment with the 'query' variable
            If you want to change the weekly percentage decrease, you must
            change the factor and amount of weeks used in this recursive query.
            Now, every week the amount is reduced with 20%, which means that points
            will remain in existence for 5 weeks.
            If you would like to change this to 10% (and an existence of 10 weeks)
            then you'll have to change "- 0.2 * (week + 1) + 1" to "- 0.1 * (week + 1) + 1"
            and "week + 1 <= 5" to "week + 1 <= 10".

            with recursive x as (
                    select 0 week, 1.0 perc
                        union all
                    select week + 1, - 0.2 * (week + 1) + 1
                    from x
                    where week + 1 <= 5
            )
            SELECT u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution, u.birthdate, u.barcode
                    , floor(sum(case when b.event_code = 16662 then b.received_points
                                else b.received_points * x.perc end)) as "penalty_points"
            FROM public.user u
                JOIN public.penalty_book b
                    ON b.user_augentid = u.augentid
                LEFT JOIN x
                    ON floor(extract(days from (now() - to_timestamp(b.timestamp, 'YYYY-MM-DD HH24\:MI\:SS'))) / 7) = x.week
            WHERE <?>
            group by u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution, u.birthdate, u.barcode;
             */
            String query = databaseProperties.getString("get_user_by_<?>").replace("<?>", "u.augentpreferredsn = ?");
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, lastName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User u = new User();
                u.setAugentID(resultSet.getString(databaseProperties.getString("user_augentid")));
                u.setLastName(resultSet.getString(databaseProperties.getString("user_surname")));
                u.setFirstName(resultSet.getString(databaseProperties.getString("user_name")));
                u.setPassword(resultSet.getString(databaseProperties.getString("user_password")));
                u.setBarcode(resultSet.getString(databaseProperties.getString("user_barcode")));
                u.setPenaltyPoints(resultSet.getInt(databaseProperties.getString("user_penalty_points")));
                String csvRole = resultSet.getString(databaseProperties.getString("user_role"));
                u.setRoles(csvToRoles(csvRole));
                u.setMail(resultSet.getString(databaseProperties.getString("user_mail")));
                u.setInstitution(resultSet.getString(databaseProperties.getString("user_institution")));
                users.add(u);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return users;
    }

    @Override
    public List<User> getUsersByFirstName(String firstName) {
        ArrayList<User> users = new ArrayList<>();
        try (Connection connection = getConnection()) {
            /* NOTE 1: if you want to change this query, do this in application.properties
               NOTE 2: the <?> literal string needs to be replaced by the correct condition, this is the first
                       thing done after this comment with the 'query' variable
            If you want to change the weekly percentage decrease, you must
            change the factor and amount of weeks used in this recursive query.
            Now, every week the amount is reduced with 20%, which means that points
            will remain in existence for 5 weeks.
            If you would like to change this to 10% (and an existence of 10 weeks)
            then you'll have to change "- 0.2 * (week + 1) + 1" to "- 0.1 * (week + 1) + 1"
            and "week + 1 <= 5" to "week + 1 <= 10".

            with recursive x as (
                    select 0 week, 1.0 perc
                        union all
                    select week + 1, - 0.2 * (week + 1) + 1
                    from x
                    where week + 1 <= 5
            )
            SELECT u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution, u.birthdate, u.barcode
                    , floor(sum(case when b.event_code = 16662 then b.received_points
                                else b.received_points * x.perc end)) as "penalty_points"
            FROM public.user u
                JOIN public.penalty_book b
                    ON b.user_augentid = u.augentid
                LEFT JOIN x
                    ON floor(extract(days from (now() - to_timestamp(b.timestamp, 'YYYY-MM-DD HH24\:MI\:SS'))) / 7) = x.week
            WHERE <?>
            group by u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution, u.birthdate, u.barcode;
             */
            String query = databaseProperties.getString("get_user_by_<?>").replace("<?>", "u.augentpreferredgivenname = ?");
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, firstName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User u = new User();
                u.setAugentID(resultSet.getString(databaseProperties.getString("user_augentid")));
                u.setLastName(resultSet.getString(databaseProperties.getString("user_surname")));
                u.setFirstName(resultSet.getString(databaseProperties.getString("user_name")));
                u.setPenaltyPoints(resultSet.getInt(databaseProperties.getString("user_penalty_points")));
                u.setBarcode(resultSet.getString(databaseProperties.getString("user_barcode")));
                String csvRole = resultSet.getString(databaseProperties.getString("user_role"));
                u.setRoles(csvToRoles(csvRole));
                users.add(u);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return users;
    }

    @Override
    public List<User> getUsersByNameSoundex(String name) {
        List<User> _users = new ArrayList<>();
        String s1 = soundex.encode(name.toLowerCase());

        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection()) {
            /* NOTE 1: if you want to change this query, do this in application.properties
               NOTE 2: the <?> literal string needs to be replaced by the correct condition, this is the first
                       thing done after this comment with the 'query' variable
            If you want to change the weekly percentage decrease, you must
            change the factor and amount of weeks used in this recursive query.
            Now, every week the amount is reduced with 20%, which means that points
            will remain in existence for 5 weeks.
            If you would like to change this to 10% (and an existence of 10 weeks)
            then you'll have to change "- 0.2 * (week + 1) + 1" to "- 0.1 * (week + 1) + 1"
            and "week + 1 <= 5" to "week + 1 <= 10".

            with recursive x as (
                    select 0 week, 1.0 perc
                        union all
                    select week + 1, - 0.2 * (week + 1) + 1
                    from x
                    where week + 1 <= 5
            )
            SELECT u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution, u.birthdate, u.barcode
                    , floor(sum(case when b.event_code = 16662 then b.received_points
                                else b.received_points * x.perc end)) as "penalty_points"
            FROM public.user u
                JOIN public.penalty_book b
                    ON b.user_augentid = u.augentid
                LEFT JOIN x
                    ON floor(extract(days from (now() - to_timestamp(b.timestamp, 'YYYY-MM-DD HH24\:MI\:SS'))) / 7) = x.week
            WHERE <?>
            group by u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution, u.birthdate, u.barcode;
             */
            // note: u.augentid is the PK of USER table, therefore it cannot be null and you'll have all results, as if the where clause wasn't there
            String query = databaseProperties.getString("get_user_by_<?>").replace("<?>", "u.augentid IS NOT NULL");
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User u = new User();
                u.setAugentID(resultSet.getString(databaseProperties.getString("user_augentid")));
                u.setLastName(resultSet.getString(databaseProperties.getString("user_surname")));
                u.setFirstName(resultSet.getString(databaseProperties.getString("user_name")));
                u.setPassword(resultSet.getString(databaseProperties.getString("user_password")));
                u.setBarcode(resultSet.getString(databaseProperties.getString("user_barcode")));
                u.setPenaltyPoints(resultSet.getInt(databaseProperties.getString("user_penalty_points")));
                String csvRole = resultSet.getString(databaseProperties.getString("user_role"));
                u.setRoles(csvToRoles(csvRole));
                u.setMail(resultSet.getString(databaseProperties.getString("user_mail")));
                u.setInstitution(resultSet.getString(databaseProperties.getString("user_institution")));
                users.add(u);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        for (User u : users) {
            String s2 = soundex.encode(u.getFirstName() + " " + u.getLastName());
            if (s2.equals(s1)) {
                _users.add(u);
            } else if (s1.equals(soundex.encode(u.getLastName().toLowerCase()))) {
                _users.add(u);
            } else if (s1.equals(soundex.encode(u.getFirstName().toLowerCase()))) {
                _users.add(u);
            }
        }
        return _users;
    }

    @Override
    public List<String> getUserNamesByRole(String role) {
        ArrayList<String> users = new ArrayList<>();
        try (Connection connection = getConnection()) {

            String query = databaseProperties.getString("get_user_by_<?>").replace("<?>", "u.role LIKE '%'||?||'%'");
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, role);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String s1 = resultSet.getString(databaseProperties.getString("user_augentid"));
                String s2 = resultSet.getString(databaseProperties.getString("user_name"));
                String s3 = resultSet.getString(databaseProperties.getString("user_surname"));
                String s = s1 + ' ' + s3 + ' ' + s2;
                users.add(s);
            }
            return users;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public User getUserFromBarcode(String barcode) {
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
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public User directlyAddUser(User u) {
        try (Connection conn = getConnection()) {
            /*
                INSERT into public.user (mail, augentpreferredsn, augentpreferredgivenname, password
                                     , institution, augentid, birthdate, role, penalty_points, barcode)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?)
             */
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_user"));
            pstmt.setString(1, u.getMail().toLowerCase());
            pstmt.setString(2, u.getLastName());
            pstmt.setString(3, u.getFirstName());
            if (Institution.UGent.equals(u.getInstitution())) {
                // Let hier zeer goed op: omdat dit sowieso als wachtwoord wordt gebruikt in de databank
                // voor UGent-studenten, mag een UGent gebruiker niet kunnen inloggen met ons eigen
                // registratiesysteem, want anders kan iedereen zomaar aan elkaars account (het wachtwoord
                // is voor iedereen gelijk)!
                // Merk wel op dat dat dit niet bijzonder onveilig is omdat de BlokAtUGentAuthenticationProvider
                // eerst het ingegeven wachtwoord zal encrypteren met de BCryptPasswordEncoder waardoor je
                // nooit deze string kan bekomen.
                pstmt.setString(4, "UGent-Student-Heeft-Geen-Paswoord-Wegens-Inloggen-Met-CAS");
            } else {
                pstmt.setString(4, u.getPassword());
            }
            pstmt.setString(5, u.getInstitution());
            pstmt.setString(6, u.getAugentID());
            pstmt.setString(7, rolesToCsv(u.getRoles()));
            pstmt.setInt(8, u.getPenaltyPoints());
            if (u.getBarcode() == null || u.getBarcode().length() == 0) {
                // indien geen barcode voorzien werd, deze instellen op UPC-A gegenereerd op basis van AUGent id
                String barcode = u.getAugentID();
                while(barcode.length() > 11){
                    barcode = barcode.substring(1);
                }
                barcode = BarcodeController.calculateUPCACheckSum(barcode);
                u.setBarcode(barcode);
                pstmt.setString(9, barcode);
            } else {
                pstmt.setString(9, u.getBarcode());
            }
            pstmt.executeUpdate();
            return u;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void removeUserById(String AUGentID) {
        try (Connection connection = getConnection()) {
            try {
                connection.setAutoCommit(false);
                /*
                When deleting a user, we need to delete every record that has a foreign key to this user
                 */

                /*
                Delete all location reservations of this user
                 */
                PreparedStatement st = connection.prepareStatement(databaseProperties.getString("delete_location_reservations_of_user_by_id"));
                st.setString(1, AUGentID);
                st.execute();

                /*
                Delete all locker reservations of this user
                 */
                st = connection.prepareStatement(databaseProperties.getString("delete_locker_reservations_of_user_by_id"));
                st.setString(1, AUGentID);
                st.execute();

                /*
                Delete all penalty records of this user
                 */
                st = connection.prepareStatement(databaseProperties.getString("delete_penalties_of_user_by_id"));
                st.setString(1, AUGentID);
                st.execute();

                /*
                Delete records in scanner_location table (necessary if user is a scanner)
                 */
                st = connection.prepareStatement(databaseProperties.getString("delete_scanners_of_location_of_user_by_id"));
                st.setString(1, AUGentID);
                st.execute();

                /*
                Finally, delete the user
                 */
                PreparedStatement statement = connection.prepareStatement(databaseProperties.getString("delete_user"));
                statement.setString(1, AUGentID);
                statement.execute();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String addUserToBeVerified(User u) {
        int count = 0;
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(databaseProperties.getString("count_user_to_be_verified_by_id"));
            statement.setString(1, u.getAugentID());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        if (accountExistsByEmail(u.getMail()) || count > 0) {
            throw new UserAlreadyExistsException("Exception for " + u.cloneToSendableUser());
        }
        String verificationCode = verificationCodeGenerator.generate();
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(databaseProperties.getString("insert_user_to_be_verified"));
            statement.setString(1, u.getMail().toLowerCase());
            statement.setString(2, u.getLastName());
            statement.setString(3, u.getFirstName());
            statement.setString(4, u.getPassword());
            statement.setString(5, u.getInstitution());
            statement.setString(6, u.getAugentID());

            Role[] roles = u.getRoles();
            statement.setString(7, rolesToCsv(roles));

            setString(statement, 8, u.getBarcode());
            statement.setString(9, verificationCode);

            LocalDateTime localDate = LocalDateTime.now();
            CustomDate today = new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), localDate.getHour(), localDate.getMinute(), localDate.getSecond());
            statement.setString(10, today.toString());

            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return verificationCode;
    }

    public void setString(PreparedStatement statement, int nr, String value) throws SQLException {
        if (value != null) {
            statement.setString(nr, value);
        } else {
            statement.setNull(nr, Types.NULL);
        }
    }

    @Override
    public void verifyNewUser(String verificationCode) {
        try (Connection connection = getConnection()) {
            // get the user in table USERS_TO_VERIFY with given verification code
            PreparedStatement pstmtUserToVerify = connection.prepareStatement(databaseProperties.getString("get_user_to_be_verfied_by_verification_code"));
            pstmtUserToVerify.setString(1, verificationCode);
            ResultSet resultSet = pstmtUserToVerify.executeQuery();

            if (resultSet.next()) {
                // there was an entry found in the table USERS_TO_VERIFY with the given verification code
                // the user must be added to the table USERS and removed from the table USERS_TO_VERIFY.

                // add user to USERS
                PreparedStatement pstmtUser = connection.prepareStatement(databaseProperties.getString("insert_user"));
                pstmtUser.setString(1, resultSet.getString(databaseProperties.getString("utv_mail")));
                pstmtUser.setString(2, resultSet.getString(databaseProperties.getString("utv_surname")));
                pstmtUser.setString(3, resultSet.getString(databaseProperties.getString("utv_name")));
                pstmtUser.setString(4, resultSet.getString(databaseProperties.getString("utv_password")));
                pstmtUser.setString(5, resultSet.getString(databaseProperties.getString("utv_institution")));
                pstmtUser.setString(6, resultSet.getString(databaseProperties.getString("utv_augentid")));
                pstmtUser.setString(7, resultSet.getString(databaseProperties.getString("utv_role")));
                pstmtUser.setInt(8, 0); // penalty_points
                pstmtUser.setString(9, resultSet.getString(databaseProperties.getString("utv_barcode")));
                pstmtUser.executeUpdate();

                // delete entry in the table USERS_TO_VERIFY
                PreparedStatement pstmtDeleteUserToVerify = connection.prepareStatement(databaseProperties.getString("delete_user_to_be_verfied"));
                pstmtDeleteUserToVerify.setString(1, verificationCode);
                pstmtDeleteUserToVerify.executeUpdate();
            } else {
                throw new WrongVerificationCodeException("No new user to be verified with verification code "
                        + verificationCode + " was found.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateUser(String email, User u) throws NoSuchUserException, UserAlreadyExistsException {
        try (Connection connection = getConnection()) {
            try {
                connection.setAutoCommit(false);

                /*
                First check if the AUGentID of the user has changed. We do this by fetching the user by email and checking if
                the AUGentID of this user equals the AUGentID of the given user.

                This is necessary because the AUGentID of a user is used as Primary Key in the database. When changing the AUGentID,
                all records including this id as a Foreign Key need to be updated too.
                */
                User currentUser = getUserByEmail(email, true);
                if (currentUser == null) {
                    throw new NoSuchUserException("No user with mail = " + email);
                }

                if (!currentUser.getAugentID().equals(u.getAugentID())) {
                    /*
                    First, add a new user with the new AUGentID, note that we keep the (same) user with the old
                    AUGentID in the database for now.

                    The old user needs to be kept in the database until all records containing a foreign key of this user
                    are updated. Because of the unique constraint on the email of a user, the new user is inserted with a temporary email.
                    After deleting the old record of the user, the email can be replaced.
                     */
                    PreparedStatement st = connection.prepareStatement(databaseProperties.getString("insert_user"));
                    st.setString(1, "no_mail");
                    st.setString(2, u.getLastName());
                    st.setString(3, u.getFirstName());
                    st.setString(4, currentUser.getPassword());
                    st.setString(5, u.getInstitution());
                    st.setString(6, u.getAugentID());
                    st.setString(7, rolesToCsv(u.getRoles()));
                    st.setInt(8, u.getPenaltyPoints());
                    st.setString(9, u.getBarcode());
                    st.execute();

                    /*
                    Update location reservations of this user
                     */
                    st = connection.prepareStatement(databaseProperties.getString("update_location_reservations_of_user"));
                    st.setString(1, u.getAugentID());
                    st.setString(2, currentUser.getAugentID());
                    st.execute();

                    /*
                    Update locker reservations of this user
                     */
                    st = connection.prepareStatement(databaseProperties.getString("update_locker_reservations_of_user"));
                    st.setString(1, u.getAugentID());
                    st.setString(2, currentUser.getAugentID());
                    st.execute();

                    /*
                    Update penalties of this user
                     */
                    st = connection.prepareStatement(databaseProperties.getString("update_penalties_of_user"));
                    st.setString(1, u.getAugentID());
                    st.setString(2, currentUser.getAugentID());
                    st.execute();

                    /*
                    Update scanners of location of this user
                     */
                    st = connection.prepareStatement(databaseProperties.getString("update_scanners_of_location_of_user"));
                    st.setString(1, u.getAugentID());
                    st.setString(2, currentUser.getAugentID());
                    st.execute();

                    /*
                    After all records using a Foreing Key to this user are changed, the (same) user with the old AUGentID record
                    can be removed.
                     */
                    st = connection.prepareStatement(databaseProperties.getString("delete_user"));
                    st.setString(1, currentUser.getAugentID());
                    st.execute();

                    /*
                    At last, change the email of the new user record to the correct email
                     */
                    st = connection.prepareStatement(databaseProperties.getString("set_mail_of_user_by_id"));
                    st.setString(1, u.getMail());
                    st.setString(2, u.getAugentID());
                    st.execute();
                } else {
                    PreparedStatement pstmt = connection.prepareStatement(databaseProperties.getString("update_user"));
                    pstmt.setString(1, u.getMail());
                    pstmt.setString(2, u.getLastName());
                    pstmt.setString(3, u.getFirstName());
                    pstmt.setString(4, u.getPassword());
                    pstmt.setString(5, u.getInstitution());
                    pstmt.setString(6, u.getAugentID());
                    pstmt.setString(7, rolesToCsv(u.getRoles()));
                    pstmt.setInt(8, u.getPenaltyPoints());
                    pstmt.setString(9, u.getBarcode());
                    pstmt.setString(10, u.getAugentID());
                    pstmt.execute();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                if(e.getSQLState().equals("23505")){
                    throw new UserAlreadyExistsException("User with augentid " + u.getAugentID() + " already exists.");
                }
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean accountExistsByEmail(String email) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(databaseProperties.getString("count_accounts_with_email"));
            statement.setString(1, email.toLowerCase());
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) >= 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public List<String> getScannerLocations(String email) {
        //get the user to know its ID
        User u = getUserByEmail(email.toLowerCase(), false);

        ArrayList<String> locations = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String query = databaseProperties.getString("get_locations_of_scanner");
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, u.getAugentID());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                locations.add(resultSet.getString(databaseProperties.getString("scanners_location_name")));
            }
            return locations;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void setScannerLocation(String AugentID, String nameLocation) {

    }

    public static User createUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setMail(rs.getString(databaseProperties.getString("user_mail")));
        u.setLastName(rs.getString(databaseProperties.getString("user_surname")));
        u.setFirstName(rs.getString(databaseProperties.getString("user_name")));
        u.setPassword(rs.getString(databaseProperties.getString("user_password")));
        u.setInstitution(rs.getString(databaseProperties.getString("user_institution")));
        u.setAugentID(rs.getString(databaseProperties.getString("user_augentid")));
        u.setRoles(csvToRoles(rs.getString(databaseProperties.getString("user_role"))));
        u.setPenaltyPoints(rs.getInt(databaseProperties.getString("user_penalty_points")));
        u.setBarcode(rs.getString(databaseProperties.getString("user_barcode")));
        return u;
    }

    private static String rolesToCsv(Role[] roles) {
        String csv = "";
        for (int i = 0; i < roles.length - 1; i++) {
            csv += roles[i].toString();
            csv += ";";
        }
        csv += roles[roles.length - 1].toString();
        return csv;
    }

    private static Role[] csvToRoles(String csvRoles) {
        String[] split = csvRoles.split(";");
        Role[] roles = new Role[split.length];
        for (int i = 0; i < roles.length; i++) {
            roles[i] = Role.valueOf(split[i]);
        }
        return roles;
    }

}
