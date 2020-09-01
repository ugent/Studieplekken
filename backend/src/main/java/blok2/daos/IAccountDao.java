package blok2.daos;

import blok2.model.users.User;

import java.sql.SQLException;
import java.util.List;

public interface IAccountDao extends IDao {

    // GETTERS

    /**
     * Gets the user associated with the given email.
     */
    User getUserByEmail(String email) throws SQLException;

    /**
     * Gets the user associated with the given id.
     */
    User getUserById(String augentID) throws SQLException;

    /**
     * Gets the users with the given lastname.
     */
    List<User> getUsersByLastName(String lastName) throws SQLException;

    /**
     * Gets the users with the given firstname.
     */
    List<User> getUsersByFirstName(String firstName) throws SQLException;

    /**
     * Gets the user that have the given role.
     */
    List<String> getUserNamesByRole(String role) throws SQLException;

    /**
     * Try to resolve user from barcode
     */
    User getUserFromBarcode(String barcode) throws SQLException;

    // ADDERS

    /**
     * Adds a user to a list of unverified users awaiting their verification.
     */
    String addUserToBeVerified(User u) throws SQLException; // return the generated verification code

    /**
     * Verifies the user with the give verification code.
     */
    boolean verifyNewUser(String verificationCode) throws SQLException;

    /**
     * Creates a new user that does not need to be verified.
     */
    User directlyAddUser(User u) throws SQLException;

    /**
     * Adds a user as a member of an authority. Returns if successful todo can only be done by a member of that authority
     */
    boolean addUserToAuthority(String augentid, int authorityId) throws SQLException;


    // UPDATERS

    /**
     * Updates the user by id
     */
    boolean updateUserById(String augentid, User u) throws SQLException;

    /**
     * Updates the user by mail
     */
    boolean updateUserByMail(String email, User u) throws SQLException;

    // DELETE

    /**
     * Removes the user with the given AUGentID.
     */
    void deleteUser(String AUGentID) throws SQLException;

    // OTHER

    /**
     * Checks if their exists a user with the given email.
     */
    boolean accountExistsByEmail(String email) throws SQLException;
}
