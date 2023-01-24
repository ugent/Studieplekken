package blok2.daos.services;

import blok2.daos.IUserLocationSubscriptionDao;
import blok2.daos.repositories.UserLocationSubscriptionRepository;
import blok2.model.reservables.Location;
import blok2.model.reservables.UserLocationSubscription;
import blok2.model.users.User;
import org.springframework.stereotype.Service;

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

    public void initializeSubscribed(Location location, User user) {
        location.setSubscribed(user != null && userLocationSubscriptionRepository.existsByLocationAndUser(location, user));
    }
}
