package blok2.controllers;

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

    private final IPenaltyEventsDao penaltyDao;

    public PenaltyEventController(IPenaltyEventsDao penaltyDao) {
        this.penaltyDao = penaltyDao;
    }

    /******************************************
     *    Controller methods for Penalties    *
     ******************************************/

    @GetMapping("/{userId}")
    @PreAuthorize("(hasAuthority('USER') and #userId == authentication.principal.userId) or " +
                  "hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<Penalty> getPenaltiesOfUserById(@PathVariable("userId") String userId) {
        try {
            return penaltyDao.getPenaltiesByUser(userId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void addPenalty(@RequestBody Penalty penalty) {
        try {
            penaltyDao.addPenalty(penalty);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void deletePenalty(@RequestBody Penalty penalty) {
        try {
            penaltyDao.deletePenalty(penalty);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    /**********************************************
     *    Controller methods for PenaltyEvents    *
     **********************************************/

    @GetMapping("/events")
    @PreAuthorize("permitAll()")
    public List<PenaltyEvent> getAllPenaltyEvents() {
        try {
            return penaltyDao.getPenaltyEvents();
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
            penaltyDao.addPenaltyEvent(penaltyEvent);
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
            penaltyDao.updatePenaltyEvent(code, penaltyEvent);
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
            penaltyDao.deletePenaltyEvent(penaltyEvent.getCode());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
