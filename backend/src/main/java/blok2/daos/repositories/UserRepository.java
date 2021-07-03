package blok2.daos.repositories;

import blok2.model.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByMail(String mail);

    List<User> findAllByFirstName(String firstName);

    List<User> findAllByLastName(String lastName);

    List<User> findAllByFirstNameAndLastName(String firstName, String lastName);

    List<User> findAllByAdminTrue();

    boolean existsByMail(String mail);

}
