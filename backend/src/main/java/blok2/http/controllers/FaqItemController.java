package blok2.http.controllers;

import blok2.database.dao.IFaqCategoryDao;
import blok2.database.dao.IFaqItemDao;
import blok2.http.security.authorization.AuthorizedController;
import blok2.model.FaqCategory;
import blok2.model.FaqItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("faq/items")
public class FaqItemController extends AuthorizedController {
    private final IFaqItemDao faqItemDao;

    @Autowired
    public FaqItemController(IFaqItemDao faqItemDao) {
        this.faqItemDao = faqItemDao;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<FaqItem> searchItems(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return faqItemDao.searchItems(search, PageRequest.of(page, size));
    }

    @GetMapping("/pinned")
    @PreAuthorize("permitAll()")
    public List<FaqItem> getPinnedItems() {
        return faqItemDao.getPinnedItems();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public FaqItem addItem(@RequestBody FaqItem item) {
        return faqItemDao.addItem(item);
    }

    @PutMapping("/{faqItemId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updateItem(@PathVariable Long faqItemId, @RequestBody FaqItem item) {
        item.setId(faqItemId);
        faqItemDao.updateItem(item);
    }

    @DeleteMapping("/{faqItemId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteItem(@PathVariable Long faqItemId) {
        faqItemDao.deleteItem(faqItemId);
    }

    @GetMapping("/{faqItemId}")
    @PreAuthorize("permitAll()")
    public FaqItem getItem(@PathVariable Long faqItemId) {
        return faqItemDao.getItemById(faqItemId);
    }
}
