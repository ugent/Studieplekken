package be.ugent.blok2.daos;

import be.ugent.blok2.TestSharedMethods;
import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.reservables.Locker;
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
import java.util.Collection;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestDBLocationDao {

    @Autowired
    private ILocationDao locationDao;

    private Location testLocation;

    @Before
    public void setup() throws SQLException {
        // Use test database
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationDao);

        // Setup test objects
        testLocation = TestSharedMethods.testLocation();

        // Add test objects to database
        locationDao.addLocation(testLocation);
    }

    @After
    public void cleanup() throws SQLException {
        // Remove test objects from database
        locationDao.deleteLocation(testLocation.getName());

        // Use regular database
        locationDao.useDefaultDatabaseConnection();
    }

    @Test
    public void addLocationTest() throws SQLException {
        Location l = locationDao.getLocation(testLocation.getName());
        Assert.assertEquals("addLocation", testLocation, l);

        locationDao.deleteLocation(testLocation.getName());
        l = locationDao.getLocation(testLocation.getName());
        Assert.assertNull("addLocation, remove added test location", l);
    }

    @Test
    public void changeLocationTest() throws SQLException {
        // TODO: test cascade
    }

    @Test
    public void deleteLockersTest() throws SQLException {
        // TODO: test cascade
    }

    /*
    * getCalendarDays(), addCalendarDays() and deleteCalendarDays will be tested
    * */
    @Test
    public void calendarDaysTest() throws SQLException {
        Calendar calendar = TestSharedMethods.testCalendar();
        Collection<Day> calendarDays = calendar.getDays();

        locationDao.addCalendarDays(testLocation.getName(), calendar);

        Collection<Day> retrievedCalendarDays = locationDao.getCalendarDays(testLocation.getName());
        Assert.assertArrayEquals("calendarDaysTest, retrieved calendar days", calendarDays.toArray(), retrievedCalendarDays.toArray());

        locationDao.deleteCalendarDays(testLocation.getName(), "2020-01-01T00:00:00", "2020-01-05T00:00:00");
        retrievedCalendarDays = locationDao.getCalendarDays(testLocation.getName());
        Assert.assertArrayEquals("calendarDaysTest, deleted calendar days", new Day[]{}, retrievedCalendarDays.toArray());
    }

    @Test
    public void lockersTest() throws SQLException {
        Collection<Locker> lockers = locationDao.getLockers(testLocation.getName());
        Assert.assertEquals("lockersTest, check size getLockers"
                , testLocation.getNumberOfLockers(), lockers.size());

        if (testLocation.getNumberOfLockers() > 0) {
            for (Locker l : lockers) {
                Assert.assertEquals("lockersTest, check location of each locker"
                        , testLocation, l.getLocation());
            }
        }
    }
}
