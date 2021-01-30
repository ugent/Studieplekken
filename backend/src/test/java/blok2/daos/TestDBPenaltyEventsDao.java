package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.helpers.Language;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDBPenaltyEventsDao extends BaseTest {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IBuildingDao buildingDao;

    @Autowired
    private IPenaltyEventsDao penaltyEventsDao;

    private PenaltyEvent cancellingTooLateEvent;
    private PenaltyEvent notShowingUpEvent;
    private PenaltyEvent blacklistEvent;
    private PenaltyEvent testEvent;

    private Location testLocation;
    private Building testBuilding;
    private User testUser;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Map<Language, String> cancellingTooLateDescriptions = new HashMap<>();
        cancellingTooLateDescriptions.put(Language.ENGLISH, "Cancelling too late.");
        cancellingTooLateDescriptions.put(Language.DUTCH, "Te laat annuleren.");
        cancellingTooLateEvent = new PenaltyEvent(16660, 30, cancellingTooLateDescriptions);

        Map<Language, String> notShowingUpDescriptions = new HashMap<>();
        notShowingUpDescriptions.put(Language.ENGLISH, "Not showing up at all.");
        notShowingUpDescriptions.put(Language.DUTCH, "Niet komen opdagen.");
        notShowingUpEvent = new PenaltyEvent(16661, 50, notShowingUpDescriptions);

        Map<Language, String> blacklistDescriptions = new HashMap<>();
        blacklistDescriptions.put(Language.ENGLISH, "Blacklist event.");
        blacklistDescriptions.put(Language.DUTCH, "Blacklist event.");
        blacklistEvent = new PenaltyEvent(16662, 100, blacklistDescriptions);

        Map<Language, String> testDescriptions = new HashMap<>();
        testDescriptions.put(Language.ENGLISH, "Test event.");
        testDescriptions.put(Language.DUTCH, "Test event.");
        testEvent = new PenaltyEvent(1, 10, testDescriptions);

        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        testUser = TestSharedMethods.studentTestUser();

        // Add test objects to database
        locationDao.addLocation(testLocation);
        penaltyEventsDao.addPenaltyEvent(testEvent);
        TestSharedMethods.addTestUsers(accountDao, testUser);
    }

    @Test
    public void permanentPenaltyEventsTest() throws SQLException {
        // These tests are supposed to be in the database
        PenaltyEvent test16660 = penaltyEventsDao.getPenaltyEvent(cancellingTooLateEvent.getCode());
        PenaltyEvent test16661 = penaltyEventsDao.getPenaltyEvent(notShowingUpEvent.getCode());
        PenaltyEvent test16662 = penaltyEventsDao.getPenaltyEvent(blacklistEvent.getCode());

        Assert.assertEquals("permanentEventsTest, 16660", cancellingTooLateEvent, test16660);
        Assert.assertEquals("permanentEventsTest, 16661", notShowingUpEvent, test16661);
        Assert.assertEquals("permanentEventsTest, 16662", blacklistEvent, test16662);
    }

    @Test
    public void addPenaltyEventTest() throws SQLException {
        PenaltyEvent retrievedTestEvent = penaltyEventsDao.getPenaltyEvent(testEvent.getCode());
        Assert.assertEquals("addPenaltyEventTest", testEvent, retrievedTestEvent);
    }

    @Test
    public void deleteAndAddPenaltyEventDescriptionTest() throws SQLException {
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
    public void penaltyBookTest() throws SQLException {
        // setup some test data

        LocalDate thisDayAMonthEarlier = LocalDate.now().minusMonths(1);

        Penalty penalty = new Penalty(testUser.getAugentID(), testEvent.getCode(), LocalDate.now(), LocalDate.now()
                , testLocation.getName(), testEvent.getPoints(), "regular test penalty");
        Penalty fatalPenalty = new Penalty(testUser.getAugentID(), blacklistEvent.getCode(), LocalDate.now()
                , LocalDate.of(1970, 1, 1), testLocation.getName(), blacklistEvent.getPoints(),
                "Fatal test penalty");
        Penalty penaltyLastMonth = new Penalty(testUser.getAugentID(), testEvent.getCode()
                , thisDayAMonthEarlier, thisDayAMonthEarlier, testLocation.getName(), testEvent.getPoints(),
                "Fatal test penalty of last month");

        User modifiableUser = testUser.clone();

        // Add penalty
        penaltyEventsDao.addPenalty(penalty);
        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByUser(testUser.getAugentID());
        Assert.assertEquals("penaltyBookTests, added one penalty", 1, penalties.size());
        Assert.assertEquals("penaltyBookTests, added one penalty", penalty, penalties.get(0));

        // Is the corresponding user updated correctly?
        modifiableUser.setPenaltyPoints(penalty.getReceivedPoints());
        User retrievedUser = accountDao.getUserById(testUser.getAugentID());
        Assert.assertEquals("penaltyBookTests, added one penalty", modifiableUser, retrievedUser);

        // Delete penalty
        penaltyEventsDao.deletePenalty(penalty);
        penalties = penaltyEventsDao.getPenaltiesByUser(testUser.getAugentID());
        Assert.assertEquals("penaltyBookTests, deleted penalty", 0, penalties.size());

        // Is the corresponding user updated correctly?
        modifiableUser.setPenaltyPoints(0);
        retrievedUser = accountDao.getUserById(testUser.getAugentID());
        Assert.assertEquals("penaltyBookTests, deleted penalty", modifiableUser, retrievedUser);

        // Add a penalty from last month
        penaltyEventsDao.addPenalty(penaltyLastMonth);
        penalties = penaltyEventsDao.getPenaltiesByUser(testUser.getAugentID());
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
        penalties = penaltyEventsDao.getPenaltiesByUser(testUser.getAugentID());
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
    }}

