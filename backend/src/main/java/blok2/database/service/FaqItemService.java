package blok2.database.service;

import blok2.database.dao.IFaqItemDao;
import blok2.database.repository.FaqItemRepository;
import blok2.model.FaqItem;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FaqItemService implements IFaqItemDao {
    private final FaqItemRepository faqItemRepository;
    private final TranslatableService translatableService;

    public FaqItemService(FaqItemRepository faqItemRepository, TranslatableService translatableService) {
        this.faqItemRepository = faqItemRepository;
        this.translatableService = translatableService;
    }

    @Override
    public FaqItem getItemById(Long categoryId) {
        return faqItemRepository.findById(categoryId).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "FAQ item not found"
                )
        );
    }

    @Override
    public List<FaqItem> searchItems(String query, Pageable pageable) {
        return faqItemRepository.findAll();
    }

    @Override
    public List<FaqItem> getPinnedItems() {
        return faqItemRepository.findAllByIsPinnedIsTrue();
    }

    @Override
    public FaqItem addItem(FaqItem item) {
        translatableService.addTranslatable(item.getTitle());
        translatableService.addTranslatable(item.getContent());
        return faqItemRepository.save(item);
    }

    @Override
    public void deleteItem(Long itemId) {
        faqItemRepository.deleteById(itemId);
    }

    @Override
    public void updateItem(FaqItem category) {
        faqItemRepository.save(category);
    }
}
