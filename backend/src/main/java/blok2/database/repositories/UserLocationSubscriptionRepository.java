package blok2.database.repositories;

import blok2.model.location.Location;
import blok2.model.location.UserLocationSubscription;
import blok2.model.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface UserLocationSubscriptionRepository extends JpaRepository<UserLocationSubscription, Integer> {

    boolean existsByLocationAndUser(Location location, User user);

    @Transactional
    void deleteByLocationAndUser(Location location, User user);

    List<UserLocationSubscription> findAllByUser(User user);
}
