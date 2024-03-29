package blok2.database.services;

import blok2.database.dao.IVolunteerDao;
import blok2.database.repositories.LocationRepository;
import blok2.database.repositories.UserRepository;
import blok2.extensions.exceptions.NoSuchDatabaseObjectException;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class VolunteerService implements IVolunteerDao {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public VolunteerService(LocationRepository locationRepository, UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public List<User> getVolunteers(int locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));

        List<User> volunteers = location.getVolunteers();
        // trigger hibernate to load the users by calling size() on the volunteers list
        // (hence the @Transactional annotation because the persistent context must remain open)
        volunteers.size();

        return volunteers;
    }

    @Override
    public List<Location> getVolunteeredLocations(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with userId '%s'", userId)));
        return new ArrayList<>(user.getUserVolunteer());
    }

    @Override
    public void addVolunteer(int locationId, String userId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with userId '%s'", userId)));

        user.getUserVolunteer().add(location);

        userRepository.save(user);
    }

    @Override
    public void deleteVolunteer(int locationId, String userId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location found with locationId '%d'", locationId)));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with userId '%s'", userId)));

        user.getUserVolunteer().remove(location);

        userRepository.save(user);
    }

}
