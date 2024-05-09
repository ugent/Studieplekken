import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FaqCategory} from '@/model/FaqCategory';
import {BaseManagementComponent} from '@/components/management/base-management.component';
import {TableMapper} from '@/model/Table';
import {TranslateService} from '@ngx-translate/core';
import {FormBuilder, Validators} from '@angular/forms';
import {FaqService} from '@/services/api/faq/faq.service';
import {tap} from 'rxjs/operators';

@Component({
    selector: 'app-categories-management',
    templateUrl: './categories-management.component.html',
    styleUrls: ['./categories-management.component.scss']
})
export class CategoriesManagementComponent extends BaseManagementComponent<FaqCategory> {

    @Input() categories: FaqCategory[] = [];
    @Output() categoriesChange = new EventEmitter();

    constructor(
        private formBuilder: FormBuilder,
        protected translateService: TranslateService,
        protected faqService: FaqService
    ) {
        super();
    }

    /**
     * @inheritDoc
     */
    setupForm(category?: FaqCategory) {
        this.formGroup = this.formBuilder.group({
            name: this.formBuilder.group({
                translations: this.formBuilder.group({
                    nl: [category?.name.translations.nl, Validators.required],
                    en: [category?.name.translations.en, Validators.required]
                })
            }),
            description: this.formBuilder.group({
                translations: this.formBuilder.group({
                    nl: [category?.description.translations.nl, Validators.required],
                    en: [category?.description.translations.en, Validators.required]
                })
            })
        })
    }

    /**
     * Store a new faq item.
     *
     * @param body
     */
    public store(body = this.formGroup.value): void {
        this.sendBackendRequest(
            this.faqService.addCategory(
                FaqCategory.fromJson(body)
            ).pipe(
                tap(() => this.categoriesChange.emit())
            )
        )
    }

    /**
     * Update a faq item.
     *
     * @param category
     * @param body
     */
    public update(category: FaqCategory, body = this.formGroup.value): void {
        this.sendBackendRequest(
            this.faqService.updateCategory(
                category.id, FaqCategory.fromJson(body)
            ).pipe(
                tap(() => this.categoriesChange.emit())
            )
        )
    }

    /**
     * Delete a faq item.
     *
     * @param item
     */
    public delete(item: FaqCategory): void {
        this.sendBackendRequest(
            this.faqService.deleteCategory(item.id).pipe(
                tap(() => this.categoriesChange.emit())
            )
        );
    }

    /**
     * @inheritDoc
     */
    getTableMapper(): TableMapper<FaqCategory> {
        const locale = this.translateService.currentLang;

        return (category: FaqCategory) => ({
            'management.faq.categories.table.header.title': category.name.translations[locale]
        });
    }
}
