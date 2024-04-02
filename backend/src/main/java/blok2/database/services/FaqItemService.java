package blok2.database.services;

import blok2.database.dao.IFaqItemDao;
import blok2.database.repositories.FaqItemRepository;
import blok2.exceptions.NoSuchDatabaseObjectException;
import blok2.model.FaqItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaqItemService implements IFaqItemDao {
    private final FaqItemRepository faqItemRepository;
    private final TranslationService translationService;

    public FaqItemService(FaqItemRepository faqItemRepository, TranslationService translationService) {
        this.faqItemRepository = faqItemRepository;
        this.translationService = translationService;
    }

    @Override
    public FaqItem getItemById(Long categoryId) {
        return faqItemRepository.findById(categoryId).orElseThrow(() ->
                new NoSuchDatabaseObjectException(
                    String.format("Location with locationId '%d' does not exist.", categoryId)
                )
        );
    }

    @Override
    public List<FaqItem> getAllItems() {
        return faqItemRepository.findAll();
    }

    @Override
    public FaqItem addItem(FaqItem item) {
        translationService.addTranslations(item.getTitle());
        translationService.addTranslations(item.getContent());
        return faqItemRepository.save(item);
    }

    @Override
    public void updateItem(FaqItem category) {
        faqItemRepository.save(category);
    }
}
