package blok2.database.services;

import blok2.database.dao.ITranslatableDao;
import blok2.database.repositories.TranslatableRepository;
import blok2.database.repositories.TranslationRepository;
import blok2.model.translations.Language;
import blok2.model.translations.Translatable;
import blok2.model.translations.Translation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class TranslatableService implements ITranslatableDao {
    private final TranslatableRepository translatableRepository;

    @Autowired
    public TranslatableService(TranslatableRepository translatableRepository) {
        this.translatableRepository = translatableRepository;
    }

    @Override
    public Translatable addTranslatable(Translatable translatable) {
        return translatableRepository.save(translatable);
    }

    @Override
    public Translatable getTranslatable(Long id) {
        return translatableRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Translatable not found"
                )
        );
    }
}
