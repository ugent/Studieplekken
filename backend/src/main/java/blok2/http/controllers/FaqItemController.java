package blok2.http.controllers;

import blok2.database.dao.IFaqCategoryDao;
import blok2.database.dao.IFaqItemDao;
import blok2.http.security.authorization.AuthorizedController;
import blok2.model.FaqCategory;
import blok2.model.FaqItem;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<FaqItem> getAllItems() {
        return faqItemDao.getAllItems();
    }

    @PostMapping
    @PreAuthorize("permitAll() or hasAuthority('ADMIN')")
    public FaqItem addCategory(@RequestBody FaqItem item) {
        return faqItemDao.addItem(item);
    }
}
