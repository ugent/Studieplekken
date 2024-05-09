package blok2.database.repositories;

import blok2.model.translations.Translatable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslatableRepository extends JpaRepository<Translatable, Long> {

}
