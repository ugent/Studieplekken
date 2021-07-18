package blok2.daos.services;

import blok2.daos.IAuthorityDao;
import blok2.daos.repositories.AuthorityRepository;
import blok2.daos.repositories.LocationRepository;
import blok2.daos.repositories.UserRepository;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthorityService implements IAuthorityDao {

    private final AuthorityRepository authorityRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuthorityService(AuthorityRepository authorityRepository, LocationRepository locationRepository,
                            UserRepository userRepository) {
        this.authorityRepository = authorityRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public List<Authority> getAllAuthorities() {
        return authorityRepository.findAll();
    }

    @Override
    public Authority getAuthorityByName(String name) {
        return authorityRepository.findByAuthorityName(name)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No authority found with authorityName '%s'", name)));
    }

    @Override
    public Authority getAuthorityByAuthorityId(int authorityId) {
        return authorityRepository.findById(authorityId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No authority found with authorityId '%d'", authorityId)));
    }

    @Override
    public List<Location> getLocationsInAuthority(int authorityId) {
        return locationRepository.findAllByAuthorityId(authorityId);
    }

    @Override
    public Authority addAuthority(Authority authority) {
        return authorityRepository.saveAndFlush(authority);
    }

    @Override
    public void updateAuthority(Authority authority) {
        authorityRepository.save(authority);
    }

    @Override
    public void deleteAuthority(int authorityId) {
        authorityRepository.deleteById(authorityId);
    }

    @Override
    public List<Authority> getAuthoritiesFromUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with userId '%s'", userId)));
        return new ArrayList<>(user.getUserAuthorities());
    }

    @Override
    public List<Location> getLocationsInAuthoritiesOfUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with userId '%s'", userId)));

        Set<Authority> authorities = user.getUserAuthorities();

        Set<Location> locations = new HashSet<>();
        authorities.forEach(authority -> locations.addAll(locationRepository
                .findAllByAuthorityId(authority.getAuthorityId())));

        return new ArrayList<>(locations);
    }

    @Override
    @Transactional
    public List<User> getUsersFromAuthority(int authorityId) {
        Authority authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No authority found with authorityId '%d'", authorityId)));

        List<User> users = authority.getUsers();
        users.size(); // trigger hibernate to load the users

        return users;
    }

    @Override
    public void addUserToAuthority(String userId, int authorityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with userId '%s'", userId)));

        Authority authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No authority found with authorityId '%d'", authorityId)));

        user.getUserAuthorities().add(authority);

        userRepository.save(user);
    }

    @Override
    public void deleteUserFromAuthority(String userId, int authorityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with userId '%s'", userId)));

        Authority authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No authority found with authorityId '%d'", authorityId)));

        user.getUserAuthorities().remove(authority);

        userRepository.save(user);
    }

}
