package be.ugent.blok2.daos;

import be.ugent.blok2.model.users.User;

import java.sql.SQLException;
import java.util.List;

public interface IAccountDao extends IDao {

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
     * Gets the users with a similar name.
     */
    List<User> getUsersByNameSoundex(String name) throws SQLException;

    /**
     * Gets the user that have the given role.
     */
    List<String> getUserNamesByRole(String role) throws SQLException;

    /**
     * Gets the locations a user with the given email is allowed to scan at.
     */
    List<String> getScannerLocations(String email) throws SQLException;

    /**
     * Try to resolve user from barcode
     */
    User getUserFromBarcode(String barcode) throws SQLException;

    /**
     * Creates a new user that does not need to be verified.
     */
    User directlyAddUser(User u) throws SQLException;

    /**
     * Removes the user with the given AUGentID.
     */
    void removeUserById(String AUGentID) throws SQLException;

    /**
     * Adds a user to a list of unverified users awaiting their verification.
     */
    String addUserToBeVerified(User u) throws SQLException; // return the generated verification code

    /**
     * Verifies the user with the give verification code.
     */
    boolean verifyNewUser(String verificationCode) throws SQLException;

    /**
     * Verifies the user with the given verification code.
     */
    boolean updateUser(String email, User u) throws SQLException;

    /**
     * Checks if their exists a user with the given email.
     */
    boolean accountExistsByEmail(String email) throws SQLException;

    /**
     * Adds a location to the list of locations that a user is allowed
     * to start the scan process at.
     */
    void setScannerLocation(String augent, String location) throws SQLException;
}
