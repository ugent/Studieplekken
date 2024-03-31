package blok2.database.services;

import blok2.database.dao.ITranslationDao;
import blok2.database.repositories.TranslationRepository;
import blok2.model.translations.Translation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class TranslationService implements ITranslationDao {
    private final EntityManager entityManager;
    private final TranslationRepository translationRepository;

    @Autowired
    public TranslationService(TranslationRepository translationRepository, EntityManager entityManager) {
        this.translationRepository = translationRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Translation saveTranslation(Translation translation) {
        return translationRepository.save(translation);
    }

    @Transactional
    @Override
    public List<Translation> saveTranslations(List<Translation> translations) {
        // Save a translation base that auto-generates a key.
        Translation base = translationRepository.save(translations.get(0));
        translations.get(0).setId(base.getId());

        for (Translation translation : translations) {
            translation.setId(base.getId());
            translationRepository.save(translation);
        }

        return translations;
    }

    @Override
    public List<Translation> getAllTranslations() {
        return this.translationRepository.findAll();
    }
}
