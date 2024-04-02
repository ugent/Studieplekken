package blok2.database.services;

import blok2.database.dao.IFaqCategoryDao;
import blok2.database.repositories.FaqCategoryRepository;
import blok2.exceptions.NoSuchDatabaseObjectException;
import blok2.model.FaqCategory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaqCategoryService implements IFaqCategoryDao {
    private final FaqCategoryRepository faqCategoryRepository;
    private final TranslationService translationService;

    public FaqCategoryService(FaqCategoryRepository faqCategoryRepository, TranslationService translationService) {
        this.faqCategoryRepository = faqCategoryRepository;
        this.translationService = translationService;
    }

    @Override
    public FaqCategory getCategoryById(Long categoryId) {
        return faqCategoryRepository.findById(categoryId).orElseThrow(() ->
                new NoSuchDatabaseObjectException(
                    String.format("Location with locationId '%d' does not exist.", categoryId)
                )
        );
    }

    @Override
    public List<FaqCategory> getAllCategories() {
        return faqCategoryRepository.findAll();
    }

    @Override
    public FaqCategory addCategory(FaqCategory category) {
        translationService.addTranslations(category.getName());
        return faqCategoryRepository.save(category);
    }

    @Override
    public void updateCategory(FaqCategory category) {
        faqCategoryRepository.save(category);
    }
}
