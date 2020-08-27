package be.ugent.blok2.daos;

import be.ugent.blok2.model.reservations.LockerReservation;

import java.sql.SQLException;
import java.util.List;

public interface ILockersDao extends IDao {
    /**
     * This method returns a 'locker reservation' instead of a 'locker' to get the statuses
     * of the lockers. At first, this might seem a bit strange. The reason for returning a
     * list of 'locker reservations' instead of 'lockers' is that the reservation determines
     * the status.
     *
     * If the LockerReservation
     */
    List<LockerReservation> getLockerStatusesOfLocation(String locationName) throws SQLException;
}
