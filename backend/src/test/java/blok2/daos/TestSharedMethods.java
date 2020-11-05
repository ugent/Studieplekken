package blok2.daos;

import blok2.helpers.Institution;
import blok2.helpers.date.CustomDate;
import blok2.model.Authority;
import blok2.model.LocationTag;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.CalendarPeriodForLockers;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.junit.Assert;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestSharedMethods {

    public static Location testLocation(Authority authority) {
        Location testLocation = new Location();
        testLocation.setName("Test Location");
        testLocation.setAddress("Test street, 10");
        testLocation.setNumberOfSeats(50);
        testLocation.setNumberOfLockers(15);
        testLocation.setImageUrl("https://example.com/image.jpg");
        testLocation.setAuthority(authority);
        testLocation.setForGroup(false);
        return testLocation;
    }

    public static Location testLocation1Seat(Authority authority) {
        Location testLocation = new Location();
        testLocation.setName("Test Location 2");
        testLocation.setAddress("Test street, 10");
        testLocation.setNumberOfSeats(1);
        testLocation.setNumberOfLockers(15);
        testLocation.setImageUrl("https://example.com/image.jpg");
        testLocation.setAuthority(authority);

        return testLocation;
    }

    public static Location testLocation2(Authority authority) {
        Location testLocation2 = new Location();
        testLocation2.setName("Second Test Location");
        testLocation2.setAddress("Second Test street, 20");
        testLocation2.setNumberOfSeats(100);
        testLocation2.setNumberOfLockers(10);
        testLocation2.setImageUrl("https://example.com/picture.png");
        testLocation2.setAuthority(authority);
        testLocation2.setForGroup(true);
        return testLocation2;
    }

    public static Location testLocation3(Authority authority) {
        Location testLocation3 = new Location();
        testLocation3.setName("Third Test Location");
        testLocation3.setAddress("Third Test street, 30");
        testLocation3.setNumberOfSeats(25);
        testLocation3.setNumberOfLockers(5);
        testLocation3.setImageUrl("https://example.com/picture.png");
        testLocation3.setAuthority(authority);
        testLocation3.setForGroup(true);
        return testLocation3;
    }

    public static LocationTag testTag() {
        LocationTag testTag = new LocationTag(1,
                "Stille ruimte",
                "Silent space");
        return testTag;
    }

    public static LocationTag testTag2() {
        LocationTag testTag = new LocationTag(2,
                "Geschikt voor vergaderingen",
                "Suitable for meetings");
        return testTag;
    }

    public static LocationTag testTag3() {
        LocationTag testTag = new LocationTag(3,
                "Geschikt voor invaliden",
                "Suitable for the less-abled");
        return testTag;
    }

    public static Authority insertTestAuthority(IAuthorityDao authorityDao) throws SQLException {
        return insertTestAuthority("Test Authority", "a test description", authorityDao);
    }

    public static Authority insertTestAuthority2(IAuthorityDao authorityDao) throws SQLException {
        return insertTestAuthority("Second Test Authority", "second test description", authorityDao);
    }

    public static Authority insertTestAuthority(String name, String description, IAuthorityDao authorityDao) throws SQLException {
        Authority authority = new Authority();
        authority.setAuthorityName(name);
        authority.setDescription(description);
        authority = authorityDao.addAuthority(authority);
        Authority dbAuthority = authorityDao.getAuthorityByAuthorityId(authority.getAuthorityId());
        Assert.assertEquals("insertTestAuthority: Failed to insert Test Authority", authority, dbAuthority);
        return authority;
    }

    public static User adminTestUser() {
        User user = new User();
        user.setLastName("Added User");
        user.setFirstName("First");
        user.setMail("First.AddedUser@ugent.be");
        user.setPassword("first_password");
        user.setInstitution(Institution.UGent);
        user.setAugentID("001");
        user.setAdmin(true);
        return user;
    }

    public static User studentTestUser() {
        User user = new User();
        user.setLastName("Added User");
        user.setFirstName("Second");
        user.setMail("Second.AddedUser@ugent.be");
        user.setPassword("second_password");
        user.setInstitution(Institution.UGent);
        user.setAugentID("002");
        user.setAdmin(false);
        return user;
    }

    public static void addTestUsers(IAccountDao accountDao, User... users) throws SQLException {
        for (User u : users) {
            accountDao.directlyAddUser(u);
            User r = accountDao.getUserById(u.getAugentID()); // retrieved user
            Assert.assertEquals("addTestUsers, setup test user failed", u, r);
        }
    }

    public static void removeTestUsers(IAccountDao accountDao, User... users) throws SQLException {
        for (User u : users) {
            accountDao.deleteUser(u.getAugentID());
            User r = accountDao.getUserById(u.getAugentID());
            Assert.assertNull("removeTestUsers, cleanup test user failed", r);
        }
    }

    public static List<CalendarPeriod> testCalendarPeriods(Location location) {
        List<CalendarPeriod> calendarPeriods = new ArrayList<>();

        CustomDate date = CustomDate.now();

        for (int i = 0; i < 2; i++) {
            CalendarPeriod period = new CalendarPeriod();
            period.setLocation(location);

            date.setDay(2 + 10 * i);
            period.setStartsAt(date.toDateString());

            date.setDay(4 + 10 * i);
            period.setEndsAt(date.toDateString());

            date.setHrs(9);
            date.setMin(0);
            period.setOpeningTime(date.toTimeWithoutSecondsString());

            date.setHrs(17);
            period.setClosingTime(date.toTimeWithoutSecondsString());

            date.setDay(1);
            period.setReservableFrom(date.toString());

            period.setReservable(true);
            period.setReservableTimeslotSize(30);


            calendarPeriods.add(period);
        }

        return calendarPeriods;
    }

    public static void addCalendarPeriods(ICalendarPeriodDao calendarPeriodDao, CalendarPeriod... periods) throws SQLException {
        calendarPeriodDao.addCalendarPeriods(Arrays.asList(periods));
    }

    public static List<CalendarPeriod> testCalendarPeriodsButUpdated(Location location) {
        List<CalendarPeriod> updatedPeriods = new ArrayList<>();
        for (CalendarPeriod calendarPeriod : testCalendarPeriods(location)) {
            updatedPeriods.add(calendarPeriod.clone());
        }

        for (int i = 0; i < updatedPeriods.size(); i++) {
            updatedPeriods.get(i).setStartsAt("1970-01-0" + i);
            updatedPeriods.get(i).setEndsAt("1970-01-1" + i);
            updatedPeriods.get(i).setOpeningTime("09:0" + i);
            updatedPeriods.get(i).setClosingTime("17:0" + i);
            updatedPeriods.get(i).setReservableFrom("1970-01-0" + i + "T09:00");
        }

        return updatedPeriods;
    }

    public static List<CalendarPeriodForLockers> testCalendarPeriodsForLockers(Location location) {
        List<CalendarPeriodForLockers> calendarPeriods = new ArrayList<>();

        CustomDate date = CustomDate.now();

        for (int i = 0; i < 2; i++) {
            CalendarPeriodForLockers period = new CalendarPeriodForLockers();
            period.setLocation(location);

            date.setDay(2 + 10 * i);
            period.setStartsAt(date.toDateString());

            date.setDay(4 + 10 * i);
            period.setEndsAt(date.toDateString());

            date.setDay(1);
            period.setReservableFrom(date.toString());

            calendarPeriods.add(period);
        }

        return calendarPeriods;
    }

    public static List<CalendarPeriodForLockers> testCalendarPeriodsForLockersButUpdated(Location location) {
        List<CalendarPeriodForLockers> updatedPeriods = new ArrayList<>();
        for (CalendarPeriodForLockers calendarPeriod : testCalendarPeriodsForLockers(location)) {
            updatedPeriods.add(calendarPeriod.clone());
        }

        for (int i = 0; i < updatedPeriods.size(); i++) {
            updatedPeriods.get(i).setStartsAt("1970-01-0" + i);
            updatedPeriods.get(i).setEndsAt("1970-01-1" + i);
            updatedPeriods.get(i).setReservableFrom("1970-01-0" + i + "T09:00");
        }

        return updatedPeriods;
    }
}
