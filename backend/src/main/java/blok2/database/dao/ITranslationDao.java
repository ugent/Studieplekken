package blok2.database.dao;

import java.util.List;

import blok2.model.translations.Language;
import blok2.model.translations.Translation;

public interface ITranslationDao {
    /**
     * Get translation by their ID.
     * 
     * @param id
     * @return the translations with specified ID.
     */
    List<Translation> getTranslationsById(int id);

    /**
     * Get a specific translation by its language.
     * 
     * @param id
     * @param language
     * @return the translation with specified language.
     */
    Translation getTranslationByIdAndLanguage(int id, Language language);
}
