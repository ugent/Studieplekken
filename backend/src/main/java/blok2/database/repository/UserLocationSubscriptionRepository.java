package blok2.database.repository;

import blok2.model.location.Location;
import blok2.model.location.UserLocationSubscription;
import blok2.model.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UserLocationSubscriptionRepository extends JpaRepository<UserLocationSubscription, Integer> {

    boolean existsByLocationAndUser(Location location, User user);

    @Transactional
    void deleteByLocationAndUser(Location location, User user);

    List<UserLocationSubscription> findAllByUser(User user);
}
