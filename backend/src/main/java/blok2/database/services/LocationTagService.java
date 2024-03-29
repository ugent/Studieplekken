package blok2.database.services;

import blok2.database.dao.ILocationTagDao;
import blok2.database.repositories.LocationRepository;
import blok2.database.repositories.LocationTagRepository;
import blok2.extensions.exceptions.NoSuchDatabaseObjectException;
import blok2.model.LocationTag;
import blok2.model.reservables.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class LocationTagService implements ILocationTagDao {

    private final LocationTagRepository locationTagRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public LocationTagService(LocationTagRepository locationTagRepository,
                              LocationRepository locationRepository) {
        this.locationTagRepository = locationTagRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public List<LocationTag> getAllLocationTags() {
        return locationTagRepository.findAll();
    }

    @Override
    public LocationTag getLocationTagById(int tagId) {
        return locationTagRepository.findById(tagId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location tag found with locationTagId '%d'", tagId)));
    }

    @Override
    public List<LocationTag> getTagsForLocation(int locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));
        return location.getAssignedTags();
    }

    @Override
    @Transactional // to lazily fetch the locations of the location tag
    public List<Location> getLocationsForTag(int tagId) {
        LocationTag tag = locationTagRepository.findById(tagId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location tag found with locationTagId '%d'", tagId)));

        List<Location> locations = tag.getLocations();
        locations.sort(Comparator.comparing(Location::getName));

        return locations;
    }

    @Override
    public LocationTag addLocationTag(LocationTag tag) {
        return locationTagRepository.save(tag);
    }

    @Override
    public void updateLocationTag(LocationTag tag) {
        locationTagRepository.save(tag);
    }

    @Override
    public void deleteLocationTag(int tagId) {
        locationTagRepository.deleteById(tagId);
    }

    @Override
    public void addTagToLocation(int locationId, int tagId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));

        LocationTag tag = locationTagRepository.findById(tagId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location tag found with locationTagId '%d'", tagId)));

        location.getAssignedTags().add(tag);
        locationRepository.save(location);
    }

    @Override
    public void bulkAddTagsToLocation(int locationId, List<Integer> tagIds) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));

        List<LocationTag> tags = locationTagRepository.findAllById(tagIds);

        location.addAllLocationTags(tags);
        locationRepository.save(location);
    }

    @Override
    public void deleteTagFromLocation(int locationId, int tagId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));

        LocationTag tag = locationTagRepository.findById(tagId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location tag found with locationTagId '%d'", tagId)));

        location.removeLocationTag(tag);
        locationRepository.save(location);
    }

    @Override
    public void deleteAllTagsFromLocation(int locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));

        location.clearAllLocationTags();
        locationRepository.save(location);
    }

    @Override
    @Transactional // to lazily fetch the locations of the location tag
    public void deleteTagFromAllLocations(int tagId) {
        LocationTag tag = locationTagRepository.findById(tagId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location tag found with locationTagId '%d'", tagId)));

        List<Location> locations = tag.getLocations();
        locations.forEach((Location location) -> location.removeLocationTag(tag));

        locationRepository.saveAll(locations);
    }

}
