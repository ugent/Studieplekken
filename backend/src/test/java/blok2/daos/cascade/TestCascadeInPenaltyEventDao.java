package blok2.daos.cascade;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.daos.*;
import blok2.daos.db.ADB;
import blok2.helpers.Language;
import blok2.helpers.Resources;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
    private IBuildingDao buildingDao;

    @Autowired
    private ADB adb;

    private PenaltyEvent testPenaltyEvent;

    private Penalty testPenalty1;
    private Penalty testPenalty2;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        User testUser1 = TestSharedMethods.studentTestUser();
        User testUser2 = TestSharedMethods.adminTestUser();

        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        Location testLocation1 = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        Location testLocation2 = TestSharedMethods.testLocation2(authority.clone(), testBuilding);

        Map<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.DUTCH, "Dit is een test omschrijving van een penalty event met code 0");
        descriptions.put(Language.ENGLISH, "This is a test description of a penalty event with code 0");
        testPenaltyEvent = new PenaltyEvent(0, 10, descriptions);

        // Note: the received amount of points are 10 and 20, not testPenaltyEvent.getCode()
        // because when the penalties are retrieved from the penaltyEventDao, the list will
        // be sorted by received points before asserting, if they would be equal we can't sort
        // on the points and be sure about the equality of the actual and expected list.
        testPenalty1 = new Penalty(testUser1.getUserId(), testPenaltyEvent.getCode(), LocalDate.now(), LocalDate.now(), testLocation1.getLocationId(), 10, "First test penalty");
        testPenalty2 = new Penalty(testUser2.getUserId(), testPenaltyEvent.getCode(), LocalDate.now(), LocalDate.now(), testLocation2.getLocationId(), 20, "Second test penalty");


        // Add test objects to database
        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);

        // now the location is added to the db, and the ids are set correctly
        testPenalty1.setReservationLocationId(testLocation1.getLocationId());
        testPenalty2.setReservationLocationId(testLocation2.getLocationId());

        userDao.addUser(testUser1);
        userDao.addUser(testUser2);

        penaltyEventsDao.addPenaltyEvent(testPenaltyEvent);
        penaltyEventsDao.addPenalty(testPenalty1);
        penaltyEventsDao.addPenalty(testPenalty2);
    }

    @Test
    public void updatePenaltyEventWithoutNeedOfCascade() throws SQLException {
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
    public void updatePenaltyEventWithNeedOfCascade() throws SQLException {
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

        Map<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.ENGLISH, "This is a changed descriptions for the penalty event");
        descriptions.put(Language.DUTCH, "Dit is een aangepaste omschrijving van het penalty event");
        penaltyEvent.setDescriptions(descriptions);
    }

    private int countDescriptionsOfPenaltyEvent(int code) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_descriptions_of_penalty_events"));
            pstmt.setInt(1, code);

            ResultSet rs = pstmt.executeQuery();
            rs.next(); // will always be true, it's a count. If a problem would occur, a SQLException will be thrown
            return rs.getInt(1);
        }
    }
}
