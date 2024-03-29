package blok2.http.controllers;

import blok2.database.dao.IActionLogDao;
import blok2.database.dao.IAuthorityDao;
import blok2.extensions.helpers.Base64String;
import blok2.extensions.exceptions.AlreadyExistsException;
import blok2.extensions.exceptions.NoSuchDatabaseObjectException;
import blok2.model.ActionLogEntry;
import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("authority")
public class AuthorityController {

    private final Logger logger = LoggerFactory.getLogger(AuthorityController.class.getSimpleName());

    private final IAuthorityDao authorityDao;
    private final IActionLogDao actionLogDao;

    @Autowired
    public AuthorityController(IAuthorityDao authorityDao, IActionLogDao actionLogDao) {
        this.authorityDao = authorityDao;
        this.actionLogDao = actionLogDao;
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
        return authorityDao.getAuthorityByAuthorityId(authorityId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addAuthority(@RequestBody Authority authority, @AuthenticationPrincipal User user) {
        try {
            ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.INSERTION, user, ActionLogEntry.Domain.AUTHORITY);
            actionLogDao.addLogEntry(logEntry);
            authorityDao.getAuthorityByName(authority.getAuthorityName());
            throw new AlreadyExistsException("Authority");
        } catch (NoSuchDatabaseObjectException ignore) {
            authorityDao.addAuthority(authority);
            logger.info(String.format("Adding authority %s", authority.getAuthorityName()));
        }
    }

    @PutMapping("/{authorityId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updateAuthority(@PathVariable int authorityId, @RequestBody Authority authority, @AuthenticationPrincipal User user) {
        ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.UPDATE, user, ActionLogEntry.Domain.AUTHORITY, authorityId);
        actionLogDao.addLogEntry(logEntry);
        authority.setAuthorityId(authorityId);
        authorityDao.updateAuthority(authority);
        logger.info(String.format("Updating authority %d", authorityId));
    }

    @DeleteMapping("/{authorityId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteAuthority(@PathVariable int authorityId, @AuthenticationPrincipal User user) {
        ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.DELETION, user, ActionLogEntry.Domain.AUTHORITY, authorityId);
        actionLogDao.addLogEntry(logEntry);
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
    public List<Authority> getAuthoritiesFromUser(@PathVariable("userId") String encodedId, @AuthenticationPrincipal User user) {
        String userId = Base64String.base64Decode(encodedId);

        if((user.getUserAuthorities().size() > 0 && userId.equals(user.getUserId())) || user.isAdmin()) {
            return authorityDao.getAuthoritiesFromUser(userId);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/users/{userId}/locations")
    public List<Location> getLocationsInAuthoritiesOfUser(@PathVariable("userId") String encodedId, @AuthenticationPrincipal User user) {
        String userId = Base64String.base64Decode(encodedId);

        if((user.getUserAuthorities().size() > 0 && userId.equals(user.getUserId())) || user.isAdmin()) {
            return authorityDao.getLocationsInAuthoritiesOfUser(userId);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @PostMapping("/{authorityId}/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addUserToAuthority(@PathVariable int authorityId, @PathVariable("userId") String encodedId, @AuthenticationPrincipal User user) {
        String userId = Base64String.base64Decode(encodedId);
        ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.OTHER, user, ActionLogEntry.Domain.AUTHORITY, authorityId);
        actionLogDao.addLogEntry(logEntry);
        authorityDao.addUserToAuthority(userId, authorityId);
        logger.info(String.format("Adding user %s to authority %d", userId, authorityId));
    }

    @DeleteMapping("/{authorityId}/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUserFromAuthority(@PathVariable int authorityId, @PathVariable("userId") String encodedId, @AuthenticationPrincipal User user) {
        String userId = Base64String.base64Decode(encodedId);
        ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.OTHER, user, ActionLogEntry.Domain.AUTHORITY, authorityId);
        actionLogDao.addLogEntry(logEntry);
        authorityDao.deleteUserFromAuthority(userId, authorityId);
        logger.info(String.format("Removing user %s from authority %d", userId, authorityId));
    }

}
