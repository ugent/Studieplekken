package be.ugent.blok2.daos;

import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.model.users.User;

import java.util.List;

public interface IAccountDao extends IDao {

    /**
     * NOTE: the password of a given user will be empty for security measures.
     * Only when the name of method has 'WithPassword' in it, the password will be filled in.
     */

    /**
     * Gets the user associated with the given email.
     */
    User getUserByEmail(String email);

    /**
     * Gets the user associated with the given email.
     * The password will be filled in.
     */
    User getUserByEmailWithPassword(String email);

    /**
     * Gets the user associated with the given id.
     */
    User getUserById(String augentID);

    /**
     * Gets the users with the given lastname.
     */
    List<User> getUsersByLastName(String lastName);

    /**
     * Gets the users with the given firstname.
     */
    List<User> getUsersByFirstName(String firstName);

    /**
     * Gets the users with a similar name.
     */
    List<User> getUsersByNameSoundex(String name);

    /**
     * Gets the user that have the given role.
     */
    List<String> getUserNamesByRole(String role);

    /**
     * Gets the locations a user with the given email is allowed to scan at.
     */
    List<String> getScannerLocations(String email);

    /**
     * Creates a new user that does not need to be verified.
     */
    User directlyAddUser(User u);

    /**
     * Removes the user with the given AUGentID.
     */
    void removeUserById(String AUGentID);

    /**
     * Adds a user to a list of unverified users awaiting their verification.
     */
    String addUserToBeVerified(User u); // return the generated verification code

    /**
     * Verifies the user with the give verification code.
     */
    void verifyNewUser(String verificationCode);

    /**
     * Verifies the user with the given verification code.
     */
    void updateUser(String email, User u) throws NoSuchUserException;

    /**
     * Checks if their exists a user with the given email.
     */
    boolean accountExistsByEmail(String email);

    /**
     * Adds a location to the list of locations that a user is allowed
     * to start the scan process at.
     */
    void setScannerLocation(String augent, String location);
}
