package blok2.http.controllers;

import blok2.database.dao.ILocationTagDao;
import blok2.http.security.authorization.AuthorizedLocationController;
import blok2.model.LocationTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("tags")
public class TagsController extends AuthorizedLocationController {

    private final ILocationTagDao locationTagDao;

    @Autowired
    public TagsController(ILocationTagDao locationTagDao) {
        this.locationTagDao = locationTagDao;
    }

    /*****************************************************
     *   API calls for CRUD operations with public.TAGS  *
     *****************************************************/

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<LocationTag> getAllTags() {
        return locationTagDao.getAllLocationTags();
    }

    @GetMapping("/{tagId}")
    @PreAuthorize("permitAll()")
    public LocationTag getTag(@PathVariable int tagId) {
        return locationTagDao.getLocationTagById(tagId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addTag(@RequestBody LocationTag tag) {
        locationTagDao.addLocationTag(tag);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updateTag(@RequestBody LocationTag tag) {
        locationTagDao.updateLocationTag(tag);
    }

    @DeleteMapping("/{tagId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteTag(@PathVariable int tagId) {
        locationTagDao.deleteLocationTag(tagId);
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
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public void assignTagsToLocation(@PathVariable("locationId") int locationId,
                                     @RequestBody List<LocationTag> tags) {
        isAuthorized(locationId);
        List<Integer> lt = tags.stream().map(LocationTag::getTagId).collect(Collectors.toList());
        locationTagDao.deleteAllTagsFromLocation(locationId);
        locationTagDao.bulkAddTagsToLocation(locationId, lt);
    }

}
