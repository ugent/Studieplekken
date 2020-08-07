package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.ILockerReservationDao;
import be.ugent.blok2.model.reservations.LockerReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * This controller handles all requests related to lockerreservations.
 * Such as creating reservations, list of reservations, cancelling reservations, ...
 */
@RestController
@RequestMapping("api/lockers/reservations")
public class LockerReservationController extends AController {

    private final ILockerReservationDao lockerReservationDao;

    @Autowired
    public LockerReservationController(ILockerReservationDao lockerReservationDao) {
        this.lockerReservationDao = lockerReservationDao;
    }

    @GetMapping("/{userId}")
    public List<LockerReservation> getLockerReservationsOfUserById(@PathVariable("userId") String userId) {
        try {
            return lockerReservationDao.getAllLockerReservationsOfUser(userId);
        } catch (SQLException ignore) {
            return null;
        }
    }
}
