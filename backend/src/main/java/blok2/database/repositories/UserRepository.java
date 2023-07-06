package blok2.daos.repositories;

import blok2.model.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserIdAndInstitution(String userId, String institution);

    Optional<User> findByMail(String mail);

    Optional<User> findByMailAndInstitution(String mail, String institution);

    List<User> findAllByFirstNameIgnoreCase(String firstName);

    List<User> findAllByFirstNameIgnoreCaseAndInstitution(String firstName, String institution);

    List<User> findAllByLastNameIgnoreCase(String lastName);

    List<User> findAllByLastNameAndInstitution(String lastName, String institution);

    List<User> findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    List<User> findAllByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndInstitution(String firstName, String lastName, String institution);

    List<User> findAllByAdminTrue();

    boolean existsByMail(String mail);

}
