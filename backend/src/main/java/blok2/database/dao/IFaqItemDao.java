package blok2.database.dao;

import blok2.model.FaqItem;

import java.util.List;

public interface IFaqItemDao {
    /**
     * Get a FAQ Item by its ID.
     */
    FaqItem getItemById(Long ItemId);

    /**
     * Get a list of all FAQ categories.
     */
    List<FaqItem> getAllItems();

    /**
     * Add a new FAQ Item.
     */
    FaqItem addItem(FaqItem Item);

    /**
     * Update the given FAQ Item
     */
    void updateItem(FaqItem Item);
}
