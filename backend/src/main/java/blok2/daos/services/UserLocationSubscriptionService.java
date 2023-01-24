package blok2.daos.services;

import blok2.daos.IUserLocationSubscriptionDao;
import blok2.daos.repositories.UserLocationSubscriptionRepository;
import blok2.model.reservables.Location;
import blok2.model.reservables.UserLocationSubscription;
import blok2.model.users.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserLocationSubscriptionService implements IUserLocationSubscriptionDao {

    private final UserLocationSubscriptionRepository userLocationSubscriptionRepository;

    public UserLocationSubscriptionService(UserLocationSubscriptionRepository userLocationSubscriptionRepository) {
        this.userLocationSubscriptionRepository = userLocationSubscriptionRepository;
    }

    @Override
    public void subscribeToLocation(Location location, User user) {
        userLocationSubscriptionRepository.save(new UserLocationSubscription(user, location));
    }

    @Override
    public void unsubscribeFromLocation(Location location, User user) {
        userLocationSubscriptionRepository.deleteByLocationAndUser(location, user);
    }

    @Override
    public void initializeSubscribed(Location location, User user) {
        location.setSubscribed(user != null && userLocationSubscriptionRepository.existsByLocationAndUser(location, user));
    }

    @Override
    public List<Location> getSubscribedLocations(User user) {
        return userLocationSubscriptionRepository.findAllByUser(user).stream()
                .map(UserLocationSubscription::getLocation)
                .collect(Collectors.toList());
    }
}
