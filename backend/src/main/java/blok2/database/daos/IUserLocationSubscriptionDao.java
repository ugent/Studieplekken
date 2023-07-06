package blok2.daos;

import blok2.model.reservables.Location;
import blok2.model.users.User;

import java.util.List;

public interface IUserLocationSubscriptionDao {
    void subscribeToLocation(Location location, User user);

    void unsubscribeFromLocation(Location location, User user);

    void initializeSubscribed(Location location, User user);

    List<Location> getSubscribedLocations(User user);
}
