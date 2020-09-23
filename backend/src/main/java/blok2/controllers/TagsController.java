package blok2.controllers;

import blok2.daos.ILocationTagDao;
import blok2.daos.ITagsDao;
import blok2.model.LocationTag;
import blok2.model.reservables.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/tags")
public class TagsController {

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

    @GetMapping("/location/{locationName}")
    public List<LocationTag> getTagsOfLocation(@PathVariable("locationName") String locationName) {
        try {
            return locationTagDao.getTagsForLocation(locationName);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
