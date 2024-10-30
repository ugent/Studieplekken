package blok2.database.service;

import blok2.database.dao.ITranslatableDao;
import blok2.database.repository.TranslatableRepository;
import blok2.model.translations.Translatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
