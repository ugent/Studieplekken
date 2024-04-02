package blok2.database.dao;

import blok2.model.FaqCategory;

import java.util.List;

public interface IFaqCategoryDao {
    /**
     * Get a FAQ category by its ID.
     */
    FaqCategory getCategoryById(Long categoryId);

    /**
     * Get a list of all FAQ categories.
     */
    List<FaqCategory> getAllCategories();

    /**
     * Add a new FAQ category.
     */
    FaqCategory addCategory(FaqCategory category);

    /**
     * Update the given FAQ category
     */
    void updateCategory(FaqCategory category);
}
