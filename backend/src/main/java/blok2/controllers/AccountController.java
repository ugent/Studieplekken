package blok2.controllers;

import blok2.daos.IUserDao;
import blok2.daos.IAuthorityDao;
import blok2.daos.ILocationDao;
import blok2.helpers.exceptions.InvalidRequestParametersException;
import blok2.helpers.exceptions.NoSuchUserException;
import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Pattern;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;


/**
 * This controller handles all requests related to users.
 * Such as registration, list of users, specific users, ...
 */
@RestController
@RequestMapping("account")
@Validated
public class AccountController {

    private final Logger logger = LoggerFactory.getLogger(AccountController.class.getSimpleName());

    private final IUserDao userDao;
    private final IAuthorityDao authorityDao;
    private final ILocationDao locationDao;

    public AccountController(IUserDao userDao, IAuthorityDao authorityDao, ILocationDao locationDao) {
        this.userDao = userDao;
        this.authorityDao = authorityDao;
        this.locationDao = locationDao;
    }

    @GetMapping("/admins")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> getAdmins() {
        try {
            return userDao.getAdmins();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/id")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public User getUserByAUGentId(@RequestParam
                                  @Pattern(regexp = "^[^%_]*$")
                                          String id) {
        try {
            return userDao.getUserById(id);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/mail")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public User getUserByMail(@RequestParam
                              @Pattern(regexp = "^[^%_]*$")
                                      String mail) {
        try {
            return userDao.getUserByEmail(mail);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/firstName")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<User> getUsersByFirstName(@RequestParam("firstName")
                                          @Pattern(regexp = "^[^%_]*$")
                                                  String firstName) {
        try {
            return userDao.getUsersByFirstName(firstName);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/lastName")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<User> getUsersByLastName(@RequestParam
                                         @Pattern(regexp = "^[^%_]*$")
                                                 String lastName) {
        try {
            return userDao.getUsersByLastName(lastName);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/firstAndLastName")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<User> getUsersByLastName(@RequestParam
                                         @Pattern(regexp = "^[^%_]*$")
                                                 String firstName,
                                         @RequestParam
                                         @Pattern(regexp = "^[^%_]*$")
                                                 String lastName) {
        try {
            return userDao.getUsersByFirstAndLastName(firstName, lastName);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/barcode")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public User getUserByBarcode(@RequestParam String barcode) {
        try {
            User userLinkedToBarcode = userDao.getUserFromBarcode(barcode);

            if (userLinkedToBarcode == null) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "No user found with barcode " + barcode);
            }

            return userDao.getUserById(userLinkedToBarcode.getUserId());
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/{userId}/authorities")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<Authority> getAuthoritiesFromUser(@PathVariable
                                                  @Pattern(regexp = "^[^%_]*$")
                                                          String userId) {
        try {
            return authorityDao.getAuthoritiesFromUser(userId);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("{userId}/manageable/locations")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<Location> getManageableLocations(@PathVariable("userId") @Pattern(regexp = "^[^%_]*$") String userId) {
        try {
            User user = userDao.getUserById(userId);

            // should never happen due to authentication, but you never know what changes in the future
            if (user == null)
                throw new NoSuchUserException("No such user");

            if (user.isAdmin()) {
                return locationDao.getAllActiveLocations();
            } else {
                return authorityDao.getLocationsInAuthoritiesOfUser(userId);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("{userId}/has/authorities")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public boolean hasUserAuthorities(@PathVariable("userId") @Pattern(regexp = "^[^%_]*$") String userId) {
        try {
            return authorityDao.getAuthoritiesFromUser(userId).size() > 0;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("{userId}/has/volunteered")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public boolean hasUserVolunteered(@PathVariable("userId") @Pattern(regexp = "^[^%_]*$") String userId) {
        try {
            return userDao.getVolunteeredLocations(userId).size() > 0;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void updateUser(@PathVariable("userId")
                           @Pattern(regexp = "^[^%_]*$")
                                   String id, @RequestBody User user) {
        try {
            User old = userDao.getUserById(id);
            userDao.updateUserById(id, user);
            logger.info(String.format("Updated user %s from %s to %s", id, old, user));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/password")
    @PreAuthorize("hasAuthority('USER')")
    public void changePassword(@RequestBody ChangePasswordBody body) {
        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            // check if 'from' is the correct current password
            User actualUser = userDao.getUserById(body.user.getUserId());

            if (!encoder.matches(body.from, actualUser.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password");
            }

            // check if 'to' is valid
            if (!isValidPassword(body.to)) {
                throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Wrong format of new password");
            }

            // change user's password
            String encryptedTo = encoder.encode(body.to);
            User updatedUser = actualUser.clone();
            updatedUser.setPassword(encryptedTo);
            userDao.updateUserById(actualUser.getUserId(), updatedUser);

        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        throw new InvalidRequestParametersException("Not valid due to validation error: " + e.getMessage());
    }

    /**
     * The password needs to contain at least:
     * - 1 capital letter
     * - 1 number
     * - 8 characters
     */
    private boolean isValidPassword(String password) {
        if (password.length() < 8)
            return false;

        boolean hasNumber = false, hasCapital = false;
        for (int i = 0; i < password.length(); i++) {
            char charAtI = password.charAt(i);

            if (charAtI >= 'A' && charAtI <= 'Z')
                hasCapital = true;

            else if (charAtI >= '0' && charAtI <= '9')
                hasNumber = true;
        }

        return hasCapital && hasNumber;
    }

    private static class ChangePasswordBody {
        private String from;
        private String to;
        private User user;

        public ChangePasswordBody() {
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }
}
