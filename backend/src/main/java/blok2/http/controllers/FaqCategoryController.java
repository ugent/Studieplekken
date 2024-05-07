package blok2.http.controllers;

import blok2.database.dao.IFaqCategoryDao;
import blok2.http.security.authorization.AuthorizedController;
import blok2.model.FaqCategory;
import blok2.model.FaqItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("faq/categories")
public class FaqCategoryController extends AuthorizedController {
    private final IFaqCategoryDao faqCategoryDao;

    @Autowired
    public FaqCategoryController(IFaqCategoryDao faqCategoryDao) {
        this.faqCategoryDao = faqCategoryDao;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<FaqCategory> getAllCategories() {
        return this.faqCategoryDao.getAllCategories();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public FaqCategory addCategory(@RequestBody FaqCategory category) {
        return faqCategoryDao.addCategory(category);
    }

    @GetMapping("/{categoryId}")
    @PreAuthorize("permitAll()")
    public FaqCategory getCategoryById(@PathVariable Long categoryId) {
        return this.faqCategoryDao.getCategoryById(categoryId);
    }

    @GetMapping("/{categoryId}/items")
    @PreAuthorize("permitAll()")
    public List<FaqItem> getItemsByCategoryId(@PathVariable Long categoryId) {
        return this.faqCategoryDao.getCategoryById(categoryId).getItems();
    }
}
