package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
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
import java.time.LocalDateTime;
import java.util.List;

public class TestDBPenaltyEventsDao extends BaseTest {

    @Autowired
    private IUserDao userDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IBuildingDao buildingDao;

    @Autowired
    private IPenaltyEventsDao penaltyEventsDao;

    @Autowired
    private IPenaltyDao penaltyDao;

    private PenaltyEvent cancellingTooLateEvent;
    private PenaltyEvent notShowingUpEvent;
    private PenaltyEvent blacklistEvent;
    private PenaltyEvent testEvent;

    private Location testLocation;
    private User testUser;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        cancellingTooLateEvent = new PenaltyEvent(16660, 30, "Te laat annuleren.", "Cancelling too late.");
        notShowingUpEvent = new PenaltyEvent(16661, 50, "Niet komen opdagen.", "Not showing up at all.");
        blacklistEvent = new PenaltyEvent(16662, 100, "Blacklist event.", "Blacklist event.");
        testEvent = penaltyEventsDao.addPenaltyEvent(new PenaltyEvent(null, 10,
                "Test event.", "Test event."));;

        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        testUser = TestSharedMethods.studentTestUser();

        // Add test objects to database
        locationDao.addLocation(testLocation);
        TestSharedMethods.addTestUsers(userDao, testUser);
    }

    @Test
    public void permanentPenaltyEventsTest() throws SQLException {
        // These tests are supposed to be in the database
        PenaltyEvent test16660 = penaltyEventsDao.getPenaltyEventByCode(cancellingTooLateEvent.getCode());
        PenaltyEvent test16661 = penaltyEventsDao.getPenaltyEventByCode(notShowingUpEvent.getCode());
        PenaltyEvent test16662 = penaltyEventsDao.getPenaltyEventByCode(blacklistEvent.getCode());

        Assert.assertEquals("permanentEventsTest, 16660", cancellingTooLateEvent, test16660);
        Assert.assertEquals("permanentEventsTest, 16661", notShowingUpEvent, test16661);
        Assert.assertEquals("permanentEventsTest, 16662", blacklistEvent, test16662);
    }

    @Test
    public void addPenaltyEventTest() throws SQLException {
        PenaltyEvent retrievedTestEvent = penaltyEventsDao.getPenaltyEventByCode(testEvent.getCode());
        Assert.assertEquals("addPenaltyEventTest", testEvent, retrievedTestEvent);
    }

    @Test
    public void penaltyBookTest() throws SQLException {
        // setup some test data

        LocalDateTime thisDayAMonthEarlier = LocalDateTime.now().minusMonths(1);

        Penalty penalty = new Penalty(testUser.getUserId(), testEvent.getCode(), LocalDateTime.now(), LocalDate.now()
                , testLocation, testEvent.getPoints(), "regular test penalty");
        Penalty fatalPenalty = new Penalty(testUser.getUserId(), blacklistEvent.getCode(), LocalDateTime.now()
                , LocalDate.of(1970, 1, 1), testLocation, blacklistEvent.getPoints(),
                "Fatal test penalty");
        Penalty penaltyLastMonth = new Penalty(testUser.getUserId(), testEvent.getCode()
                , thisDayAMonthEarlier, thisDayAMonthEarlier.toLocalDate(), testLocation, testEvent.getPoints(),
                "Fatal test penalty of last month");

        User modifiableUser = testUser.clone();

        // Add penalty
        penaltyDao.addPenalty(penalty);
        List<Penalty> penalties = penaltyDao.getPenaltiesByUser(testUser.getUserId());
        Assert.assertEquals("penaltyBookTests, added one penalty", 1, penalties.size());
        Assert.assertEquals("penaltyBookTests, added one penalty", penalty, penalties.get(0));

        // Is the corresponding user updated correctly?
        modifiableUser.setPenaltyPoints(penalty.getReceivedPoints());
        User retrievedUser = userDao.getUserById(testUser.getUserId());
        Assert.assertEquals("penaltyBookTests, added one penalty", modifiableUser, retrievedUser);

        // Delete penalty
        penaltyDao.deletePenalty(penalty);
        penalties = penaltyDao.getPenaltiesByUser(testUser.getUserId());
        Assert.assertEquals("penaltyBookTests, deleted penalty", 0, penalties.size());

        // Is the corresponding user updated correctly?
        modifiableUser.setPenaltyPoints(0);
        retrievedUser = userDao.getUserById(testUser.getUserId());
        Assert.assertEquals("penaltyBookTests, deleted penalty", modifiableUser, retrievedUser);

        // Add a penalty from last month
        penaltyDao.addPenalty(penaltyLastMonth);
        penalties = penaltyDao.getPenaltiesByUser(testUser.getUserId());
        Assert.assertEquals("penaltyBookTests, added penalty from last month", 1, penalties.size());
        Assert.assertEquals("penaltyBookTests, added penalty from last month"
                , penaltyLastMonth, penalties.get(0));
/*
        // Is the corresponding user updated correctly?
        retrievedUser = userDao.getUserById(testUser.getAugentID());
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
        retrievedUser = userDao.getUserById(testUser.getAugentID());
        modifiableUser.setPenaltyPoints(fatalPenalty.getReceivedPoints());
        Assert.assertEquals("penaltyBookTests, added penalty from last month, points should decrease over time"
                , modifiableUser, retrievedUser);

        penaltyEventsDao.deletePenalty(fatalPenalty);
 */
    }}

