package blok2.database.repositories;

import blok2.model.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqCategoryRepository extends JpaRepository<FaqCategory, Long> {
}
