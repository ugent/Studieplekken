package blok2.database.repositories;

import blok2.model.translations.TranslationId;
import org.springframework.data.jpa.repository.JpaRepository;
import blok2.model.translations.Translation;

public interface TranslationRepository extends JpaRepository<Translation, TranslationId> {
    
}
