package blok2.database.repositories;

import blok2.model.FaqItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqItemRepository extends JpaRepository<FaqItem, Long> {
}
