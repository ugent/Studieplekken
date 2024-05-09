package blok2.database.repositories;

import blok2.model.FaqItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FaqItemRepository extends JpaRepository<FaqItem, Long> {
    /**
     * Get all pinned faq items.
     *
     * @return list of pinned faq items
     */
    List<FaqItem> findAllByIsPinnedIsTrue();
}
