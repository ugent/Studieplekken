package blok2.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import blok2.model.translations.Translation;

public interface TranslationRepository extends JpaRepository<Translation, Translation.TranslationId> {
    
}
