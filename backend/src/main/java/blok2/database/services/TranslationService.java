package blok2.database.services;

import blok2.database.dao.ITranslationDao;
import blok2.database.repositories.TranslationRepository;
import blok2.model.translations.Translation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TranslationService implements ITranslationDao {
    private final TranslationRepository translationRepository;

    @Autowired
    public TranslationService(TranslationRepository translationRepository) {
        this.translationRepository = translationRepository;
    }

    @Override
    public List<Translation> getAllTranslations() {
        return this.translationRepository.findAll();
    }
}
