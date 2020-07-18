package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.helpers.*;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.*;
import be.ugent.blok2.model.users.Authority;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.security.UsersCache;
import be.ugent.blok2.services.LdapService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This controller handles all requests related to users.
 * Such as registration, list of users, specific users, ...
 */
@RestController
@RequestMapping("api/account")
@Api(value = "Account login/registration system")
public class AccountController extends AController{
    private final String VERIFICATION_SUBJECT;

    private final IAccountDao accountDao;
    private final ResourceBundle applicationBundle;
    private final EmailService emailService;
    private final LdapService ldapService;
    private final UsersCache usersCache;

    public AccountController(IAccountDao dao, EmailService emailService, LdapService ldapService) {
        this.accountDao = dao;
        this.applicationBundle = Resources.applicationProperties;
        this.emailService = emailService;
        this.ldapService = ldapService;
        this.usersCache = UsersCache.getInstance();
        VERIFICATION_SUBJECT = this.applicationBundle.getString("verification.mail.subject");
    }

    /**
     * There are two situations for adding a user:
     *   - New user is created by the student itself: through RegistrationComponent
     *   - New user is created by an employee: through UserOverviewComponent
     * This implementation is meant for the user
     */
    @PostMapping(value = "/new")
    public ResponseEntity newAccount(@RequestBody User user) {
        try {
            // If user is from UGhent, he/she needs to use CAS
            if (user.getMail().toLowerCase().contains("@ugent"))
                return new ResponseEntity("UGhent user -> CAS", HttpStatus.NOT_ACCEPTABLE);

            // Check that there is no user with email a.getEmail()
            if (accountDao.accountExistsByEmail(user.getMail()))
                return new ResponseEntity("WARNING: User with email " + user.getMail() + " already exists.", HttpStatus.CONFLICT);

            List<User> ldapList = ldapService.searchUserByMail(user.getMail());
            if (ldapList.size() == 0) {
                return new ResponseEntity("WARNING: No student found with email " + user.getMail() +
                        ". Note that you must be a student at an institution that is part of the " +
                        "Association UGent", HttpStatus.FORBIDDEN);
            } else if (ldapList.size() > 1) {
                // Note: this will never occur because there can only be one student with a certain email address
                //  but it is still here, you never know...
                return new ResponseEntity("WARNING: More than one LDAP Person found with email " + user.getMail(), HttpStatus.CONFLICT);
            }

            // Encode the received password
            String pwd = (new BCryptPasswordEncoder()).encode(user.getPassword());
            User student = ldapList.get(0);
            student.setPassword(pwd);

            String verificationCode = accountDao.addUserToBeVerified(student);
            sendVerificationMail(student.getMail(), verificationCode, student);
            return new ResponseEntity(HttpStatus.ACCEPTED);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (LdapException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * There are two situations for adding a user:
     *   - New user is created by the student itself: through RegistrationComponent
     *   - New user is created by an employee: through UserOverviewComponent
     * This implementation is meant for the employee (or admin)
     */
    @PostMapping(value = "/new/by/employee")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
    public ResponseEntity newAccountByEmployee(@RequestBody User u) {
        // Check that there is no user with email a.getEmail()
        if (accountDao.accountExistsByEmail(u.getMail()))
            return new ResponseEntity("WARNING: User with email " + u.getMail() + " already exists.", HttpStatus.CONFLICT);

        List<User> ldapList = ldapService.searchUserByMail(u.getMail());
        if (ldapList.size() == 0) {
            return new ResponseEntity("WARNING: No student found with email " + u.getMail() +
                    ". Note that you must be a student at an institution that is part of the " +
                    "Association UGent", HttpStatus.FORBIDDEN);
        } else if (ldapList.size() > 1) {
            // Note: this will never occur because there can only be one student with a certain email address
            //  but it is still here, you never know...
            return new ResponseEntity("WARNING: More than one LDAP Person found with email " + u.getMail(), HttpStatus.CONFLICT);
        }
        User toAdd = ldapList.get(0);

        // Encode the received password
        String pwd = (new BCryptPasswordEncoder()).encode(u.getPassword());
        toAdd.setPassword(pwd);
        toAdd.setRoles(u.getRoles());

        accountDao.directlyAddUser(toAdd);

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @GetMapping(value = "/verify")
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    public ResponseEntity verifyAccount(@RequestParam("code") String verificationCode) {
        try {
            accountDao.verifyNewUser(verificationCode);
            return new ResponseEntity(HttpStatus.ACCEPTED);
        } catch (WrongVerificationCodeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/email/{email}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "View a specific user")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
            User u = accountDao.getUserByEmail(email);
            return new ResponseEntity<>(u, HttpStatus.OK);
    }

    @GetMapping(value = "/role/{role}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "View a specific user")
    public List<String> getUsersNamesByRole(@PathVariable String role) {
        return accountDao.getUserNamesByRole(role);
    }

    /**
     * Get the user that is logged in and has the given mapping cookie
     * that corresponds to his session id. This prevents that users with
     * a copied mapping cookie can access other users their details. The
     * session id can never be changed because it is saved in a HTTPOnly cookie.
     */
    @GetMapping(value = "/session/{mapping}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE','SCAN')")
    @ApiOperation(value = "View the logged in user, according to his session id")
    public ResponseEntity<User> getUserFromSession(@PathVariable String mapping, HttpSession session) {
        if(!this.usersCache.isValid(session.getId(), mapping)){
            // in case the cookies don't match, invalidate the session
            session.invalidate();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            User user = this.usersCache.getBySessionIdMapping(mapping);
            return new ResponseEntity<>(user.cloneToSendableUser(), HttpStatus.OK);
        } catch (NoUserLoggedInWithGivenSessionIdMappingException e) {

        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE')")
    @ApiOperation(value = "View a specific user")
    public ResponseEntity<User> getUserById(@PathVariable String id, HttpServletRequest request) throws NoUserLoggedInWithGivenSessionIdMappingException {
        User u=getCurrentUser(request);
        // make sure students can not access other user their details
        if (!isTesting() && u.getAuthorities().contains(new Authority(Role.STUDENT)) &&
                u.getAuthorities().size() == 1 && !id.equals(u.getAugentID())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(accountDao.getUserById(id), HttpStatus.OK);
    }

    @GetMapping(value = "/lastName/{lastName}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @ApiOperation(value = "View a list of users")
    public List<User> getUsersByLastName(@PathVariable String lastName) {
        return accountDao.getUsersByLastName(lastName);
    }

    @GetMapping(value = "/name/{name}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @ApiOperation(value = "View a list of users")
    public List<User> getUsersByName(@PathVariable String name) {
        // used for searches, the name does not have to written correct
        return accountDao.getUsersByNameSoundex(name);
    }

    @GetMapping(value = "/firstName/{firstName}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @ApiOperation(value = "View a list of users")
    public List<User> getUsersByFirstName(@PathVariable String firstName) {
        return accountDao.getUsersByFirstName(firstName);
    }


    @PutMapping(value = "/{email}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE')")
    @ApiOperation(value = "Update a specific user")
    public ResponseEntity updateUser(@RequestBody User user, @PathVariable String email) {
        /*
        Received user contains password in plain-text. First we encode the password so the DAO / DB
        only knows the encoded password.
         */
        if (user.getPassword() != null && user.getPassword().length() > 0) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            user.setPassword(encoder.encode(user.getPassword()));
        }
        try{
            accountDao.updateUser(email, user);
            return new ResponseEntity(HttpStatus.OK);
        } catch (NoSuchUserException ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch( UserAlreadyExistsException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> exists(@PathVariable String email) {
        return new ResponseEntity<>(accountDao.accountExistsByEmail(email), HttpStatus.OK);
    }

    /**
     * Get the locations that a user is allowed to start the scanning process
     * at.
     */
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @GetMapping("/scanlocations/{email}")
    public ResponseEntity getScanlocationsNames(@PathVariable String email) {
        boolean flag = false;
        User u = accountDao.getUserByEmail(email);
        if(u == null){
            return new ResponseEntity<>("No user with mail = " + email,HttpStatus.NOT_FOUND);
        }
        for (Role r: u.getRoles()){
            if(r.equals(Role.EMPLOYEE) || r.equals(Role.ADMIN)) flag = true;
        }
        if(!flag){
            return new ResponseEntity<>("This user isn't an employee or admin so can't have any locations to scan.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(accountDao.getScannerLocations(email),HttpStatus.OK);


    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable String id){
        accountDao.removeUserById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Sending the verification mail
    private void sendVerificationMail(String email, String verificationCode, User student) {
        String name = student.getFirstName() + " " + student.getLastName();
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        String link = baseUrl + applicationBundle.getString("verification.url") + "/" + verificationCode;
        String text = String.format(applicationBundle.getString("verification.mail.template"), name, link, name, link);

        emailService.sendMessage(email, VERIFICATION_SUBJECT, text);
    }

    @ExceptionHandler(NoUserLoggedInWithGivenSessionIdMappingException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handleUnauthorized() {

    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handle() {

    }
}
