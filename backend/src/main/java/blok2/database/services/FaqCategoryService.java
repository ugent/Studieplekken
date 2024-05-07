package blok2.database.services;

import blok2.database.dao.IFaqCategoryDao;
import blok2.database.repositories.FaqCategoryRepository;
import blok2.model.FaqCategory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FaqCategoryService implements IFaqCategoryDao {
    private final FaqCategoryRepository faqCategoryRepository;
    private final TranslatableService translatableService;

    public FaqCategoryService(FaqCategoryRepository faqCategoryRepository, TranslatableService translatableService) {
        this.faqCategoryRepository = faqCategoryRepository;
        this.translatableService = translatableService;
    }

    @Override
    public FaqCategory getCategoryById(Long categoryId) {
        return faqCategoryRepository.findById(categoryId).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "FAQ category not found"
                )
        );
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
    public void updateCategory(FaqCategory category) {
        faqCategoryRepository.save(category);
    }
}
