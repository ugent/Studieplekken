package blok2.database.dao;

import java.util.List;

import blok2.model.translations.Language;
import blok2.model.translations.Translation;

public interface ITranslationDao {
    public List<Translation> getAllTranslations();
}
