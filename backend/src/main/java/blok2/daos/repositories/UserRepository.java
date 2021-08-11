package blok2.daos.repositories;

import blok2.model.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserIdAndInstitution(String userId, String institution);

    Optional<User> findByMail(String mail);

    Optional<User> findByMailAndInstitution(String mail, String institution);

    List<User> findAllByFirstName(String firstName);

    List<User> findAllByFirstNameAndInstitution(String firstName, String institution);

    List<User> findAllByLastName(String lastName);

    List<User> findAllByLastNameAndInstitution(String lastName, String institution);

    List<User> findAllByFirstNameAndLastName(String firstName, String lastName);

    List<User> findAllByFirstNameAndLastNameAndInstitution(String firstName, String lastName, String institution);

    List<User> findAllByAdminTrue();

    boolean existsByMail(String mail);

}
