package be.ugent.blok2.daos;

import be.ugent.blok2.TestSharedMethods;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.penalty.Penalty;
import be.ugent.blok2.model.penalty.PenaltyEvent;
import be.ugent.blok2.model.users.User;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestDBPenaltyEventsDao {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IPenaltyEventsDao penaltyEventsDao;

    private Map<Language, String> cancellingTooLateDescriptions;
    private Map<Language, String> notShowingUpDescriptions;
    private Map<Language, String> blacklistDescriptions;
    private Map<Language, String> testDescriptions;

    private PenaltyEvent cancellingTooLateEvent;
    private PenaltyEvent notShowingUpEvent;
    private PenaltyEvent blacklistEvent;
    private PenaltyEvent testEvent;

    private Location testLocation;
    private User testUser;

    @Before
    public void setup() {
        // Use test database
        TestSharedMethods.setupTestDaoDatabaseCredentials(accountDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(penaltyEventsDao);

        // Setup test objects
        cancellingTooLateDescriptions = new HashMap<>();
        cancellingTooLateDescriptions.put(Language.ENGLISH, "Cancelling too late.");
        cancellingTooLateDescriptions.put(Language.DUTCH, "Te laat annuleren.");
        cancellingTooLateEvent = new PenaltyEvent(16660, 30, true, cancellingTooLateDescriptions);

        notShowingUpDescriptions = new HashMap<>();
        notShowingUpDescriptions.put(Language.ENGLISH, "Not showing up at all.");
        notShowingUpDescriptions.put(Language.DUTCH, "Niet komen opdagen.");
        notShowingUpEvent = new PenaltyEvent(16661, 50, true, notShowingUpDescriptions);

        blacklistDescriptions = new HashMap<>();
        blacklistDescriptions.put(Language.ENGLISH, "Blacklist event.");
        blacklistDescriptions.put(Language.DUTCH, "Blacklist event.");
        blacklistEvent = new PenaltyEvent(16662, 100, true, blacklistDescriptions);

        testDescriptions = new HashMap<>();
        testDescriptions.put(Language.ENGLISH, "Test event.");
        testDescriptions.put(Language.DUTCH, "Test event.");
        testEvent = new PenaltyEvent(1, 10, true, testDescriptions);

        testLocation = TestSharedMethods.testLocation();
        testUser = TestSharedMethods.studentEmployeeTestUser();

        // Add test objects to database
        locationDao.addLocation(testLocation);
        penaltyEventsDao.addPenaltyEvent(testEvent);
        TestSharedMethods.addTestUsers(accountDao, testUser);
    }

    @After
    public void cleanup() {
        // Remove test objects from database
        TestSharedMethods.removeTestUsers(accountDao, testUser);
        penaltyEventsDao.deletePenaltyEvent(testEvent.getCode());
        locationDao.deleteLocation(testLocation.getName());

        // Use regular database
        penaltyEventsDao.useDefaultDatabaseConnection();
    }

    @Test
    public void permanentPenaltyEventsTest() {
        // These tests are supposed to be in the database
        PenaltyEvent test16660 = penaltyEventsDao.getPenaltyEvent(cancellingTooLateEvent.getCode());
        PenaltyEvent test16661 = penaltyEventsDao.getPenaltyEvent(notShowingUpEvent.getCode());
        PenaltyEvent test16662 = penaltyEventsDao.getPenaltyEvent(blacklistEvent.getCode());

        Assert.assertEquals("permanentEventsTest, 16660", cancellingTooLateEvent, test16660);
        Assert.assertEquals("permanentEventsTest, 16661", notShowingUpEvent, test16661);
        Assert.assertEquals("permanentEventsTest, 16662", blacklistEvent, test16662);
    }

    @Test
    public void addPenaltyEventTest() {
        PenaltyEvent retrievedTestEvent = penaltyEventsDao.getPenaltyEvent(testEvent.getCode());
        Assert.assertEquals("addPenaltyEventTest", testEvent, retrievedTestEvent);
    }

    @Test
    public void deleteAndAddPenaltyEventDescriptionTest() {
        // Not changing testEvent (although you could change it because the @Before creates a new testEvent for the next @Test)
        PenaltyEvent modifiablePenaltyEvent = testEvent.clone();

        // Delete the English description
        modifiablePenaltyEvent.getDescriptions().remove(Language.ENGLISH);
        penaltyEventsDao.deleteDescription(modifiablePenaltyEvent.getCode(), Language.ENGLISH);

        // Check whether the description has been deleted correctly
        PenaltyEvent retrievedModifiedTestEvent = penaltyEventsDao.getPenaltyEvent(modifiablePenaltyEvent.getCode());
        Assert.assertEquals("deleteAndAddPenaltyEventDescriptionsTest, remove description",
                modifiablePenaltyEvent, retrievedModifiedTestEvent);

        // Add the English description again
        penaltyEventsDao.addDescription(testEvent.getCode(), Language.ENGLISH,
                testEvent.getDescriptions().get(Language.ENGLISH));

        // Check whether the description has been added correctly
        retrievedModifiedTestEvent = penaltyEventsDao.getPenaltyEvent(modifiablePenaltyEvent.getCode());
        Assert.assertEquals("deleteAndAddPenaltyEventDescriptionsTest, add description",
                testEvent, retrievedModifiedTestEvent);
    }

    @Test
    public void penaltyBookTest() {
        // setup some test data
        CustomDate thisDayAMonthEarlier = CustomDate.now();
        thisDayAMonthEarlier.setMonth(thisDayAMonthEarlier.getMonth() == 1 ? 12 : thisDayAMonthEarlier.getMonth() - 1);

        Penalty penalty = new Penalty(testUser.getAugentID(), testEvent.getCode()
                , CustomDate.now(), CustomDate.today(), testLocation.getName(), testEvent.getPoints());
        Penalty fatalPenalty = new Penalty(testUser.getAugentID(), blacklistEvent.getCode(), CustomDate.now()
                , new CustomDate(1970, 1, 1), testLocation.getName(), blacklistEvent.getPoints());
        Penalty penaltyLastMonth = new Penalty(testUser.getAugentID(), testEvent.getCode()
                , thisDayAMonthEarlier, thisDayAMonthEarlier, testLocation.getName(), testEvent.getPoints());

        User modifiableUser = testUser.clone();

        // Add penalty
        penaltyEventsDao.addPenalty(penalty);
        List<Penalty> penalties = penaltyEventsDao.getPenalties(testUser.getAugentID());
        Assert.assertEquals("penaltyBookTests, added one penalty", 1, penalties.size());
        Assert.assertEquals("penaltyBookTests, added one penalty", penalty, penalties.get(0));

        // Is the corresponding user updated correctly?
        modifiableUser.setPenaltyPoints(penalty.getReceivedPoints());
        User retrievedUser = accountDao.getUserById(testUser.getAugentID());
        Assert.assertEquals("penaltyBookTests, added one penalty", modifiableUser, retrievedUser);

        // Delete penalty
        penaltyEventsDao.deletePenalty(penalty);
        penalties = penaltyEventsDao.getPenalties(testUser.getAugentID());
        Assert.assertEquals("penaltyBookTests, deleted penalty", 0, penalties.size());

        // Is the corresponding user updated correctly?
        modifiableUser.setPenaltyPoints(0);
        retrievedUser = accountDao.getUserById(testUser.getAugentID());
        Assert.assertEquals("penaltyBookTests, deleted penalty", modifiableUser, retrievedUser);

        // Add a penalty from last month
        penaltyEventsDao.addPenalty(penaltyLastMonth);
        penalties = penaltyEventsDao.getPenalties(testUser.getAugentID());
        Assert.assertEquals("penaltyBookTests, added penalty from last month", 1, penalties.size());
        Assert.assertEquals("penaltyBookTests, added penalty from last month"
                , penaltyLastMonth, penalties.get(0));

        // Is the corresponding user updated correctly?
        retrievedUser = accountDao.getUserById(testUser.getAugentID());
        Assert.assertTrue("penaltyBookTests, added penalty from last month, points should decrease over time"
                , retrievedUser.getPenaltyPoints() < penaltyLastMonth.getReceivedPoints() &&
                retrievedUser.getPenaltyPoints() > 0);

        penaltyEventsDao.deletePenalty(penaltyLastMonth);

        // Add a blacklist event from ages ago (blacklist events may not decrease over time)
        penaltyEventsDao.addPenalty(fatalPenalty);
        penalties = penaltyEventsDao.getPenalties(testUser.getAugentID());
        Assert.assertEquals("penaltyBookTests, added blacklist event from ages ago, may not decrease over time"
                , 1, penalties.size());
        Assert.assertEquals("penaltyBookTests, added blacklist event from ages ago, may not decrease over time"
                , fatalPenalty, penalties.get(0));

        // Is the corresponding user updated correctly?
        retrievedUser = accountDao.getUserById(testUser.getAugentID());
        modifiableUser.setPenaltyPoints(fatalPenalty.getReceivedPoints());
        Assert.assertEquals("penaltyBookTests, added penalty from last month, points should decrease over time"
                , modifiableUser, retrievedUser);

        penaltyEventsDao.deletePenalty(fatalPenalty);
    }

}
