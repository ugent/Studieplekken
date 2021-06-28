package blok2.daos;

import blok2.model.reservables.Location;
import blok2.model.users.User;

import java.sql.SQLException;
import java.util.List;

public interface IUserDao extends IDao {

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
     * Gets the users with the given first and last name
     */
    List<User> getUsersByFirstAndLastName(String firstName, String lastName) throws SQLException;

    /**
     * Try to resolve user from barcode
     */
    User getUserFromBarcode(String barcode) throws SQLException;

    /**
     * Get all locations for which the specified user has volunteered
     */
    List<Location> getVolunteeredLocations(String userId) throws SQLException;

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

    // UPDATERS

    /**
     * Updates the user by id
     */
    void updateUserById(String augentid, User u) throws SQLException;

    /**
     * Updates the user by mail
     */
    void updateUserByMail(String email, User u) throws SQLException;

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

    List<User> getAdmins() throws SQLException;
}
