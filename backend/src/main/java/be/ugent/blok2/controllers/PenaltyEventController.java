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

import java.util.Comparator;
import java.util.List;

/**
 * This controller handles all requests related to penalties.
 * Such as adding penalties, removing penalties, ...
 */
@RestController
@RequestMapping("api/penalties")
@PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
@Api(value="Penalty events system")
public class PenaltyEventController {
    private final IPenaltyEventsDao penaltyDao;

    public PenaltyEventController(IPenaltyEventsDao penaltyDao) {
        this.penaltyDao = penaltyDao;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "View a list of penalties")
    public List<PenaltyEvent> getPenaltyEvents() {
        List<PenaltyEvent> ret = penaltyDao.getPenaltyEvents();
        sort(ret);  // sort based on code, in ascending order
        return ret;
    }

    @GetMapping("/{code}")
    @ApiOperation(value = "View a penalty")
    public ResponseEntity<Object> getPenaltyEvent(@PathVariable("code") int code) {
        try {
            return new ResponseEntity<>(penaltyDao.getPenaltyEvent(code), HttpStatus.OK);
        } catch(NoSuchPenaltyEventException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "View a list of penalties assigned to the user with the given id")
    public List<Penalty> getPenalties(@PathVariable("id") String augentID) {
        return penaltyDao.getPenalties(augentID);
    }

    @GetMapping("/cancelPoints/{date}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Calculates the penalty points that a user will receive if he cancel at this moment for the given date")
    public int getCancelPoints(@PathVariable("date") String d) throws Exception {
        int points = penaltyDao.getPenaltyEvent(PenaltyEvent.CODE_LATE_CANCEL).getPoints();
        return Penalty.calculateLateCancelPoints(CustomDate.parseString(d), points);
    }

    @PostMapping("/{code}")
    @ApiOperation(value = "Create a new penalty")
    public ResponseEntity addPenaltyEvent(@PathVariable int code, @RequestBody PenaltyEvent event) {
        if (code != event.getCode()) {
            return new ResponseEntity("URL code is in conflict with event's (from body) code", HttpStatus.BAD_REQUEST);
        }

        try {
            penaltyDao.addPenaltyEvent(event);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (AlreadyExistsException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value="/description")
    @ApiOperation(value = "Create a new description for a penaltyevent")
    public ResponseEntity addDescription(@RequestParam("code") int code, @RequestParam("language") Language language, @RequestParam("description") String description) {
        try {
            penaltyDao.addDescription(code, language, description);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (AlreadyExistsException e  ) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NoSuchPenaltyEventException e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{code}")
    @ApiOperation(value = "Update a penalty event")
    public ResponseEntity updatePenaltyEvent(@PathVariable("code") int code, @RequestBody PenaltyEvent event) {
        if (code != event.getCode()) {
            return new ResponseEntity("URL code is in conflict with event's (from body) code", HttpStatus.BAD_REQUEST);
        }
        try {
            penaltyDao.updatePenaltyEvent(code, event);
            return new ResponseEntity(HttpStatus.OK);
        } catch (NoSuchPenaltyEventException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/user/{id}")
    @ApiOperation(value = "Update penalties of a user")
    public ResponseEntity updatePenalties(@PathVariable("id") String augentID, @RequestBody Pair<List<Penalty>, List<Penalty>> pair) {
        penaltyDao.updatePenalties(augentID, pair.getFirst(), pair.getSecond());
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/{code}")
    @ApiOperation(value = "Delete a penaltyevent")
    public ResponseEntity deletePenaltyEvent(@PathVariable("code") int code) {
        try {
            penaltyDao.deletePenaltyEvent(code);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (NoSuchPenaltyEventException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/description")
    @ApiOperation(value = "Delete a description of a penaltyevent")
    public ResponseEntity deleteDescription(@RequestParam("code") int code, @RequestParam("language") Language language) {
        try {
            penaltyDao.deleteDescription(code, language);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (NoSuchPenaltyEventException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    private void sort(List<PenaltyEvent> list) {
        if (list != null)
            list.sort(Comparator.comparingInt(PenaltyEvent::getCode));
    }
}
