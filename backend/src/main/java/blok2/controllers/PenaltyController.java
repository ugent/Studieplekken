package blok2.controllers;

import blok2.daos.IPenaltyDao;
import blok2.daos.IPenaltyEventsDao;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * This controller handles all requests related to penalties.
 * Such as adding penalties, removing penalties, ...
 */
@RestController
@RequestMapping("penalties")
public class PenaltyController {

    private final IPenaltyDao penaltyDao;

    public PenaltyController(IPenaltyDao penaltyDao) {
        this.penaltyDao = penaltyDao;
    }

    /******************************************
     *    Controller methods for Penalties    *
     ******************************************/

    @GetMapping("/{userId}")
    @PreAuthorize("(hasAuthority('USER') and #userId == authentication.principal.userId) or " +
                  "hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public PenaltyListDao getPenaltiesOfUserById(@PathVariable("userId") String userId) {
        List<Penalty> penalties = penaltyDao.getPenaltiesByUser(userId);
        int points = penaltyDao.getUserPenalty(userId);
        return new PenaltyListDao(penalties, points);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void addPenalty(@RequestBody Penalty penalty, @AuthenticationPrincipal User issuer) {
        penalty.setIssuer(issuer);
        penaltyDao.addPenalty(penalty);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void deletePenalty(@RequestBody Penalty penalty) {
        penaltyDao.deletePenalty(penalty);
    }


    public static class PenaltyListDao {
        public List<Penalty> penalties;
        public int currentPoints;

        public PenaltyListDao(List<Penalty> penalties, int currentPoints) {
            this.penalties = penalties;
            this.currentPoints = currentPoints;
        }
    }
}
