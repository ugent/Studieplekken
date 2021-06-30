package blok2.daos.repositories;

import blok2.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<Authority, Integer> {

    Optional<Authority> findByAuthorityName(String authorityName);

}
