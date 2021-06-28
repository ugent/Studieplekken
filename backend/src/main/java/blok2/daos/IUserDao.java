package blok2.daos;

import blok2.model.users.User;

import java.util.List;

public interface IUserDao {

    // GETTERS

    /**
     * Gets the user associated with the given email.
     */
    User getUserByEmail(String email);

    /**
     * Gets the user associated with the given id.
     */
    User getUserById(String userId);

    /**
     * Gets the users with the given lastname.
     */
    List<User> getUsersByLastName(String lastName);

    /**
     * Gets the users with the given firstname.
     */
    List<User> getUsersByFirstName(String firstName);

    /**
     * Gets the users with the given first and last name
     */
    List<User> getUsersByFirstAndLastName(String firstName, String lastName);

    /**
     * Get a list of admins
     */
    List<User> getAdmins();

    /**
     * Try to resolve user from barcode
     */
    User getUserFromBarcode(String barcode);

    // ADDERS

    /**
     * Creates a new user that does not need to be verified.
     */
    User addUser(User user);

    // UPDATERS

    /**
     * Updates the user
     */
    void updateUser(User user);

    // DELETE

    /**
     * Removes the user with the given AUGentID.
     */
    void deleteUser(String userId);

    // OTHER

    /**
     * Checks if their exists a user with the given email.
     */
    boolean accountExistsByEmail(String email);

}
