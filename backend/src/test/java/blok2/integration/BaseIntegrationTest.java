package blok2.integration;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.daos.*;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.reservables.Location;
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
public class BaseIntegrationTest extends BaseTest {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;
    @Autowired
    private ICalendarPeriodDao calendarPeriodDao;
    @Autowired
    private IBuildingDao buildingDao;
    @Autowired
    protected MockMvc mockMvc;

    protected Location testLocation;
    protected User admin;
    protected User student;
    protected User student2;
    protected List<CalendarPeriod> calendarPeriods;
    @Autowired
    protected ObjectMapper objectMapper;

    protected final String baseURI = "";


    public void populateDatabase() throws SQLException {
        // USERS
        /*
         * Users are added by the TestSecurityConfig
         * For security reasons
         */
        // LOCATIONS
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        locationDao.addLocation(testLocation);
        // CALENDAR PERIOD
        calendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);

        CalendarPeriod[] cps = new CalendarPeriod[calendarPeriods.size()];
        cps = calendarPeriods.toArray(cps);
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, cps);
    }
}
