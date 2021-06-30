package blok2.daos.services;

import blok2.daos.ILocationTagDao;
import blok2.daos.repositories.LocationRepository;
import blok2.daos.repositories.LocationTagRepository;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
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
    public boolean addTagToLocation(int locationId, int tagId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));

        LocationTag tag = locationTagRepository.findById(tagId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location tag found with locationTagId '%d'", tagId)));

        location.getAssignedTags().add(tag);
        location = locationRepository.saveAndFlush(location);

        return location.getAssignedTags().contains(tag);
    }

    @Override
    public boolean bulkAddTagsToLocation(int locationId, List<Integer> tagIds) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));

        List<LocationTag> tags = locationTagRepository.findAllById(tagIds);

        location.addAllLocationTags(tags);
        location = locationRepository.saveAndFlush(location);

        return location.getAssignedTags().containsAll(tags);
    }

    @Override
    public boolean deleteTagFromLocation(int locationId, int tagId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));

        LocationTag tag = locationTagRepository.findById(tagId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location tag found with locationTagId '%d'", tagId)));

        location.removeLocationTag(tag);
        location = locationRepository.saveAndFlush(location);

        return !location.getAssignedTags().contains(tag);
    }

    @Override
    public boolean deleteAllTagsFromLocation(int locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));

        location.clearAllLocationTags();
        location = locationRepository.saveAndFlush(location);

        return location.getAssignedTags().isEmpty();
    }

    @Override
    @Transactional // to lazily fetch the locations of the location tag
    public boolean deleteTagFromAllLocations(int tagId) {
        LocationTag tag = locationTagRepository.findById(tagId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location tag found with locationTagId '%d'", tagId)));

        List<Location> locations = tag.getLocations();
        locations.forEach((Location location) -> location.removeLocationTag(tag));

        locations = locationRepository.saveAll(locations);
        boolean match = locations.stream()
                .anyMatch((Location location) -> location.getAssignedTags().contains(tag));

        return !match;
    }

}
