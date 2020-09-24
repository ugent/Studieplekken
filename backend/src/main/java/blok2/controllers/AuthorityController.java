package blok2.controllers;

import blok2.daos.IAuthorityDao;
import blok2.model.Authority;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("api/authority")
public class AuthorityController {
    private final Logger logger = LoggerFactory.getLogger(AuthorityController.class.getSimpleName());
    private final IAuthorityDao authorityDao;

    @Autowired
    public AuthorityController(IAuthorityDao authorityDao) {
        this.authorityDao = authorityDao;
    }

    @GetMapping
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
    public Authority getAuthority(@PathVariable int authorityId) {
        try {
            return authorityDao.getAuthorityByAuthorityId(authorityId);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/{authorityId}/users")
    public List<User> getUsersFromAuthority(@PathVariable int authorityId) {
        try {
            return authorityDao.getUsersFromAuthority(authorityId);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping
    public void addAuthority(@RequestBody Authority authority) {
        try {
            authorityDao.addAuthority(authority);
            logger.info(String.format("Adding authority %s", authority.getAuthorityName()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/{authorityId}")
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

    @PostMapping("/{authorityId}/user/{userId}")
    public void addUserToAuthority(@PathVariable int authorityId, @PathVariable String userId) {
        try {
            authorityDao.addUserToAuthority(userId, authorityId);
            logger.info(String.format("Adding user %s to authority %d", userId, authorityId));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping("/{authorityId}/user/{userId}")
    public void removeUserFromAuthority(@PathVariable int authorityId, @PathVariable String userId) {
        try {
            authorityDao.removeUserFromAuthority(userId, authorityId);
            logger.info(String.format("Removing user %s from authority %d", userId, authorityId));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

}
