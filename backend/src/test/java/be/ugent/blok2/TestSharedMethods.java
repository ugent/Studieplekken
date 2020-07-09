package be.ugent.blok2;

import be.ugent.blok2.daos.IDao;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.Resources;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.reservables.Location;

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

    public static Location setupTestLocation() {
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
}
