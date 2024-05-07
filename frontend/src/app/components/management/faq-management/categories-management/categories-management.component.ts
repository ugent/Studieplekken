import {Component, Input} from '@angular/core';
import {FaqCategory} from '@/model/FaqCategory';
import {BaseManagementComponent} from '@/components/management/base-management.component';
import {TableMapper} from '@/model/Table';
import {TranslateService} from '@ngx-translate/core';
import {FormBuilder, Validators} from '@angular/forms';

@Component({
    selector: 'app-categories-management',
    templateUrl: './categories-management.component.html',
    styleUrls: ['./categories-management.component.scss']
})
export class CategoriesManagementComponent extends BaseManagementComponent<FaqCategory> {

    @Input() categories: FaqCategory[] = [];

    constructor(
        private formBuilder: FormBuilder,
        protected translateService: TranslateService
    ) {
        super();
    }

    /**
     * @inheritDoc
     */
    setupForm(category?: FaqCategory) {
        this.formGroup = this.formBuilder.group({
            name: this.formBuilder.group({
                nl: [category?.name.translations.nl, Validators.required],
                en: [category?.name.translations.en, Validators.required]
            }),
            description: this.formBuilder.group({
                nl: [category?.description.translations.nl, Validators.required],
                en: [category?.description.translations.en, Validators.required]
            })
        })
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
