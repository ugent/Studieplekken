package be.ugent.blok2.daos.dummies;

import be.ugent.blok2.controllers.BarcodeController;
import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.daos.IPenaltyEventsDao;
import be.ugent.blok2.helpers.Institution;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.helpers.generators.IGenerator;
import be.ugent.blok2.helpers.generators.VerificationCodeGenerator;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.UserAlreadyExistsException;
import be.ugent.blok2.helpers.exceptions.WrongVerificationCodeException;
import be.ugent.blok2.model.penalty.Penalty;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import org.apache.commons.codec.language.Soundex;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Profile("dummy")
@Service
public class DummyAccountDao implements IAccountDao {
    /*
     * In this dummy dao, all users are kept in a HashMap.
     * The keys are the user's email and the value is the instance of the user.
     */
    private Map<String, User> users; // maps augentid to user
    private Soundex soundex = new Soundex();
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private IGenerator<String> verificationCodeGenerator =  new VerificationCodeGenerator();
    public static List<User> usersForReservations;

    private IPenaltyEventsDao penaltyDao;
    public static final double WEEKLY_PENALTY_POINTS_DECREASE = 0.2;
    public static User TEST_USER;

    /*
     * When a new user wants to register, they need to verify their account
     * with their email account. This to make sure the new user is who he
     * says he is.
     *
     * A verification code is created when the request for a new user is made.
     * Then a mail is send to the user's mail in which a link is put. In the
     * link, the verification code is put as a query string. This allows a
     * get request to send the user to the profile page
     *
     * In between the time of making a new account and verifying the account,
     * the account may not be put in the users container. Instead we keep a
     * second container of all the users to be verified. If one user verifies,
     * the user is moved to the users container.
     *
     * The key is the verification code
     * The value is the instance of the user to be verified
     */
    private Map<String, User> usersToBeVerified;

    public DummyAccountDao(IPenaltyEventsDao penaltyDao) {
        this.penaltyDao = penaltyDao;

        users = new HashMap<>();
        usersToBeVerified = new HashMap<>();
        TEST_USER = new User("001700580731", "Callebaut", "Paulien"
                , "paulien.callebaut@ugent.be", passwordEncoder.encode("paulien"), "UGent"
                , new Role[]{Role.STUDENT},25,"001700580731");
        //users.put("paulien.callebaut@ugent.be", TEST_USER);
        //users.put("001700580731", TEST_USER);
        User bdb = new User("2000020400000", "De Baer", "Brecht"
                , "brecht.debaer@ugent.be", passwordEncoder.encode("brecht"), "UGent"
                , new Role[]{Role.STUDENT}, 50,"2000020400000");
        users.put("2000020400000",bdb );
        User sdc =  new User("001707977015", "De Coninck", "Sander"
                , "sander.deconinck@ugent.be", passwordEncoder.encode("sander"), "UGent"
                , new Role[]{Role.STUDENT}, 75,"001707977015");
        //users.put("001707977015", sdc);
        users.put("0000000005005", new User("0000000005005", "user", "user"
                , "student", passwordEncoder.encode("student"), "UGent"
                ,new Role[]{Role.STUDENT}, 75,"001703195697"));
        users.put("0000000006002", new User("0000000006002", "admin", "admin"
                , "admin", passwordEncoder.encode("admin"), "UGent"
                , new Role[]{Role.EMPLOYEE, Role.ADMIN}, -1,"001703195697"));
        users.put("0000000007009", new User("0000000007009", "scanmedewerker", "scanmedewerker"
                , "scanmedewerker", passwordEncoder.encode("scanmedewerker"), "UGent"
                ,  new Role[]{Role.EMPLOYEE}, -1,"001703195697"));
        users.put("0000000008006", new User("0000000008006", "student-scanmedewerker", "student-scanmedewerker"
                , "student-scanmedewerker", passwordEncoder.encode("student-scanmedewerker"), "UGent"
                ,  new Role[]{Role.STUDENT, Role.EMPLOYEE}, 50,"001703195697"));

        usersForReservations = new ArrayList<>();
        usersForReservations.add(TEST_USER);
        usersForReservations.add(bdb);
        //usersForReservations.add(sdc);
    }

