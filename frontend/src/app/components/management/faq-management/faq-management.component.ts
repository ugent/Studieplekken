import {Component, ViewChild} from '@angular/core';
import {FaqService} from '@/services/api/faq/faq.service';
import {BaseManagementComponent} from '@/components/management/base-management.component';
import {FaqItem} from '@/model/FaqItem';
import {ModalComponent} from '@/components/stad-gent-components/molecules/modal/modal.component';
import {EMPTY, Observable} from 'rxjs';
import {FaqCategory} from '@/model/FaqCategory';
import {AbstractControl, FormBuilder, Validators} from '@angular/forms';
import {TableMapper} from '@/model/Table';
import {TranslateService} from '@ngx-translate/core';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import {startWith, switchMap, tap} from 'rxjs/operators';

@Component({
    selector: 'app-faq-management',
    templateUrl: './faq-management.component.html',
    styleUrls: ['./faq-management.component.scss']
})
export class FaqManagementComponent extends BaseManagementComponent<FaqItem> {

    /* View children */
    @ViewChild('categoriesModal') categoriesModal: ModalComponent;

    /* Observables */
    protected $faqItems: Observable<FaqItem[]>;
    protected $faqCategories: Observable<FaqCategory[]>;

    /* State */
    protected editor: unknown = ClassicEditor;

    constructor(
        private formBuilder: FormBuilder,
        private faqService: FaqService,
        protected translateService: TranslateService
    ) {
        super();
    }

    /**
     * Initialize the component
     */
    ngOnInit(): void {
        super.ngOnInit();

        this.$faqCategories = this.refresh$.pipe(
            switchMap(() => this.faqService.getCategories())
        );

        this.$faqItems = this.refresh$.pipe(
            switchMap(() => this.faqService.getItems())
        );
    }

    /**
     * Set up the item creation form.
     */
    setupForm(item: FaqItem = FaqItem.fromJson()): void {
        this.formGroup = this.formBuilder.group({
            category: [item.category?.id, Validators.required],
            title: this.formBuilder.group({
                translations: this.formBuilder.group({
                    nl: [item.title?.translations.nl, Validators.required],
                    en: [item.title?.translations.en, Validators.required]
                })
            }),
            content: this.formBuilder.group({
                translations: this.formBuilder.group({
                    nl: [item.content?.translations.nl, Validators.required],
                    en: [item.content?.translations.en, Validators.required]
                })
            }),
            isPinned: [item.isPinned, Validators.required]
        })
    }

    /**
     * Store a new faq item.
     *
     * @param body
     */
    public store(body = this.formGroup.value): void {
        body.category = {
            id: body.category
        };

        this.sendBackendRequest(
            this.faqService.addItem(
                FaqItem.fromJson(body)
            )
        )
    }

    /**
     * Update a faq item.
     *
     * @param item
     * @param body
     */
    public update(item: FaqItem, body = this.formGroup.value): void {
        body.category = {
            id: body.category
        };

        this.sendBackendRequest(
            this.faqService.updateItem(
                item.id, FaqItem.fromJson(body)
            )
        )
    }

    /**
     * Delete a faq item.
     *
     * @param item
     */
    public delete(item: FaqItem): void {
        this.sendBackendRequest(
            this.faqService.deleteItem(item.id)
        );
    }

    /**
     * Get the table mapper for the faq items.
     *
     * @returns TableMapper<FaqItem>
     */
    getTableMapper(): TableMapper<FaqItem> {
        const locale = this.translateService.currentLang;

        return (item: FaqItem) => ({
            'management.faq.table.header.title': item.title.translations[locale],
            'management.faq.table.header.category': item.category.name.translations[locale],
            'management.faq.table.header.pinned': this.translateService.instant('management.managementTable.' + item.isPinned.toString()),
        });
    }

    /**
     * Open the modal to manage the categories.
     */
    showCategoriesModal() {
        this.categoriesModal.open();
    }
}
