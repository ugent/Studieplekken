package blok2.controllers;

import blok2.daos.IAuthorityDao;
import blok2.model.Authority;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("api/authority")
public class AuthorityController {
    private final Logger logger = Logger.getLogger(AuthorityController.class.getSimpleName());
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
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/{authorityId}")
    public Authority getAuthority(@PathVariable int authorityId) {
        try {
            return authorityDao.getAuthorityByAuthorityId(authorityId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/{authorityId}/users")
    public List<User> getUsersFromAuthority(@PathVariable int authorityId) {
        try {
            return authorityDao.getUsersFromAuthority(authorityId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping
    public void addAuthority(@RequestBody Authority authority) {
        try {
            authorityDao.addAuthority(authority);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/{authorityId}")
    public void updateAuthority(@PathVariable int authorityId, @RequestBody Authority authority) {
        try {
            authority.setAuthorityId(authorityId);
            authorityDao.updateAuthority(authority);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping("/{authorityId}")
    public void deleteAuthority(@PathVariable int authorityId) {
        try {
            authorityDao.deleteAuthority(authorityId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping("/{authorityId}/user/{userId}")
    public void addUserToAuthority(@PathVariable int authorityId, @PathVariable String userId) {
        try {
            authorityDao.addUserToAuthority(userId, authorityId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping("/{authorityId}/user/{userId}")
    public void removeUserFromAuthority(@PathVariable int authorityId, @PathVariable String userId) {
        try {
            authorityDao.removeUserFromAuthority(userId, authorityId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

}
