package blok2.database.dao;

import blok2.model.Authority;
import blok2.model.location.Location;
import blok2.model.users.User;

import java.util.List;

public interface IAuthorityDao {

    // *************************************
    // *   CRUD operations for AUTHORITY   *
    // *************************************/

    /**
     * get all authorities
     */
    List<Authority> getAllAuthorities();

    /**
     * get authority with the given name.
     */
    Authority getAuthorityByName(String name);

    /**
     * get authority by its id.
     */
    Authority getAuthorityByAuthorityId(int authorityId);

    /**
     * get locations in authority
     */
    List<Location> getLocationsInAuthority(int authorityId);

    /**
     * Add an authority to the database. AuthorityId is ignored.
     *
     * @return the added authority with updated authorityId
     */
    Authority addAuthority(Authority authority);

    /**
     * Updates the authority given by the authorityId
     *
     * @param authority Authority with new values, with Authority.authorityId the authority to update
     */
    void updateAuthority(Authority authority);

    /**
     * delete authority by its id, including user-authority relation and its locations
     */
    void deleteAuthority(int authorityId);


    // ************************************************
    // *   CRUD operations for ROLES_USER_AUTHORITY   *
    // ************************************************/

    /**
     * get a list of Authorities the user is a member of. Can be empty.
     */
    List<Authority> getAuthoritiesFromUser(String userId);

    /**
     * get a list of Authorities the user is a member of. Can be empty.
     * The user should belong to the given institution.
     */
    List<Authority> getAuthoritiesFromUserAndInstitution(String userId, String institution);

    /**
     * get a list of Locations that the user can manage
     */
    List<Location> getLocationsInAuthoritiesOfUser(String userId);

    /**
     * get a list of Locations that the user can manage
     */
    List<Location> getLocationsInAuthoritiesOfUserAndInstitution(String userId, String institution);

    /**
     * get list of users that are a member of the given authority.
     */
    List<User> getUsersFromAuthority(int authorityId);

    /**
     * Adds a user as a member of an authority. Returns if successful
     */
    void addUserToAuthority(String userId, int authorityId);

    /**
     * remove a user from the given authority
     */
    void deleteUserFromAuthority(String userId, int authorityId);
}
