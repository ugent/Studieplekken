package blok2.database.service;

import blok2.database.dao.IFaqCategoryDao;
import blok2.database.repository.FaqCategoryRepository;
import blok2.model.FaqCategory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FaqCategoryService implements IFaqCategoryDao {
    private final FaqCategoryRepository faqCategoryRepository;

    public FaqCategoryService(FaqCategoryRepository faqCategoryRepository) {
        this.faqCategoryRepository = faqCategoryRepository;
    }

    @Override
    public Optional<FaqCategory> getCategoryById(Long categoryId) {
        return faqCategoryRepository.findById(categoryId);
    }

    @Override
    public List<FaqCategory> getAllCategories() {
        return faqCategoryRepository.findByParentIsNull();
    }

    @Override
    public FaqCategory addCategory(FaqCategory category) {
        // Create the translations for the category.
        return faqCategoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        faqCategoryRepository.deleteById(categoryId);
    }

    @Override
    public void updateCategory(FaqCategory category) {
        faqCategoryRepository.save(category);
    }
}
