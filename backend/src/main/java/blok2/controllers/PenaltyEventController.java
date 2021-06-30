package blok2.controllers;

import blok2.daos.IPenaltyDao;
import blok2.daos.IPenaltyEventsDao;
import blok2.helpers.Language;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This controller handles all requests related to penalties.
 * Such as adding penalties, removing penalties, ...
 */
@RestController
@RequestMapping("penalties")
public class PenaltyEventController {

    private final Logger logger = Logger.getLogger(PenaltyEventController.class.getSimpleName());

    private final IPenaltyEventsDao penaltyEventsDao;
    private final IPenaltyDao penaltyDao;

    public PenaltyEventController(IPenaltyEventsDao penaltyEventsDao,
                                  IPenaltyDao penaltyDao) {
        this.penaltyEventsDao = penaltyEventsDao;
        this.penaltyDao = penaltyDao;
    }

    /******************************************
     *    Controller methods for Penalties    *
     ******************************************/

    @GetMapping("/{userId}")
    @PreAuthorize("(hasAuthority('USER') and #userId == authentication.principal.userId) or " +
                  "hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<Penalty> getPenaltiesOfUserById(@PathVariable("userId") String userId) {
        return penaltyDao.getPenaltiesByUser(userId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void addPenalty(@RequestBody Penalty penalty) {
        penaltyDao.addPenalty(penalty);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void deletePenalty(@RequestBody Penalty penalty) {
        penaltyDao.deletePenalty(penalty);
    }

    /**********************************************
     *    Controller methods for PenaltyEvents    *
     **********************************************/

    @GetMapping("/events")
    @PreAuthorize("permitAll()")
    public List<PenaltyEvent> getAllPenaltyEvents() {
        try {
            return penaltyEventsDao.getPenaltyEvents();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping("/events")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addPenaltyEvent(@RequestBody PenaltyEvent penaltyEvent) {
        try {
            if (penaltyEvent.getDescriptions().size() != Language.values().length) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Not all languages have a description");
            }
            penaltyEventsDao.addPenaltyEvent(penaltyEvent);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/events/{code}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updatePenaltyEvent(@PathVariable("code") int code, @RequestBody PenaltyEvent penaltyEvent) {
        try {
            if (penaltyEvent.getDescriptions().size() != Language.values().length) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Not all languages have a description");
            }
            penaltyEventsDao.updatePenaltyEvent(code, penaltyEvent);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping("/events")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deletePenaltyEvent(@RequestBody PenaltyEvent penaltyEvent) {
        try {
            penaltyEventsDao.deletePenaltyEvent(penaltyEvent.getCode());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
