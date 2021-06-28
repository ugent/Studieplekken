package blok2.controllers;

import blok2.daos.IAuthorityDao;
import blok2.helpers.exceptions.AlreadyExistsException;
import blok2.helpers.exceptions.NoSuchAuthorityException;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("authority")
public class AuthorityController {

    private final Logger logger = LoggerFactory.getLogger(AuthorityController.class.getSimpleName());

    private final IAuthorityDao authorityDao;

    @Autowired
    public AuthorityController(IAuthorityDao authorityDao) {
        this.authorityDao = authorityDao;
    }

    // *************************************
    // *   CRUD operations for AUTHORITY   *
    // *************************************/

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Authority> getAllAuthorities() {
        return authorityDao.getAllAuthorities();
    }

    @GetMapping("/{authorityId}")
    @PreAuthorize("permitAll()")
    public Authority getAuthority(@PathVariable int authorityId) {
        Authority authority = authorityDao.getAuthorityByAuthorityId(authorityId);
        if (authority == null)
            throw new NoSuchAuthorityException("Authority not found");
        return authority;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addAuthority(@RequestBody Authority authority) {
        try {
            authorityDao.getAuthorityByName(authority.getAuthorityName());
            throw new AlreadyExistsException("Authority");
        } catch (NoSuchDatabaseObjectException ignore) {
            authorityDao.addAuthority(authority);
            logger.info(String.format("Adding authority %s", authority.getAuthorityName()));
        }
    }

    @PutMapping("/{authorityId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updateAuthority(@PathVariable int authorityId, @RequestBody Authority authority) {
        authority.setAuthorityId(authorityId);
        authorityDao.updateAuthority(authority);
        logger.info(String.format("Updating authority %d", authorityId));
    }

    @DeleteMapping("/{authorityId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteAuthority(@PathVariable int authorityId) {
        authorityDao.deleteAuthority(authorityId);
        logger.info(String.format("Removing authority %d", authorityId));
    }

    // ************************************************
    // *   CRUD operations for ROLES_USER_AUTHORITY   *
    // ************************************************/

    @GetMapping("/{authorityId}/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> getUsersFromAuthority(@PathVariable int authorityId) {
        return authorityDao.getUsersFromAuthority(authorityId);
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("(hasAuthority('HAS_AUTHORITIES') and #userId == authentication.principal.userId) or hasAuthority('ADMIN')")
    public List<Authority> getAuthoritiesFromUser(@PathVariable("userId") String userId) {
        return authorityDao.getAuthoritiesFromUser(userId);
    }

    @GetMapping("/users/{userId}/locations")
    @PreAuthorize("(hasAuthority('HAS_AUTHORITIES') and #userId == authentication.principal.userId) or hasAuthority('ADMIN')")
    public List<Location> getLocationsInAuthoritiesOfUser(@PathVariable("userId") String userId) {
        return authorityDao.getLocationsInAuthoritiesOfUser(userId);
    }

    @PostMapping("/{authorityId}/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addUserToAuthority(@PathVariable int authorityId, @PathVariable String userId) {
        authorityDao.addUserToAuthority(userId, authorityId);
        logger.info(String.format("Adding user %s to authority %d", userId, authorityId));
    }

    @DeleteMapping("/{authorityId}/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUserFromAuthority(@PathVariable int authorityId, @PathVariable String userId) {
        authorityDao.deleteUserFromAuthority(userId, authorityId);
        logger.info(String.format("Removing user %s from authority %d", userId, authorityId));
    }

}
