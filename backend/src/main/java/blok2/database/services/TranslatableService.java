package blok2.database.services;

import blok2.database.dao.ITranslatableDao;
import blok2.database.repositories.TranslatableRepository;
import blok2.model.translations.Translatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TranslationService implements ITranslatableDao {
    private final TranslatableRepository translationRepository;

    @Autowired
    public TranslationService(TranslatableRepository translationRepository) {
        this.translationRepository = translationRepository;
    }

    @Override
    public Translatable addTranslation(Translatable translation) {
        return translationRepository.save(translation);
    }

    @Override
    public List<Translatable> addTranslations(List<Translatable> translations) {
        if (!translations.isEmpty()) {
            // Save a translation base that auto-generates a key.
            Translatable base = translationRepository.save(translations.get(0));

            for (Translatable translation : translations) {
                translation.setId(base.getId());
                translationRepository.save(translation);
            }
        }

        return translations;
    }

    @Override
    public List<Translatable> getTranslations(Long id) {
        return this.translationRepository.findAllById(id);
    }
}
