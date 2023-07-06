package blok2.daos.repositories;

import blok2.model.reservables.Location;
import blok2.model.reservables.UserLocationSubscription;
import blok2.model.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface UserLocationSubscriptionRepository extends JpaRepository<UserLocationSubscription, String> {

    boolean existsByLocationAndUser(Location location, User user);

    @Transactional
    void deleteByLocationAndUser(Location location, User user);

    List<UserLocationSubscription> findAllByUser(User user);
}
