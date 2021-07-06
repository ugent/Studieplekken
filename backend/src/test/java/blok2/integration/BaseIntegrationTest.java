package blok2.integration;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.daos.*;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
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
    protected ICalendarPeriodDao calendarPeriodDao;

    @Autowired
    protected IBuildingDao buildingDao;

    @Autowired
    protected ILocationReservationDao locationReservationDao;

    @Autowired
    protected IVolunteerDao volunteerDao;

    @Autowired
    protected MockMvc mockMvc;

    protected Location testLocation;
    protected Location testLocationUnapproved;

    protected Authority authority;
    protected Building testBuilding;

    protected User admin;
    protected User student;
    protected User student2;
    protected List<CalendarPeriod> calendarPeriods;
    @Autowired
    protected ObjectMapper objectMapper;
    protected User authHolder;

    public void populateDatabase() throws SQLException {
        // USERS
        /*
         * Users are added by the TestSecurityConfig
         * For security reasons
         */

        // LOCATIONS
        authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        locationDao.addLocation(testLocation);
        locationDao.approveLocation(testLocation, true);
        testLocationUnapproved = TestSharedMethods.testLocation2(authority.clone(), testBuilding);
        locationDao.addLocation(testLocationUnapproved);

        // CALENDAR PERIOD
        calendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);

        CalendarPeriod[] cps = new CalendarPeriod[calendarPeriods.size()];
        cps = calendarPeriods.toArray(cps);
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, cps);

        admin = userDao.getUserByEmail("admin@ugent.be");
        student= userDao.getUserByEmail("student1@ugent.be");
        student2 = userDao.getUserByEmail("student2@ugent.be");
        authHolder = userDao.getUserByEmail("authholder@ugent.be");

        authorityDao.addUserToAuthority(authHolder.getUserId(), authority.getAuthorityId());
        authHolder = userDao.getUserByEmail("authholder@ugent.be");

        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);
        // Add another timeslot which is in the future so we can test if an email is sent when deleting an upcoming reservation slot.
        Timeslot timeslot2 = calendarPeriodDao.getById(calendarPeriods.get(calendarPeriods.size() - 1).getId()).getTimeslots().get(calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().size() - 1);

        LocationReservation reservation = new LocationReservation(student, timeslot, null);
        LocationReservation reservation2 = new LocationReservation(student, timeslot2, null);
        locationReservationDao.addLocationReservationIfStillRoomAtomically(reservation);
        locationReservationDao.addLocationReservationIfStillRoomAtomically(reservation2);

        volunteerDao.addVolunteer(testLocation.getLocationId(), student2.getUserId());
    }
}
