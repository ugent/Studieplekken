package blok2;

import blok2.daos.IUserDao;
import blok2.daos.IAuthorityDao;
import blok2.daos.ITimeslotDAO;
import blok2.helpers.Institution;
import blok2.helpers.Pair;
import blok2.helpers.TimeException;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.LocationTag;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.junit.Assert;
import org.threeten.extra.YearWeek;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestSharedMethods {

    public static Location testLocation(Authority authority, Building building) {
        Location testLocation = new Location();
        testLocation.setName("TestLocation");
        testLocation.setNumberOfSeats(50);
        testLocation.setNumberOfLockers(15);
        testLocation.setImageUrl("https://example.com/image.jpg");
        testLocation.setAuthority(authority);
        testLocation.setBuilding(building);
        testLocation.setForGroup(false);
        return testLocation;
    }

    public static Location testLocation1Seat(Authority authority, Building building) {
        Location testLocation = new Location();
        testLocation.setName("TestLocation2");
        testLocation.setNumberOfSeats(1);
        testLocation.setNumberOfLockers(1);
        testLocation.setImageUrl("https://example.com/image.jpg");
        testLocation.setAuthority(authority);
        testLocation.setBuilding(building);
        testLocation.setForGroup(false);
        return testLocation;
    }

    public static Location testLocation2(Authority authority, Building building) {
        Location testLocation2 = new Location();
        testLocation2.setName("SecondTestLocation");
        testLocation2.setNumberOfSeats(100);
        testLocation2.setNumberOfLockers(10);
        testLocation2.setImageUrl("https://example.com/picture.png");
        testLocation2.setAuthority(authority);
        testLocation2.setBuilding(building);
        testLocation2.setForGroup(true);
        return testLocation2;
    }

    public static Location testLocation3(Authority authority, Building building) {
        Location testLocation3 = new Location();
        testLocation3.setName("ThirdTestLocation");
        testLocation3.setNumberOfSeats(25);
        testLocation3.setNumberOfLockers(5);
        testLocation3.setImageUrl("https://example.com/picture.png");
        testLocation3.setAuthority(authority);
        testLocation3.setBuilding(building);
        testLocation3.setForGroup(true);
        return testLocation3;
    }

    public static Building testBuilding() {
        Building testBuilding = new Building();
        testBuilding.setName("TestBuilding");
        testBuilding.setAddress("TestStreet 123");
        return testBuilding;
    }

    public static LocationTag testTag() {
        return new LocationTag(1,
                "Stille ruimte",
                "Silent space");
    }

    public static LocationTag testTag2() {
        return new LocationTag(2,
                "Geschikt voor vergaderingen",
                "Suitable for meetings");
    }

    public static LocationTag testTag3() {
        return new LocationTag(3,
                "Geschikt voor invaliden",
                "Suitable for the less-abled");
    }

    public static Authority insertTestAuthority(IAuthorityDao authorityDao) {
        return insertTestAuthority("Test Authority", "a test description", authorityDao);
    }

    public static Authority insertTestAuthority2(IAuthorityDao authorityDao) {
        return insertTestAuthority("Second Test Authority", "second test description", authorityDao);
    }

    public static Authority insertTestAuthority(String name, String description, IAuthorityDao authorityDao) {
        Authority authority = new Authority();
        authority.setAuthorityName(name);
        authority.setDescription(description);
        authority = authorityDao.addAuthority(authority);
        Authority dbAuthority = authorityDao.getAuthorityByAuthorityId(authority.getAuthorityId());
        Assert.assertEquals("insertTestAuthority: Failed to insert Test Authority", authority, dbAuthority);
        return authority;
    }

    public static User adminTestUser() {
        return adminTestUser("admin");
    }

    public static User adminTestUser(String s) {
        User user = new User();
        user.setLastName("Added User");
        user.setFirstName("First");
        user.setMail(s+"@ugent.be");
        user.setPassword("first_password");
        user.setInstitution(Institution.UGent);
        user.setUserId(s);
        user.setAdmin(true);
        return user;
    }

    public static User studentTestUser() {
        return studentTestUser("002");
    }

    public static User studentTestUser(String id) {
        User user = new User();
        user.setLastName("Added User");
        user.setFirstName("Second");
        user.setMail(id + "@ugent.be");
        user.setPassword("second_password");
        user.setInstitution(Institution.UGent);
        user.setUserId(id);
        user.setAdmin(false);
        return user;
    }

    public static void addTestUsers(IUserDao userDao, User... users) {
        for (User u : users) {
            userDao.addUser(u);
            User r = userDao.getUserById(u.getUserId()); // retrieved user
            Assert.assertEquals("addTestUsers, setup test user failed", u, r);
        }
    }

    public static void removeTestUsers(IUserDao userDao, User... users) {
        for (User u : users) {
            userDao.deleteUser(u.getUserId());
            try {
                userDao.getUserById(u.getUserId());
                Assert.fail("cleanup test user should throw NoSuchDatabaseObjectException after deletion");
            } catch (NoSuchDatabaseObjectException e) {
                Assert.assertTrue(true);
            }
        }
    }

    public static  List<Timeslot> testCalendarPeriods(Location location) {

        List<Timeslot> timeslots = new ArrayList<>();
        YearWeek date = YearWeek.now();

        for (int i = -1; i < 1; i++) {
            LocalDateTime reservableFrom = LocalDateTime.now().withDayOfMonth(1);


            LocalTime mondayStartTime = LocalTime.of(8,0);
            LocalTime mondayEndTime = LocalTime.of(16,30);
            Timeslot timeslotMonday = new Timeslot(null, date.atDay(DayOfWeek.MONDAY), mondayStartTime, mondayEndTime, true, reservableFrom, location.getNumberOfSeats(), location.getLocationId());


            LocalTime fridayStartTime = LocalTime.of(12,0);
            LocalTime fridayEndTime = LocalTime.of(20, 0);
            Timeslot timeslotFriday = new Timeslot(null, date.atDay(DayOfWeek.FRIDAY), fridayStartTime, fridayEndTime, true, reservableFrom, location.getNumberOfSeats(), location.getLocationId());

            timeslots.add(timeslotMonday);
            timeslots.add(timeslotFriday);
        }
        return timeslots;
    }

    /**
     * Create timeslots that are completely in the past
     * @param location the location for which to create the timeslots
     * @return a timeslots in the past
     */
    public static List<Timeslot> pastCalendarPeriods(Location location) {
        YearWeek past = YearWeek.now().minusWeeks(1);
        LocalDateTime now = LocalDateTime.now();



        LocalTime startTime = now.toLocalTime().plusMinutes(1);
        LocalTime endTime = now.toLocalTime().plusMinutes(2);
        Timeslot timeslot = new Timeslot(null, past.atDay(DayOfWeek.MONDAY), startTime, endTime, true, now.minusDays(3), location.getNumberOfSeats(), location.getLocationId());

        return Collections.singletonList(timeslot);
    }

    /**
     * Create timeslots that is completely in the future
     * @param location the location for which to create the timeslots
     * @return a list of timeslots in the future
     */
    public static List<Timeslot> upcomingCalendarPeriods(Location location) {
        YearWeek past = YearWeek.now().plusWeeks(3);
        LocalDateTime now = LocalDateTime.now();


        LocalTime startTime = now.toLocalTime().plusMinutes(1);
        LocalTime endTime = now.toLocalTime().plusMinutes(2);
        Timeslot timeslot = new Timeslot(null, past.atDay(now.getDayOfWeek()), startTime, endTime, true, now.minusDays(3), location.getNumberOfSeats(), location.getLocationId());

        return Collections.singletonList(timeslot);

    }

    /**
     * Create a timeslots that is active (today is between start and end date) but not during the active hours
     * @param location the location for which to create the period
     * @return a list of timeslots that are active, but outside the hours
     */
    public static List<Timeslot> activeCalendarPeriodsOutsideHours(Location location) throws TimeException {
        LocalDateTime now = LocalDateTime.now();

        YearWeek past = YearWeek.now();

        if (LocalTime.now().isAfter(LocalTime.of(23, 59)) || LocalTime.now().isBefore(LocalTime.of(0, 1)))
            throw new TimeException("Impossible to create active calendar period at this time");


        LocalTime startTime = now.toLocalTime().plusMinutes(1);
        LocalTime endTime = now.toLocalTime().plusMinutes(2);
        Timeslot timeslot = new Timeslot(null, past.atDay(now.getDayOfWeek()), startTime, endTime, true, now.minusDays(3), location.getNumberOfSeats(), location.getLocationId());




        return Collections.singletonList(timeslot);
    }

    /**
     * Create a list of timeslots that is active (today is between start and end date) and during the active hours
     * @param location the location for which to create the period
     * @return a list of timeslots that is active and within hours
     */
    public static List<Timeslot> activeCalendarPeriodsInsideHours(Location location) throws TimeException {

        LocalDateTime now = LocalDateTime.now();

        YearWeek past = YearWeek.now();

        if (LocalTime.now().isAfter(LocalTime.of(23, 59)) || LocalTime.now().isBefore(LocalTime.of(0, 1)))
            throw new TimeException("Impossible to create active calendar period at this time");


        LocalTime startTime = now.toLocalTime().minusMinutes(1);
        LocalTime endTime = now.toLocalTime().plusMinutes(1);
        Timeslot timeslot = new Timeslot(null, past.atDay(now.getDayOfWeek()), startTime, endTime, true, now.minusDays(3), location.getNumberOfSeats(), location.getLocationId());



        return Collections.singletonList(timeslot);
    }

}
