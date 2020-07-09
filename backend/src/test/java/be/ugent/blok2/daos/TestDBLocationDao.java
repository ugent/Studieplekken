package be.ugent.blok2.daos;

import be.ugent.blok2.TestSharedMethods;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.Resources;
import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.date.Time;
import be.ugent.blok2.reservables.Location;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

/**
 * Note: the test that combines scanner users with locations, is to be found in TestScannerLocation.java
 */

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestDBLocationDao {

    @Autowired
    private ILocationDao locationDao;

    private Location testLocation;

    @Before
    public void setup() {
        // Change database credentials for used daos
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationDao);

        // setup test location objects
        // (reason for static function: avoid duplicate code, TestScannerLocation.java uses the same method)
        testLocation = TestSharedMethods.setupTestLocation();
    }

    @After
    public void cleanup() {
        locationDao.useDefaultDatabaseConnection();
    }

    @Test
    public void addLocationTest() {
        locationDao.addLocation(testLocation);
        Location l = locationDao.getLocationWithoutLockersAndCalendar(testLocation.getName());
        Assert.assertEquals("addLocation", testLocation, l);

        locationDao.deleteLocation(testLocation.getName());
        l = locationDao.getLocation(testLocation.getName());
        Assert.assertNull("addLocation, remove added test location", l);
    }

    @Test
    public void changeLocationTest() {
        locationDao.addLocation(testLocation);

        Location changedTestLocation = testLocation.clone();
        changedTestLocation.setName("Changed Test Location");

        locationDao.changeLocation(testLocation.getName(), changedTestLocation);
        Location location = locationDao.getLocationWithoutLockersAndCalendar(changedTestLocation.getName());
        Assert.assertEquals("changeLocationTest, fetch location by changed name", changedTestLocation, location);

        location = locationDao.getLocationWithoutLockersAndCalendar(testLocation.getName());
        Assert.assertNull("changeLocationTest, old location name may not have an entry", location);

        locationDao.deleteLocation(changedTestLocation.getName());
    }

    @Test
    public void addLockersTest() {
        locationDao.addLocation(testLocation);
        Location expectedLocation = testLocation.clone();
        int prev_n = expectedLocation.getNumberOfLockers();

        // test adding positive amount of lockers
        int n = 10;
        expectedLocation.setNumberOfLockers(prev_n + n);
        locationDao.addLockers(testLocation.getName(), n);
        Location location = locationDao.getLocationWithoutLockersAndCalendar(testLocation.getName());
        Assert.assertEquals("addLockersTest, added lockers", expectedLocation, location);

        // test adding negative amount of lockers
        int _n = -5;
        expectedLocation.setNumberOfLockers(prev_n + n + _n);
        locationDao.addLockers(testLocation.getName(), _n);
        location = locationDao.getLocationWithoutLockersAndCalendar(testLocation.getName());
        Assert.assertEquals("addLocker, added negative amount of lockers", expectedLocation, location);

        // TODO: reserve lockers and expect SQLException

        locationDao.deleteLocation(testLocation.getName());
    }

    @Test
    public void deleteLockersTest() {
        locationDao.addLocation(testLocation);
        Location expectedLocation = testLocation.clone();
        int prev_n = expectedLocation.getNumberOfLockers();

        int n = 5;
        expectedLocation.setNumberOfLockers(prev_n - n);
        locationDao.deleteLockers(testLocation.getName(), prev_n - n);
        Location location = locationDao.getLocationWithoutLockersAndCalendar(testLocation.getName());
        Assert.assertEquals("deleteLockersTest", expectedLocation, location);

        locationDao.deleteLocation(testLocation.getName());
    }

    /*
    * getCalendarDays(), addCalendarDays() and deleteCalendarDays will be tested
    * */
    @Test
    public void calendarDaysTest() {
        locationDao.addLocation(testLocation);

        Calendar calendar = new Calendar();
        Collection<Day> calendarDays = calendar.getDays();
        for (int i = 1; i <= 5; i++) {
            Day d = new Day();
            d.setDate(CustomDate.parseString("2020-01-0" + i + "T00:00:00"));
            d.setOpeningHour(new Time(9, 0, 0));
            d.setClosingHour(new Time(17, 0, 0));
            d.setOpenForReservationDate(CustomDate.parseString("2019-12-31T09:00:00"));
            calendarDays.add(d);
        }

        locationDao.addCalendarDays(testLocation.getName(), calendar);

        Collection<Day> retrievedCalendarDays = locationDao.getCalendarDays(testLocation.getName());
        Assert.assertArrayEquals("calendarDaysTest, retrieved calendar days", calendarDays.toArray(), retrievedCalendarDays.toArray());

        locationDao.deleteCalendarDays(testLocation.getName(), "2020-01-01T00:00:00", "2020-01-05T00:00:00");
        retrievedCalendarDays = locationDao.getCalendarDays(testLocation.getName());
        Assert.assertArrayEquals("calendarDaysTest, deleted calendar days", new Day[]{}, retrievedCalendarDays.toArray());

        locationDao.deleteLocation(testLocation.getName());
    }
}
