package blok2.integration;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.database.dao.*;
import blok2.model.ActionLogEntry;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.calendar.Timeslot;
import blok2.model.location.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest extends BaseTest {

    @Autowired
    protected IUserDao userDao;

    @Autowired
    protected ILocationDao locationDao;

    @Autowired
    protected IAuthorityDao authorityDao;

    @Autowired
    protected ITimeslotDao timeslotDAO;
    @Autowired
    protected IBuildingDao buildingDao;

    @Autowired
    protected IActionLogDao actionLogDao;

    @Autowired
    protected ILocationReservationDao locationReservationDao;

    @Autowired
    protected IVolunteerDao volunteerDao;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AuthenticationManager am;

    protected Location testLocation;
    protected Location testLocationHoGent;
    protected Location testLocationUnapproved;
    protected List<Location> locations = new ArrayList<>();
    protected List<Location> unapprovedLocations = new ArrayList<>();


    protected Authority authority;
    protected Building testBuilding;
    protected Building testBuildingHoGent;
    protected List<Building> buildings = new ArrayList<>();

    protected User admin;
    protected User student;
    protected User student2;
    protected List<Timeslot> calendarPeriods;
    @Autowired
    protected ObjectMapper objectMapper;
    protected User authHolder;
    protected User authHolderHoGent;

    public void populateDatabase() throws SQLException {
        // USERS
        /*
         * Users are added by the TestSecurityConfig
         * For security reasons
         */

        // LOCATIONS
        authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        testBuildingHoGent = buildingDao.addBuilding(TestSharedMethods.testBuildingHoGent());
        buildings.add(testBuilding);
        buildings.add(testBuildingHoGent);

        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        locationDao.addLocation(testLocation);
        locationDao.approveLocation(testLocation, true);
        testLocationUnapproved = TestSharedMethods.testLocation2(authority.clone(), testBuilding);
        locationDao.addLocation(testLocationUnapproved);
        locations.add(testLocation);
        unapprovedLocations.add(testLocationUnapproved);

        testLocationHoGent = TestSharedMethods.testLocation(authority.clone(), testBuildingHoGent);
        testLocationHoGent.setName("TestLocation HoGent");
        locationDao.addLocation(testLocationHoGent);
        locationDao.approveLocation(testLocationHoGent, true);
        locations.add(testLocationHoGent);

        // CALENDAR PERIOD
        calendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);

        calendarPeriods = timeslotDAO.addTimeslots(calendarPeriods);

        admin = userDao.getUserByEmail("admin@ugent.be");
        student = userDao.getUserByEmail("student1@ugent.be");
        student2 = userDao.getUserByEmail("student2@ugent.be");
        authHolder = userDao.getUserByEmail("authholder@ugent.be");
        authHolderHoGent = userDao.getUserByEmail("authholderHoGent@hogent.be");

        authorityDao.addUserToAuthority(authHolder.getUserId(), authority.getAuthorityId());
        authHolder = userDao.getUserByEmail("authholder@ugent.be");


        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());
        // Add another timeslot which is in the future so we can test if an email is sent when deleting an upcoming reservation slot.
        Timeslot timeslot2 = timeslotDAO.getTimeslot(calendarPeriods.get(calendarPeriods.size() - 1).getTimeslotSeqnr());

        LocationReservation reservation = new LocationReservation(student, timeslot, null);
        LocationReservation reservation2 = new LocationReservation(student, timeslot2, null);
        locationReservationDao.addLocationReservationIfStillRoomAtomically(reservation);
        locationReservationDao.addLocationReservationIfStillRoomAtomically(reservation2);

        volunteerDao.addVolunteer(testLocation.getLocationId(), student2.getUserId());


        // Refreshing the user to the latest database state
        // Only to be done in tests!
        SecurityContext securityContext = SecurityContextHolder.getContext();
        User currentUser = (User) securityContext.getAuthentication().getPrincipal();
        User refreshedUser = userDao.getUserById(currentUser.getUserId());
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(refreshedUser, "", refreshedUser.getAuthorities());
        securityContext.setAuthentication(authRequest);
    }

    public boolean hasActionLogEntry(String userId, String domain) {
        List<ActionLogEntry> list = actionLogDao.getAllActions();
        for (ActionLogEntry entry : list) {
            if (userId != null && !userId.equals(entry.getUser().getUserId())) {
                continue;
            }
            if (!entry.getDomain().toLowerCase().contains(domain.toLowerCase())) {
                continue;
            }
            return true;
        }
        return false;
    }

}
