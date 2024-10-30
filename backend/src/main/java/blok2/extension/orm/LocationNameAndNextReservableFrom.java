package blok2.extension.orm;

import java.util.Date;

// Helper for named query Location.getNextReservationMomentsOfAllLocations
public interface LocationNameAndNextReservableFrom {
    String getLocationName();
    Date getNextReservableFrom();
}
