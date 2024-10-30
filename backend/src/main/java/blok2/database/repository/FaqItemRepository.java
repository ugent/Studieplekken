package blok2.database.repository;

import blok2.model.FaqItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqItemRepository extends JpaRepository<FaqItem, Long> {
    /**
     * Get all pinned faq items.
     *
     * @return list of pinned faq items
     */
    List<FaqItem> findAllByIsPinnedIsTrue();
}
