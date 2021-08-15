package blok2.controllers;

import blok2.daos.IPenaltyDao;
import blok2.daos.IPenaltyEventsDao;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * This controller handles all requests related to penalties.
 * Such as adding penalties, removing penalties, ...
 */
@RestController
@RequestMapping("penalties")
public class PenaltyEventController {

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
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #penalty.getReservationLocation().getLocationId())")
    public void addPenalty(@RequestBody Penalty penalty) {
        penaltyDao.addPenalty(penalty);
    }

    @DeleteMapping
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #penalty.getReservationLocation().getLocationId())")
    public void deletePenalty(@RequestBody Penalty penalty) {
        penaltyDao.deletePenalty(penalty);
    }

    /**********************************************
     *    Controller methods for PenaltyEvents    *
     **********************************************/

    @GetMapping("/events")
    @PreAuthorize("permitAll()")
    public List<PenaltyEvent> getAllPenaltyEvents() {
        return penaltyEventsDao.getPenaltyEvents();
    }

    @PostMapping("/events")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addPenaltyEvent(@RequestBody PenaltyEvent penaltyEvent) {
        penaltyEventsDao.addPenaltyEvent(penaltyEvent);
    }

    @PutMapping("/events/{code}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updatePenaltyEvent(@PathVariable("code") int code, @RequestBody PenaltyEvent penaltyEvent) {
        penaltyEventsDao.updatePenaltyEvent(penaltyEvent);
    }

    @DeleteMapping("/events")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deletePenaltyEvent(@RequestBody PenaltyEvent penaltyEvent) {
        penaltyEventsDao.deletePenaltyEvent(penaltyEvent.getCode());
    }
}
