package be.ugent.blok2.daos.cascade;

import be.ugent.blok2.TestSharedMethods;
import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.daos.ILocationDao;
import be.ugent.blok2.daos.IPenaltyEventsDao;
import be.ugent.blok2.daos.db.ADB;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.Resources;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.penalty.Penalty;
import be.ugent.blok2.model.penalty.PenaltyEvent;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.users.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestCascadeInPenaltyEventDao {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IPenaltyEventsDao penaltyEventsDao;

    private PenaltyEvent testPenaltyEvent;

    private User testUser1;
    private User testUser2;

    private Location testLocation1;
    private Location testLocation2;

    private Penalty testPenalty1;
    private Penalty testPenalty2;

    @Before
    public void setup() throws SQLException {
        // Use test database
        TestSharedMethods.setupTestDaoDatabaseCredentials(accountDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(penaltyEventsDao);

        // Setup test objects
        testUser1 = TestSharedMethods.studentEmployeeTestUser();
        testUser2 = TestSharedMethods.employeeAdminTestUser();

        testLocation1 = TestSharedMethods.testLocation();
        testLocation2 = TestSharedMethods.testLocation2();

        Map<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.DUTCH, "Dit is een test omschrijving van een penalty event met code 0");
        descriptions.put(Language.ENGLISH, "This is a test description of a penalty event with code 0");
        testPenaltyEvent = new PenaltyEvent(0, 10, true, descriptions);

        // Note: the received amount of points are 10 and 20, not testPenaltyEvent.getCode()
        // because when the penalties are retrieved from the penaltyEventDao, the list will
        // be sorted by received points before asserting, if they would be equal we can't sort
        // on the points and be sure about the equality of the actual and expected list.
        testPenalty1 = new Penalty(testUser1.getAugentID(), testPenaltyEvent.getCode(), CustomDate.now(), CustomDate.now(), testLocation1.getName(), 10);
        testPenalty2 = new Penalty(testUser2.getAugentID(), testPenaltyEvent.getCode(), CustomDate.now(), CustomDate.now(), testLocation2.getName(), 20);


        // Add test objects to database
        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);

        accountDao.directlyAddUser(testUser1);
        accountDao.directlyAddUser(testUser2);

        penaltyEventsDao.addPenaltyEvent(testPenaltyEvent);
        penaltyEventsDao.addPenalty(testPenalty1);
        penaltyEventsDao.addPenalty(testPenalty2);
    }

    @After
    public void cleanup() throws SQLException {
        // Remove test objects from database
        // Note, I am not relying on the cascade because that's
        // what we are testing here in this class ...
        penaltyEventsDao.deletePenalty(testPenalty2);
        penaltyEventsDao.deletePenalty(testPenalty1);
        penaltyEventsDao.deletePenaltyEvent(testPenaltyEvent.getCode());

        accountDao.deleteUser(testUser2.getAugentID());
        accountDao.deleteUser(testUser1.getAugentID());

        // ... okay, cascade is assumed to be okay for the lockers here... (but it is)
        locationDao.deleteLocation(testLocation2.getName());
        locationDao.deleteLocation(testLocation1.getName());

        // Use regular database
        accountDao.useDefaultDatabaseConnection();
        locationDao.useDefaultDatabaseConnection();
        penaltyEventsDao.useDefaultDatabaseConnection();
    }

    @Test
    public void updatePenaltyEventWithoutNeedOfCascade() throws SQLException{
        updatePenaltyEventWithoutChangeInFK(testPenaltyEvent);

        // PENALTY_EVENTS and PENALTY_DESCRIPTIONS updated?
        penaltyEventsDao.updatePenaltyEvent(testPenaltyEvent.getCode(), testPenaltyEvent);
        PenaltyEvent p = penaltyEventsDao.getPenaltyEvent(testPenaltyEvent.getCode());
        Assert.assertEquals("updatePenaltyEventWithoutNeedOfCascade, penalty event", testPenaltyEvent, p);

        // PENALTY_BOOK updated?
        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByEventCode(testPenaltyEvent.getCode());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updatePenaltyEventWithoutNeedOfCascade, penalty book",
                expectedPenalties, penalties);
    }

    @Test
    public void updatePenaltyEventWithNeedOfCascade() throws SQLException{
        updatePenaltyEventWithoutChangeInFK(testPenaltyEvent);
        int oldCode = testPenaltyEvent.getCode();
        testPenaltyEvent.setCode(999);
        penaltyEventsDao.updatePenaltyEvent(oldCode, testPenaltyEvent);

        // old penalty event removed?
        PenaltyEvent p = penaltyEventsDao.getPenaltyEvent(oldCode);
        Assert.assertNull("updateLocationWithCascadeNeededTest, old location must be deleted", p);

        // PENALTY_EVENTS and PENALTY_DESCRIPTIONS updated?
        p = penaltyEventsDao.getPenaltyEvent(testPenaltyEvent.getCode());
        Assert.assertEquals("updatePenaltyEventWithoutNeedOfCascade, penalty event", testPenaltyEvent, p);

        // PENALTY_BOOK updated?
        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByEventCode(testPenaltyEvent.getCode());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        testPenalty1.setEventCode(testPenaltyEvent.getCode());
        testPenalty2.setEventCode(testPenaltyEvent.getCode());

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updatePenaltyEventWithoutNeedOfCascade, penalty book",
                expectedPenalties, penalties);
    }

    @Test
    public void deletePenaltyEventTest() throws SQLException {
        penaltyEventsDao.deletePenaltyEvent(testPenaltyEvent.getCode());
        PenaltyEvent p = penaltyEventsDao.getPenaltyEvent(testPenaltyEvent.getCode());
        Assert.assertNull("deletePenaltyEventTest, penalty event must be deleted", p);

        Assert.assertEquals("deletePenaltyEventTest, descriptions need to be removed",
                0, countDescriptionsOfPenaltyEvent(testPenaltyEvent.getCode()));

        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByEventCode(testPenaltyEvent.getCode());
        Assert.assertEquals("deletePenaltyEventTest, penalties", 0, penalties.size());
    }

    private void updatePenaltyEventWithoutChangeInFK(PenaltyEvent penaltyEvent) {
        penaltyEvent.setPoints(penaltyEvent.getPoints() * 2);
        penaltyEvent.setPublicAccessible(!penaltyEvent.getPublicAccessible());

        Map<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.ENGLISH, "This is a changed descriptions for the penalty event");
        descriptions.put(Language.DUTCH, "Dit is een aangepaste omschrijving van het penalty event");
        penaltyEvent.setDescriptions(descriptions);
    }

    private int countDescriptionsOfPenaltyEvent(int code) throws SQLException {
        try (Connection conn = ADB.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources
                    .databaseProperties.getString("count_descriptions_of_penalty_events"));
            pstmt.setInt(1, code);

            ResultSet rs = pstmt.executeQuery();
            rs.next(); // will always be true, it's a count. If a problem would occur, a SQLException will be thrown
            return rs.getInt(1);
        }
    }
}