    @Override
    public boolean accountExistsByEmail(String email) {
        return getUserByEmail(email)!=null;
    }

    @Override
    public User getUserByEmail(String email) {
        for (User u : users.values()) {
            if (u.getMail().toLowerCase().equals(email.toLowerCase())) {
                u.setPenaltyPoints(calcPenaltyPoints(u.getAugentID()));
                // clone to avoid potential privacy leak!
                return u.cloneToSendableUser();
            }
        }
        return null;
    }

    @Override
    public User getUserByEmailWithPassword(String email) {
        for (User u : users.values()) {
            if (u.getMail().toLowerCase().equals(email.toLowerCase())) {
                u.setPenaltyPoints(calcPenaltyPoints(u.getAugentID()));
                // clone to avoid potential privacy leak!
                return u;
            }
        }
        return null;
    }

    @Override
    public User getUserById(String augentID) {
        User user = users.get(augentID);
        if(user!=null){
            return user.cloneToSendableUser();
        }
        return null;
    }

    @Override
    public List<User> getUsersByLastName(String lastName) {
        List<User> _users = new ArrayList<>();

        for (User u : users.values()) {
            if (u.getLastName().toLowerCase().equals(lastName.toLowerCase())) {
                u.setPenaltyPoints(calcPenaltyPoints(u.getAugentID()));
                // clone to avoid potential privacy leak!
                _users.add(u.cloneToSendableUser());
            }
        }

        return _users;
    }

    @Override
    public List<User> getUsersByFirstName(String firstName) {
        List<User> _users = new ArrayList<>();
        for (User u : users.values()) {
            if (u.getFirstName().toLowerCase().equals(firstName.toLowerCase())) {
                u.setPenaltyPoints(calcPenaltyPoints(u.getAugentID()));
                // clone to avoid potential privacy leak!
                _users.add(u.cloneToSendableUser());
            }
        }

        return _users;
    }

    // gets users whose firstname, lastname or first+last name sounds like the inputparameter
    @Override
    public List<User> getUsersByNameSoundex(String name) {
        List<User> _users = new ArrayList<>();
        String s1 = soundex.encode(name.toLowerCase());
        for (User u : users.values()) {
            String s2 = soundex.encode(u.getFirstName() + " " + u.getLastName());
            if (s2.equals(s1) || s1.equals(soundex.encode(u.getLastName().toLowerCase()))
                || s1.equals(soundex.encode(u.getFirstName().toLowerCase()))) {
                u.setPenaltyPoints(calcPenaltyPoints(u.getAugentID()));
                _users.add(u.cloneToSendableUser());
            }
        }
        return _users;
    }

    @Override
    public List<String> getUserNamesByRole(String role) {
        List<String> _users_names = new ArrayList<>();

        for (User u: users.values()){
            for(Role r : u.getRoles()){
                if(r.toString().equals(role.toUpperCase())){
                    _users_names.add(u.shortString());
                }
            }
        }

        return _users_names;
    }

    @Override
    public List<String> getScannerLocations(String email) {
        User user =  new User();
        for (User u : users.values()) {
            if (u.getMail().toLowerCase().equals(email.toLowerCase())) {
                u.setPenaltyPoints(calcPenaltyPoints(u.getAugentID()));
                user = u;
            }
        }
        return (List<String>) user.getScannerLocations();
    }

    @Override
    public void setScannerLocation(String augentID, String location) {
        User user = new User();
        for (User u : users.values()) {
            if (u.getAugentID().equals(augentID)) {
                user = u;
            }
        }
        user.addScanLocation(location);
    }

