package blok2.controllers;

import blok2.daos.IUserDao;
import blok2.daos.IAuthorityDao;
import blok2.daos.ILocationDao;
import blok2.daos.IVolunteerDao;
import blok2.helpers.exceptions.InvalidRequestParametersException;
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
    private final IVolunteerDao volunteerDao;

    public AccountController(IUserDao userDao, IAuthorityDao authorityDao, ILocationDao locationDao, IVolunteerDao volunteerDao) {
        this.userDao = userDao;
        this.authorityDao = authorityDao;
        this.locationDao = locationDao;
        this.volunteerDao = volunteerDao;
    }

    @GetMapping("/admins")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> getAdmins() {
        return userDao.getAdmins();
    }

    @GetMapping("/id")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public User getUserByAUGentId(@RequestParam
                                  @Pattern(regexp = "^[^%_]*$")
                                          String id) {
        return userDao.getUserById(id);
    }

    @GetMapping("/mail")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public User getUserByMail(@RequestParam
                              @Pattern(regexp = "^[^%_]*$")
                                      String mail) {
        return userDao.getUserByEmail(mail);
    }

    @GetMapping("/firstName")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<User> getUsersByFirstName(@RequestParam("firstName")
                                          @Pattern(regexp = "^[^%_]*$")
                                                  String firstName) {
        return userDao.getUsersByFirstName(firstName);
    }

    @GetMapping("/lastName")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<User> getUsersByLastName(@RequestParam
                                         @Pattern(regexp = "^[^%_]*$")
                                                 String lastName) {
        return userDao.getUsersByLastName(lastName);
    }

    @GetMapping("/firstAndLastName")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<User> getUsersByLastName(@RequestParam
                                         @Pattern(regexp = "^[^%_]*$")
                                                 String firstName,
                                         @RequestParam
                                         @Pattern(regexp = "^[^%_]*$")
                                                 String lastName) {
        return userDao.getUsersByFirstAndLastName(firstName, lastName);
    }

    @GetMapping("/barcode")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public User getUserByBarcode(@RequestParam String barcode) {
        User userLinkedToBarcode = userDao.getUserFromBarcode(barcode);
        return userDao.getUserById(userLinkedToBarcode.getUserId());
    }

    @GetMapping("/{userId}/authorities")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<Authority> getAuthoritiesFromUser(@PathVariable
                                                  @Pattern(regexp = "^[^%_]*$")
                                                          String userId) {
        return authorityDao.getAuthoritiesFromUser(userId);
    }

    @GetMapping("{userId}/manageable/locations")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<Location> getManageableLocations(@PathVariable("userId") @Pattern(regexp = "^[^%_]*$") String userId) {
        User user = userDao.getUserById(userId);

        if (user.isAdmin()) {
            return locationDao.getAllActiveLocations();
        } else {
            return authorityDao.getLocationsInAuthoritiesOfUser(userId);
        }
    }

    @GetMapping("{userId}/has/authorities")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public boolean hasUserAuthorities(@PathVariable("userId") @Pattern(regexp = "^[^%_]*$") String userId) {
        return authorityDao.getAuthoritiesFromUser(userId).size() > 0;
    }

    @GetMapping("{userId}/has/volunteered")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public boolean hasUserVolunteered(@PathVariable("userId") @Pattern(regexp = "^[^%_]*$") String userId) {
        return volunteerDao.getVolunteeredLocations(userId).size() > 0;
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void updateUser(@PathVariable("userId")
                           @Pattern(regexp = "^[^%_]*$")
                                   String id, @RequestBody User user) {
        User old = userDao.getUserById(id);
        userDao.updateUser(user);
        logger.info(String.format("Updated user %s from %s to %s", id, old, user));
    }

    @PutMapping("/password")
    @PreAuthorize("hasAuthority('USER')")
    public void changePassword(@RequestBody ChangePasswordBody body) {
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
        userDao.updateUser(updatedUser);
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
