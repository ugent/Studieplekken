package blok2.database.dao;

import blok2.model.translations.Translatable;

public interface ITranslatableDao {
    /**
     * Create multiple linked translations.
     *
     * @param translatable the translatable to create.
     * @return The created translations.
     */
    Translatable addTranslatable(Translatable translatable);

    /**
     * Get all translations for a given ID.
     *
     * @param id The ID of the translation.
     * @return A list of translations for each language.
     */
    Translatable getTranslatable(Long id);
}
