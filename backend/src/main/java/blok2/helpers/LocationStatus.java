package blok2.helpers;

/**
 * If you change anything here, you also must change the app.constants.ts in frontend
 */
public enum LocationStatus {
    OPEN,            // today() is opened, and time() is inside opening hours
    CLOSED_UPCOMING, // today() is closed, but there is a calendar period for the future
    CLOSED_ACTIVE,   // today() is opened, but time() is outside opening hours
    CLOSED           // today() is closed, and no upcoming calendar periods for the future
}
