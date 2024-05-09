package blok2.database.dao;

import blok2.model.FaqCategory;

import java.util.List;
import java.util.Optional;

public interface IFaqCategoryDao {
    /**
     * Get a FAQ category by its ID.
     *
     * @param categoryId The ID of the FAQ category.
     * @return The FAQ category with the given ID, or null if no such category exists.
     */
    Optional<FaqCategory> getCategoryById(Long categoryId);

    /**
     * Get a list of all parent FAQ categories.
     *
     * @return A list of all parent FAQ categories.
     */
    List<FaqCategory> getAllCategories();

    /**
     * Add a new FAQ category.
     *
     * @param category The category to add.
     * @return The added category.
     */
    FaqCategory addCategory(FaqCategory category);

    /**
     * Delete the category with the given ID.
     *
     * @param categoryId The ID of the category to delete.
     */
    void deleteCategory(Long categoryId);

    /**
     * Update the given FAQ category
     *
     * @param category The category to update.
     */
    void updateCategory(FaqCategory category);
}
