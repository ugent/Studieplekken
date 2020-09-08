package blok2.controllers;

import blok2.daos.ILockersDao;
import blok2.model.reservations.LockerReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("api/lockers")
public class LockersController {

    private final Logger logger = Logger.getLogger(LockersController.class.getSimpleName());

    private final ILockersDao lockersDao;

    @Autowired
    public LockersController(ILockersDao lockersDao) {
        this.lockersDao = lockersDao;
    }

    @GetMapping("/status/{locationName}")
    public List<LockerReservation> getLockerStatuses(@PathVariable("locationName") String locationName) {
        try {
            return lockersDao.getLockerStatusesOfLocation(locationName);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

}