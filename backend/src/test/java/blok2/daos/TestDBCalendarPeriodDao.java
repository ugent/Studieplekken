package blok2.daos;

import blok2.model.Authority;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.reservables.Location;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestDBCalendarPeriodDao {

    @Autowired
    private ICalendarPeriodDao calendarPeriodDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    private Location testLocation;
    private Authority authority;
    private List<CalendarPeriod> calendarPeriods;

    // the reason for making this an attribute of the class
    // is to make sure the values are deleted when something
    // goes wrong
    private List<CalendarPeriod> updatedPeriods;

    @Before
    public void setup() throws SQLException {
        // Use test database
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(calendarPeriodDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(authorityDao);

        // Setup test objects
        authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testLocation = TestSharedMethods.testLocation(authority.getAuthorityId());
        calendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);
        updatedPeriods = TestSharedMethods.testCalendarPeriodsButUpdated(testLocation);

        // Add test objects to database
        locationDao.addLocation(testLocation);
        calendarPeriodDao.addCalendarPeriods(calendarPeriods);
    }

    @After
    public void cleanup() throws SQLException {
        // Remove test objects from database
        calendarPeriodDao.deleteCalendarPeriods(updatedPeriods); // in case this would be necessary
        calendarPeriodDao.deleteCalendarPeriods(calendarPeriods);
        locationDao.deleteLocation(testLocation.getName());
        authorityDao.deleteAuthority(authority.getAuthorityId());

        // Use regular database
        calendarPeriodDao.useDefaultDatabaseConnection();
        locationDao.useDefaultDatabaseConnection();
        authorityDao.useDefaultDatabaseConnection();
    }

    @Test
    public void addCalendarPeriodsTest() throws SQLException {
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        calendarPeriods.sort(Comparator.comparing(CalendarPeriod::toString));

        Assert.assertEquals("addCalendarPeriodsTest", calendarPeriods, actualPeriods);
    }
    
    @Test
    public void updateCalendarPeriodsTest() throws SQLException {
        // update the periods
        calendarPeriodDao.updateCalendarPeriods(calendarPeriods, updatedPeriods);

        // check whether the periods are successfully updated
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        updatedPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        Assert.assertEquals("updateCalendarPeriodsTest", updatedPeriods, actualPeriods);
    }

    @Test
    public void deleteCalendarPeriodsTest() throws SQLException {
        calendarPeriodDao.deleteCalendarPeriods(calendarPeriods);

        // are the periods deleted?
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        Assert.assertEquals("deleteCalendarPeriodsTest", 0, actualPeriods.size());
    }
}
