package blok2.database.dao;

import java.util.List;

import blok2.model.translations.Translatable;

public interface ITranslationDao {
    /**
     * Create a new translation.
     *
     * @param translation The translation to create.
     * @return The created translation.
     */
    Translatable addTranslation(Translatable translation);

    /**
     * Create multiple linked translations.
     *
     * @param translation The translations to create.
     * @return The created translations.
     */
    List<Translatable> addTranslations(List<Translatable> translation);

    /**
     * Get all translations for a given ID.
     *
     * @param id The ID of the translation.
     * @return A list of translations for each language.
     */
    List<Translatable> getTranslations(Long id);
}
