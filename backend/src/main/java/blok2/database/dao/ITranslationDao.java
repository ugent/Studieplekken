package blok2.database.dao;

import java.util.List;

import blok2.model.translations.Language;
import blok2.model.translations.Translation;

public interface ITranslationDao {
    /**
     * Create a new translation.
     */
    Translation addTranslation(Translation translation);

    /**
     * Create multiple linked translations.
     */
    List<Translation> addTranslations(List<Translation> translation);

    /**
     * Get all translations.
     */
    List<Translation> getAllTranslations();
}
