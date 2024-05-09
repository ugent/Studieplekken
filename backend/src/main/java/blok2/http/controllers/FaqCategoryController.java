package blok2.http.controllers;

import blok2.database.dao.IFaqCategoryDao;
import blok2.http.security.authorization.AuthorizedController;
import blok2.model.FaqCategory;
import blok2.model.FaqItem;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/{categoryId}")
    @PreAuthorize("permitAll()")
    public FaqCategory getCategoryById(@PathVariable Long categoryId) throws EntityNotFoundException {
        return this.faqCategoryDao.getCategoryById(categoryId).orElseThrow(() ->
                new EntityNotFoundException("Category not found")
        );
    }

    @GetMapping("/{categoryId}/items")
    @PreAuthorize("permitAll()")
    public List<FaqItem> getItemsByCategoryId(@PathVariable Long categoryId) {
        return this.faqCategoryDao.getCategoryById(categoryId).orElseThrow(() ->
                new EntityNotFoundException("Category not found")
        ).getItems();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public FaqCategory addCategory(@AuthenticationPrincipal User user, @RequestBody FaqCategory category) {
        category.setUser(user);
        return faqCategoryDao.addCategory(category);
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updateCategory(@PathVariable Long categoryId, @RequestBody FaqCategory category) {
        category.setId(categoryId);
        this.faqCategoryDao.updateCategory(category);
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteCategory(@PathVariable Long categoryId) {
        this.faqCategoryDao.deleteCategory(categoryId);
    }
}
