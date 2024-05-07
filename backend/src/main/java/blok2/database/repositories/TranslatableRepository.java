package blok2.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import blok2.model.translations.Translatable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslationRepository extends JpaRepository<Translatable, TranslationId> {
    /**
     * Get all translations by specified key ID (not a primary key).
     * See the {@link Translatable} class for more information on the actual primary key.
     *
     * @param id Key ID
     * @return List of translations with specified key ID.
     */
    List<Translatable> findAllById(Long id);
}
