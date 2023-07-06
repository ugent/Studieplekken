package blok2.extensions.orm;

import java.util.Date;

// Helper for named query Location.getNextReservationMomentsOfAllLocations
public interface LocationNameAndNextReservableFrom {
    String getLocationName();
    Date getNextReservableFrom();
}
