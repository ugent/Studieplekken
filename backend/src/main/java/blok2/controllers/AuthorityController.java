package blok2.controllers;

import blok2.daos.IAccountDao;
import blok2.daos.IAuthorityDao;
import blok2.helpers.exceptions.AlreadyExistsException;
import blok2.helpers.exceptions.NoSuchAuthorityException;
import blok2.helpers.exceptions.NoSuchUserException;
import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("authority")
public class AuthorityController {
    private final Logger logger = LoggerFactory.getLogger(AuthorityController.class.getSimpleName());
    private final IAuthorityDao authorityDao;
    private final IAccountDao accountDao;

    @Autowired
    public AuthorityController(IAuthorityDao authorityDao, IAccountDao accountDao) {
        this.authorityDao = authorityDao;
        this.accountDao = accountDao;
    }

    // *************************************
    // *   CRUD operations for AUTHORITY   *
    // *************************************/

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Authority> getAllAuthorities() {
        try {
            return authorityDao.getAllAuthorities();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/{authorityId}")
    @PreAuthorize("permitAll()")
    public Authority getAuthority(@PathVariable int authorityId) {
        try {
            Authority authority = authorityDao.getAuthorityByAuthorityId(authorityId);
            if(authority == null)
                throw new NoSuchAuthorityException("Authority not found");
            return authority;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addAuthority(@RequestBody Authority authority) {
        try {
            if(authorityDao.getAuthorityByName(authority.getAuthorityName()) != null) {
                throw new AlreadyExistsException("Authority");
            }
            authorityDao.addAuthority(authority);
            logger.info(String.format("Adding authority %s", authority.getAuthorityName()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/{authorityId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updateAuthority(@PathVariable int authorityId, @RequestBody Authority authority) {
        try {
            authority.setAuthorityId(authorityId);
            authorityDao.updateAuthority(authority);
            logger.info(String.format("Updating authority %d", authorityId));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping("/{authorityId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteAuthority(@PathVariable int authorityId) {
        try {
            authorityDao.deleteAuthority(authorityId);
            logger.info(String.format("Removing authority %d", authorityId));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    // ************************************************
    // *   CRUD operations for ROLES_USER_AUTHORITY   *
    // ************************************************/

    @GetMapping("/{authorityId}/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> getUsersFromAuthority(@PathVariable int authorityId) {
        try {
            if(authorityDao.getAuthorityByAuthorityId(authorityId) == null) {
                throw new NoSuchAuthorityException("Authority not found");
            }

            return authorityDao.getUsersFromAuthority(authorityId);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("(hasAuthority('HAS_AUTHORITIES') and #userId == authentication.principal.augentID) or hasAuthority('ADMIN')")
    public List<Authority> getAuthoritiesFromUser(@PathVariable("userId") String userId) {
        try {
            if(accountDao.getUserById(userId) == null) {
                throw new NoSuchUserException("User not found");
            }

            return authorityDao.getAuthoritiesFromUser(userId);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/users/{userId}/locations")
    @PreAuthorize("(hasAuthority('HAS_AUTHORITIES') and #userId == authentication.principal.augentID) or hasAuthority('ADMIN')")
    public List<Location> getLocationsInAuthoritiesOfUser(@PathVariable("userId") String userId) {
        try {
            return authorityDao.getLocationsInAuthoritiesOfUser(userId);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping("/{authorityId}/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addUserToAuthority(@PathVariable int authorityId, @PathVariable String userId) {
        try {
            if(accountDao.getUserById(userId) == null) {
                throw new NoSuchUserException("User not found");
            }
            if(authorityDao.getAuthorityByAuthorityId(authorityId) == null) {
                throw new NoSuchAuthorityException("Authority not found");
            }

            authorityDao.addUserToAuthority(userId, authorityId);
            logger.info(String.format("Adding user %s to authority %d", userId, authorityId));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping("/{authorityId}/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUserFromAuthority(@PathVariable int authorityId, @PathVariable String userId) {
        try {
            authorityDao.deleteUserFromAuthority(userId, authorityId);
            logger.info(String.format("Removing user %s from authority %d", userId, authorityId));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

}
