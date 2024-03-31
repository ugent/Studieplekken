package blok2.http.controllers;

import blok2.database.dao.ITranslationDao;
import blok2.http.security.authorization.AuthorizedController;
import blok2.model.translations.Translation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("translations")
public class TranslationController extends AuthorizedController {
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
}
