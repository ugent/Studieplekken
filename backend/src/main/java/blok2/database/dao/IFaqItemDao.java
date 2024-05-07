package blok2.database.dao;

import blok2.model.FaqItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IFaqItemDao {
    /**
     * Get a FAQ Item by its ID.
     *
     * @param itemId The ID of the FAQ Item.
     * @return The FAQ Item with the given ID.
     */
    FaqItem getItemById(Long itemId);

    /**
     * Get a paginated list of all FAQ items.
     *
     * @param query The search query.
     * @param pageable The pagination information.
     * @return A paginated list of all FAQ items.
     */
    List<FaqItem> searchItems(String query, Pageable pageable);

    /**
     * Get a list of all pinned FAQ items.
     *
     * @return A list of all pinned FAQ items.
     */
    List<FaqItem> getPinnedItems();

    /**
     * Add a new FAQ Item.
     */
    FaqItem addItem(FaqItem Item);

    /**
     * Update the given FAQ Item
     */
    void updateItem(FaqItem Item);
}
