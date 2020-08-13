package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.IPenaltyEventsDao;
import be.ugent.blok2.model.penalty.Penalty;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * This controller handles all requests related to penalties.
 * Such as adding penalties, removing penalties, ...
 */
@RestController
@RequestMapping("api/penalties")
public class PenaltyEventController {
    private final IPenaltyEventsDao penaltyDao;

    public PenaltyEventController(IPenaltyEventsDao penaltyDao) {
        this.penaltyDao = penaltyDao;
    }

    @GetMapping("/{userId}")
    public List<Penalty> getPenaltiesOfUserById(@PathVariable("userId") String userId) {
        try {
            return penaltyDao.getPenaltiesByUser(userId);
        } catch (SQLException ignore) {
            return null;
        }
    }
}