    @Override
    public User directlyAddUser(User u) {
        // immediately add the user to the
        // note that if the u.getAugentID() already exists, it will be overwritten
        if (Institution.UGent.equals(u.getInstitution()))
            u.setPassword("UGent-Student-Heeft-Geen-Paswoord-Wegens-Inloggen-Met-CAS");
        users.put(u.getAugentID(), u);
        if (u.getBarcode() == null || u.getBarcode().length() == 0) {
            // indien geen barcode voorzien werd, deze instellen op UPC-A gegenereerd op basis van AUGent id
            String barcode = u.getAugentID();
            while(barcode.length()>11){
                barcode=barcode.substring(1);
            }
            barcode= BarcodeController.calculateUPCACheckSum(barcode);
            u.setBarcode(barcode);
        }
        return u;
    }

    @Override
    public String addUserToBeVerified(User u) throws UserAlreadyExistsException {
        System.out.println("User to be verified added: " + u);
        // the email used for the new user cannot be already used
        // for an existing account
        if (users.containsKey(u.getAugentID()) || usersToBeVerified.containsKey(u.getMail().toLowerCase()))
            throw new UserAlreadyExistsException("Exception for " + u.cloneToSendableUser());

        // clone to avoid potential privacy leak!
        // verification code will be used in the verification mail to ensure
        // the user is who he says he is
        String verificationCode = verificationCodeGenerator.generate();
        usersToBeVerified.put(verificationCode, u.clone());

        return verificationCode;
    }

    @Override
    public void verifyNewUser(String verificationCode) throws WrongVerificationCodeException {
        User newUser = usersToBeVerified.remove(verificationCode);

        // null means: no entry with the given verification code
        if (newUser == null)
            throw new WrongVerificationCodeException("No new user to be verified with verification code "
                    + verificationCode + " was found.");

        // no need to clone newUser because the reference came from usersToBeVerified
        // so no privacy leaks can be introduced (if the adding-process of usersToBeVerified
        // takes care of it)
        users.put(newUser.getAugentID(), newUser);
    }

    @Override
    public void updateUser(String email, User u) throws NoSuchUserException {
        User user = getUserByEmailWithPassword(email);
        if (user == null)
            throw new NoSuchUserException("No user with email = " + email );
        // if the password field was empty, use the previous saved password
        if(u.getPassword() == null || u.getPassword().length()==0){
            u.setPassword(user.getPassword());
        }

        //change of augentId should delete old one
        if(!u.getAugentID().equals(user.getAugentID())){
            users.remove(user.getAugentID());
        }
        // clone to avoid potential privacy leak!
        users.put(u.getAugentID(), u.clone());
    }

    @Override
    public void removeUserById(String AUGentID){
        this.users.remove(AUGentID);
    }

    private int calcPenaltyPoints(String augentID) {
        List<Penalty> l = penaltyDao.getPenalties(augentID);

        // make a list which contains all percentage values for all possible weeks
        // (this is based on the recursive part of the DBAccountDao query performed to calculate the penalty points)
        List<Double> pnts = new ArrayList<>();
        for (double d = 1; d >= 0; d -= WEEKLY_PENALTY_POINTS_DECREASE) {
            pnts.add(d);
        }

        int s = 0;
        for (Penalty p : l) {
            // Event with points 16663 is a Blacklist event and may therefore not be decreased in function of time.
            // If an employee wants reintegrate the student into the reservation system, the employee needs to
            // delete this event manually in the UserPenaltiesOverviewComponent (Management > Users > "search for
            // student" > Edit > Manage Penalties > "remove the 16663 event"
            if (p.getEventCode() == 16663) {
                s += p.getReceivedPoints();
            } else if (l.size() > 0) {
                DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                LocalDateTime t1 = LocalDateTime.parse(p.getTimestamp().toString(), dtf);
                LocalDateTime t2 = LocalDateTime.now();
                int weeksBetween = (int) (Duration.between(t1, t2).toDays() / 7.0);
                s += (int) (pnts.get(weeksBetween) * p.getReceivedPoints());
            }
        }
        return s;
    }
}
