package blok2.daos;

import blok2.TestSharedMethods;
import blok2.model.Authority;
import blok2.model.calendar.CalendarPeriodForLockers;
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
public class TestDBCalendarPeriodForLockersDao {

    @Autowired
    private ICalendarPeriodForLockersDao calendarPeriodForLockersDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    IAuthorityDao authorityDao;

    private Location testLocation;
    private Authority authority;
    private List<CalendarPeriodForLockers> calendarPeriodsForLockers;

    // the reason for making this an attribute of the class
    // is to make sure the values are deleted when something
    // goes wrong
    private List<CalendarPeriodForLockers> updatedPeriodsForLockers;

    @Before
    public void setup() throws SQLException {
        // Use test database
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(calendarPeriodForLockersDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(authorityDao);

        // Setup test objects
        authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testLocation = TestSharedMethods.testLocation(authority.getAuthorityId());
        calendarPeriodsForLockers = TestSharedMethods.testCalendarPeriodsForLockers(testLocation);
        updatedPeriodsForLockers = TestSharedMethods.testCalendarPeriodsForLockersButUpdated(testLocation);

        // Add test objects to database
        locationDao.addLocation(testLocation);
        calendarPeriodForLockersDao.addCalendarPeriodsForLockers(calendarPeriodsForLockers);
    }

    @After
    public void cleanup() throws SQLException {
        // Remove test objects from database
        calendarPeriodForLockersDao.deleteCalendarPeriodsForLockers(updatedPeriodsForLockers); // in case this would be necessary
        calendarPeriodForLockersDao.deleteCalendarPeriodsForLockers(calendarPeriodsForLockers);
        locationDao.deleteLocation(testLocation.getName());
        authorityDao.deleteAuthority(authority.getAuthorityId());

        // Use regular database
        calendarPeriodForLockersDao.useDefaultDatabaseConnection();
        locationDao.useDefaultDatabaseConnection();
        authorityDao.useDefaultDatabaseConnection();
    }

    @Test
    public void addCalendarPeriodsForLockersTest() throws SQLException {
        List<CalendarPeriodForLockers> actualPeriods = calendarPeriodForLockersDao
                .getCalendarPeriodsForLockersOfLocation(testLocation.getName());
        actualPeriods.sort(Comparator.comparing(CalendarPeriodForLockers::toString));
        calendarPeriodsForLockers.sort(Comparator.comparing(CalendarPeriodForLockers::toString));

        Assert.assertEquals("addCalendarPeriodsForLockersTest", calendarPeriodsForLockers, actualPeriods);
    }

    @Test
    public void updateCalendarPeriodsForLockersTest() throws SQLException {
        // update the periods
        calendarPeriodForLockersDao.updateCalendarPeriodsForLockers(calendarPeriodsForLockers, updatedPeriodsForLockers);

        // check whether the periods are successfully updated
        List<CalendarPeriodForLockers> actualPeriods = calendarPeriodForLockersDao
                .getCalendarPeriodsForLockersOfLocation(testLocation.getName());
        actualPeriods.sort(Comparator.comparing(CalendarPeriodForLockers::toString));
        updatedPeriodsForLockers.sort(Comparator.comparing(CalendarPeriodForLockers::toString));
        Assert.assertEquals("updateCalendarPeriodsForLockersTest", updatedPeriodsForLockers, actualPeriods);
    }

    @Test
    public void deleteCalendarPeriodsForLockersTest() throws SQLException {
        calendarPeriodForLockersDao.deleteCalendarPeriodsForLockers(calendarPeriodsForLockers);

        // are the periods deleted?
        List<CalendarPeriodForLockers> actualPeriods = calendarPeriodForLockersDao
                .getCalendarPeriodsForLockersOfLocation(testLocation.getName());
        Assert.assertEquals("deleteCalendarPeriodsForLockersTest", 0, actualPeriods.size());
    }
}
