package blok2.daos.cascade;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.daos.*;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class TestCascadeInPenaltyEventDao extends BaseTest {

    @Autowired
    private IUserDao userDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IPenaltyEventsDao penaltyEventsDao;

    @Autowired
    private IPenaltyDao penaltyDao;

    @Autowired
    private IBuildingDao buildingDao;

    private PenaltyEvent testPenaltyEvent;

    private Penalty testPenalty1;
    private Penalty testPenalty2;

    @Override
    public void populateDatabase() {
        // Setup test objects
        User testUser1 = TestSharedMethods.studentTestUser();
        User testUser2 = TestSharedMethods.adminTestUser();

        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        Location testLocation1 = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        Location testLocation2 = TestSharedMethods.testLocation2(authority.clone(), testBuilding);

        testPenaltyEvent = penaltyEventsDao.addPenaltyEvent(new PenaltyEvent(null, 10,
                "Dit is een test omschrijving van een penalty event",
                "This is a test description of a penalty event"));

        // Note: the received amount of points are 10 and 20, not testPenaltyEvent.getCode()
        // because when the penalties are retrieved from the penaltyEventDao, the list will
        // be sorted by received points before asserting, if they would be equal we can't sort
        // on the points and be sure about the equality of the actual and expected list.
        testPenalty1 = new Penalty(testUser1.getUserId(), testPenaltyEvent.getCode(), LocalDateTime.now(), LocalDate.now(), testLocation1, 10, "First test penalty");
        testPenalty2 = new Penalty(testUser2.getUserId(), testPenaltyEvent.getCode(), LocalDateTime.now(), LocalDate.now(), testLocation2, 20, "Second test penalty");


        // Add test objects to database
        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);

        // now the location is added to the db, and the ids are set correctly
        testPenalty1.setReservationLocation(testLocation1);
        testPenalty2.setReservationLocation(testLocation2);

        userDao.addUser(testUser1);
        userDao.addUser(testUser2);

        penaltyDao.addPenalty(testPenalty1);
        penaltyDao.addPenalty(testPenalty2);
    }

    @Test
    public void updatePenaltyEvent() {
        updatePenaltyEventWithoutChangeInFK(testPenaltyEvent);

        // PENALTY_EVENTS and PENALTY_DESCRIPTIONS updated?
        penaltyEventsDao.updatePenaltyEvent(testPenaltyEvent);
        PenaltyEvent p = penaltyEventsDao.getPenaltyEventByCode(testPenaltyEvent.getCode());
        Assert.assertEquals("updatePenaltyEventWithoutNeedOfCascade, penalty event", testPenaltyEvent, p);

        // PENALTY_BOOK updated?
        List<Penalty> penalties = penaltyDao.getPenaltiesByEventCode(testPenaltyEvent.getCode());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updatePenaltyEventWithoutNeedOfCascade, penalty book",
                expectedPenalties, penalties);
    }

    @Test
    public void deletePenaltyEventTest() {
        penaltyEventsDao.deletePenaltyEvent(testPenaltyEvent.getCode());
        try {
            penaltyEventsDao.getPenaltyEventByCode(testPenaltyEvent.getCode());
            Assert.fail("Penalty event was not deleted");
        } catch (NoSuchDatabaseObjectException ignore) {
            Assert.assertTrue(true);
        }

        List<Penalty> penalties = penaltyDao.getPenaltiesByEventCode(testPenaltyEvent.getCode());
        Assert.assertEquals("deletePenaltyEventTest, penalties", 0, penalties.size());
    }

    private void updatePenaltyEventWithoutChangeInFK(PenaltyEvent penaltyEvent) {
        penaltyEvent.setPoints(penaltyEvent.getPoints() * 2);
        penaltyEvent.setDescriptionDutch("Dit is een aangepaste omschrijving van het penalty event");
        penaltyEvent.setDescriptionEnglish("This is a changed descriptions for the penalty event");
    }

}
