package blok2.database.repository;

import blok2.model.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqCategoryRepository extends JpaRepository<FaqCategory, Long> {
    List<FaqCategory> findByParentIsNull();
}
