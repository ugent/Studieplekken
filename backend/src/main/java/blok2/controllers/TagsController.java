package blok2.controllers;

import blok2.daos.ILocationTagDao;
import blok2.daos.ITagsDao;
import blok2.helpers.authorization.AuthorizedLocationController;
import blok2.model.LocationTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("tags")
public class TagsController extends AuthorizedLocationController {

    private final Logger logger = Logger.getLogger(LocationController.class.getSimpleName());

    private final ITagsDao tagsDao;
    private final ILocationTagDao locationTagDao;

    @Autowired
    public TagsController(ITagsDao tagsDao,
                          ILocationTagDao locationTagDao) {
        this.tagsDao = tagsDao;
        this.locationTagDao = locationTagDao;
    }

    /*****************************************************
     *   API calls for CRUD operations with public.TAGS  *
     *****************************************************/

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<LocationTag> getAllTags() {
        try {
            return tagsDao.getTags();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/{tagId}")
    @PreAuthorize("permitAll()")
    public LocationTag getTag(@PathVariable int tagId) {
        try {
            return tagsDao.getTag(tagId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addTag(@RequestBody LocationTag tag) {
        try {
            tagsDao.addTag(tag);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updateTag(@RequestBody LocationTag tag) {
        try {
            tagsDao.updateTag(tag);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping("/{tagId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteTag(@PathVariable int tagId) {
        try {
            tagsDao.deleteTag(tagId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    /**************************************************************
     *   API calls for CRUD operations with public.LOCATION_TAGS  *
     **************************************************************/

    @GetMapping("/location/{locationId}")
    @PreAuthorize("permitAll()")
    public List<LocationTag> getTagsOfLocation(@PathVariable("locationId") int locationId) {
        return locationTagDao.getTagsForLocation(locationId);
    }

    @PutMapping("/location/assign/{locationId}")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void assignTagsToLocation(@PathVariable("locationId") int locationId,
                                     @RequestBody List<LocationTag> tags) {
        isAuthorized(locationId);
        List<Integer> lt = tags.stream().map(LocationTag::getTagId).collect(Collectors.toList());
        locationTagDao.deleteAllTagsFromLocation(locationId);
        locationTagDao.bulkAddTagsToLocation(locationId, lt);
    }

}
