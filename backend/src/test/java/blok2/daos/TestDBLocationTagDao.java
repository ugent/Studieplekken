package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.LocationTag;
import blok2.model.reservables.Location;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class TestDBLocationTagDao extends BaseTest {

    @Autowired
    private ILocationTagDao locationTagDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IBuildingDao buildingDao;

    @Autowired
    private ITagsDao tagsDao;

    private Location testLocation;
    private Location testLocation2;

    private LocationTag testTag;
    private LocationTag testTag2;
    private LocationTag testTag3;

    @Override
    public void populateDatabase() throws SQLException {
        // Set up test objects
        Authority testAuthority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation = TestSharedMethods.testLocation(testAuthority, testBuilding);
        testLocation2 = TestSharedMethods.testLocation2(testAuthority, testBuilding);

        testTag = TestSharedMethods.testTag();
        testTag2 = TestSharedMethods.testTag2();
        testTag3 = TestSharedMethods.testTag3();

        // Save objects in database
        locationDao.addLocation(testLocation);
        locationDao.addLocation(testLocation2);

        tagsDao.addTag(testTag);
        tagsDao.addTag(testTag2);
        tagsDao.addTag(testTag3);
    }

    // addTagToLocation, getTagsForLocation, deleteTagFromLocation
    @Test
    public void addAndDeleteLocationTagTest() throws SQLException {
        // First check that there are no previous tags
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation.getLocationId()).isEmpty());
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation2.getLocationId()).isEmpty());

        locationTagDao.addTagToLocation(testLocation.getLocationId(), testTag.getTagId());
        locationTagDao.addTagToLocation(testLocation2.getLocationId(), testTag2.getTagId());
        testLocation.getAssignedTags().add(testTag);
        testLocation2.getAssignedTags().add(testTag2);

        // Assert that location 1 has testTag but not testTag2 and the inverse for location 2
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation.getLocationId()).contains(testTag));
        Assert.assertFalse(locationTagDao.getTagsForLocation(testLocation.getLocationId()).contains(testTag2));
        Assert.assertFalse(locationTagDao.getTagsForLocation(testLocation2.getLocationId()).contains(testTag));
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation2.getLocationId()).contains(testTag2));

        // Delete tag from location
        locationTagDao.deleteTagFromLocation(testLocation.getLocationId(), testTag.getTagId());

        // Assert that the tag has been deleted from the location
        Assert.assertFalse(locationTagDao.getTagsForLocation(testLocation.getLocationId()).contains(testTag));
    }

    // addTagToLocation, getLocationsForTag
    @Test
    public void getLocationsForTagTest() throws SQLException {
        locationTagDao.addTagToLocation(testLocation.getLocationId(), testTag.getTagId());
        locationTagDao.addTagToLocation(testLocation.getLocationId(), testTag2.getTagId());
        locationTagDao.addTagToLocation(testLocation2.getLocationId(), testTag2.getTagId());
        testLocation.getAssignedTags().add(testTag);
        testLocation.getAssignedTags().add(testTag2);
        testLocation2.getAssignedTags().add(testTag2);

        Assert.assertTrue(locationTagDao.getLocationsForTag(testTag.getTagId()).contains(testLocation));
        Assert.assertTrue(locationTagDao.getLocationsForTag(testTag2.getTagId()).contains(testLocation));
        Assert.assertTrue(locationTagDao.getLocationsForTag(testTag2.getTagId()).contains(testLocation2));
    }

    // bulkAddTagToLocation, getTagsForLocation
    @Test
    public void bulkAddTagToLocationTest() throws SQLException {
        // Create a list of tags and add them in bulk
        ArrayList<Integer> tagIds = new ArrayList<>(Arrays.asList(testTag.getTagId(), testTag2.getTagId()));
        locationTagDao.bulkAddTagsToLocation(testLocation.getLocationId(), tagIds);
        testLocation.getAssignedTags().addAll(new ArrayList<>(Arrays.asList(testTag, testTag2)));

        // Assert that the addition of tags worked
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation.getLocationId()).contains(testTag));
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation.getLocationId()).contains(testTag2));
        Assert.assertFalse(locationTagDao.getTagsForLocation(testLocation2.getLocationId()).contains(testTag));
        Assert.assertFalse(locationTagDao.getTagsForLocation(testLocation2.getLocationId()).contains(testTag2));
    }

    // bulkAddTagToLocation, getTagsForLocation, deleteTagFromAllLocations, deleteAllTagsFromLocation
    @Test
    public void deleteMethodsTest() throws SQLException {
        // Create a list of tags and add them in bulk
        ArrayList<Integer> tagIds = new ArrayList<>(Arrays.asList(testTag.getTagId(), testTag2.getTagId()));
        ArrayList<Integer> tagIds2 = new ArrayList<>(Arrays.asList(testTag.getTagId(), testTag2.getTagId(), testTag3.getTagId()));
        locationTagDao.bulkAddTagsToLocation(testLocation.getLocationId(), tagIds);
        locationTagDao.bulkAddTagsToLocation(testLocation2.getLocationId(), tagIds2);
        testLocation.getAssignedTags().addAll(new ArrayList<>(Arrays.asList(testTag, testTag2)));
        testLocation2.getAssignedTags().addAll(new ArrayList<>(Arrays.asList(testTag, testTag2, testTag3)));

        // Assert that the addition of tags worked
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation.getLocationId()).contains(testTag));
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation.getLocationId()).contains(testTag2));
        Assert.assertFalse(locationTagDao.getTagsForLocation(testLocation.getLocationId()).contains(testTag3));
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation2.getLocationId()).contains(testTag));
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation2.getLocationId()).contains(testTag2));
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation2.getLocationId()).contains(testTag3));

        locationTagDao.deleteTagFromAllLocations(testTag.getTagId());

        // Assert that the deletion of tag 1 worked
        Assert.assertFalse(locationTagDao.getTagsForLocation(testLocation.getLocationId()).contains(testTag));
        Assert.assertFalse(locationTagDao.getTagsForLocation(testLocation2.getLocationId()).contains(testTag));

        locationTagDao.deleteAllTagsFromLocation(testLocation2.getLocationId());

        // Assert that all tags are deleted from location2
        Assert.assertTrue(locationTagDao.getTagsForLocation(testLocation2.getLocationId()).isEmpty());
    }

    @Test
    public void doubleAddTagToLocationTest() throws SQLException {
        // Create a list of tags and add them in bulk
        ArrayList<Integer> tagIds = new ArrayList<>(Arrays.asList(testTag.getTagId(), testTag2.getTagId(), testTag3.getTagId()));
        locationTagDao.bulkAddTagsToLocation(testLocation.getLocationId(), tagIds);
        testLocation.getAssignedTags().addAll(new ArrayList<>(Arrays.asList(testTag, testTag2, testTag3)));

        // Assert that all the tags have been assigned
        Assert.assertEquals(locationTagDao.getTagsForLocation(testLocation.getLocationId()).size(), tagIds.size());
        Assert.assertEquals(1, locationTagDao.getLocationsForTag(testTag.getTagId()).size());
        Assert.assertEquals(1, locationTagDao.getLocationsForTag(testTag2.getTagId()).size());
        Assert.assertEquals(1, locationTagDao.getLocationsForTag(testTag3.getTagId()).size());

        // Now try to add an already added tag to a location
        try {
            locationTagDao.addTagToLocation(testLocation.getLocationId(), testTag.getTagId());
            Assert.fail("Violation should be thrown on unique constraint");
        } catch (DataIntegrityViolationException ignore) {
            Assert.assertTrue(true);
        }

        // Assert that this tag is not added anymore
        Assert.assertEquals(locationTagDao.getTagsForLocation(testLocation.getLocationId()).size(), tagIds.size());
        Assert.assertEquals(locationTagDao.getLocationsForTag(testTag.getTagId()).size(), 1);
    }
}
