package be.ugent.blok2;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.daos.IDao;
import be.ugent.blok2.helpers.Institution;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.Resources;
import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.date.Time;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.model.reservables.Location;
import org.junit.Assert;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collection;
import java.util.ResourceBundle;

public class TestSharedMethods {

    private static final ResourceBundle applicationProperties = Resources.applicationProperties;

    public static void setupTestDaoDatabaseCredentials(IDao dao) {
        dao.setDatabaseConnectionUrl(applicationProperties.getString("test_db_url"));
        dao.setDatabaseCredentials(
                applicationProperties.getString("test_db_user"),
                applicationProperties.getString("test_db_password")
        );
    }

    public static Location testLocation() {
        // setup test location objects
        CustomDate startPeriodLockers = new CustomDate(1970, 1, 1, 9, 0, 0);
        CustomDate endPeriodLockers = new CustomDate(1970, 1, 31, 17, 0, 0);

        Location testLocation = new Location();
        testLocation.setName("Test Location");
        testLocation.setAddress("Test street, 10");
        testLocation.setNumberOfSeats(50);
        testLocation.setNumberOfLockers(15);
        testLocation.setMapsFrame("Test Google Maps frame");
        testLocation.getDescriptions().put(Language.DUTCH, "Dit is een testlocatie.");
        testLocation.getDescriptions().put(Language.ENGLISH, "This is a test location.");
        testLocation.setImageUrl("https://example.com/image.jpg");
        testLocation.setStartPeriodLockers(startPeriodLockers);
        testLocation.setEndPeriodLockers(endPeriodLockers);

        return testLocation;
    }

    public static Location testLocation2() {
        Location testLocation2 = new Location();
        testLocation2.setName("Second Test Location");
        testLocation2.setAddress("Second Test street, 20");
        testLocation2.setNumberOfSeats(100);
        testLocation2.setNumberOfLockers(10);
        testLocation2.setMapsFrame("Second Test Google Maps frame");
        testLocation2.getDescriptions().put(Language.DUTCH, "Dit is een tweede testlocatie.");
        testLocation2.getDescriptions().put(Language.ENGLISH, "This is a second test location.");
        testLocation2.setImageUrl("https://example.com/picture.png");
        return testLocation2;
    }

    public static User employeeAdminTestUser() {
        User user = new User();
        user.setLastName("Added User");
        user.setFirstName("First");
        user.setMail("First.AddedUser@ugent.be");
        user.setPassword((new BCryptPasswordEncoder()).encode("first_password"));
        user.setInstitution(Institution.UGent);
        user.setAugentID("001");
        user.setRoles(new Role[]{Role.ADMIN, Role.EMPLOYEE});
        return user;
    }

    public static User studentEmployeeTestUser() {
        User user = new User();
        user.setLastName("Added User");
        user.setFirstName("Second");
        user.setMail("Second.AddedUser@ugent.be");
        user.setPassword((new BCryptPasswordEncoder()).encode("second_password"));
        user.setInstitution(Institution.UGent);
        user.setAugentID("002");
        user.setRoles(new Role[]{Role.STUDENT, Role.EMPLOYEE});
        return user;
    }

    public static void addTestUsers(IAccountDao accountDao, User... users) {
        for (User u : users) {
            accountDao.directlyAddUser(u);
            User r = accountDao.getUserById(u.getAugentID()); // retrieved user
            Assert.assertEquals("addTestUsers, setup test user failed", u, r);
        }
    }

    public static void removeTestUsers(IAccountDao accountDao, User... users) {
        for (User u : users) {
            accountDao.removeUserById(u.getAugentID());
            User r = accountDao.getUserById(u.getAugentID());
            Assert.assertNull("removeTestUsers, cleanup test user failed", r);
        }
    }

    public static Calendar testCalendar() {
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
        return calendar;
    }
}
