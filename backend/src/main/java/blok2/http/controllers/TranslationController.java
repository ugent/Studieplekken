package blok2.http.controllers;

import blok2.database.dao.ITranslationDao;
import blok2.http.security.authorization.AuthorizedController;
import blok2.model.translations.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("translations")
public class TranslationController extends AuthorizedController {
    private final Logger logger = LoggerFactory.getLogger(
            LocationController.class.getSimpleName()
    );
    private final ITranslationDao translationDao;

    @Autowired
    public TranslationController(ITranslationDao translationDao) {
        this.translationDao = translationDao;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Translation> getAllTranslations() {
        return this.translationDao.getAllTranslations();
    }

    @PostMapping
    @PreAuthorize("permitAll() or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<Translation> addTranslations(@RequestBody List<Translation> translations) {
        if (!translations.isEmpty()) {
            translations = translationDao.addTranslations(translations);
        }

        return translations;
    }
}
