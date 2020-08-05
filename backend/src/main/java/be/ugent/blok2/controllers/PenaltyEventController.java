package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.IPenaltyEventsDao;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.Pair;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.AlreadyExistsException;
import be.ugent.blok2.helpers.exceptions.NoSuchPenaltyEventException;
import be.ugent.blok2.model.penalty.Penalty;
import be.ugent.blok2.model.penalty.PenaltyEvent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/**
 * This controller handles all requests related to penalties.
 * Such as adding penalties, removing penalties, ...
 */
@RestController
@RequestMapping("api/penalties")
@PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
@Api(value = "Penalty events system")
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
