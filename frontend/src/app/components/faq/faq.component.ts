import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {FaqCategory} from '../../model/FaqCategory';
import {FaqService} from '../../services/api/faq/faq.service';
import {TranslateService} from '@ngx-translate/core';
import {FaqItem} from '../../model/FaqItem';

@Component({
    selector: 'app-faq',
    templateUrl: './faq.component.html',
    styleUrls: ['./faq.component.scss']
})
export class FaqComponent implements OnInit{

    /* Observable of all the categories */
    protected $categories: Observable<FaqCategory[]>;

    /* Locale */
    protected locale: string;

    constructor(
        private translateService: TranslateService,
        private faqService: FaqService
    ) {
        this.locale = this.translateService.currentLang;

        this.translateService.onLangChange.subscribe(() => {
            this.locale = translateService.currentLang;
        });
    }

    /**
     * Initialize the component
     */
    public ngOnInit(): void {
        this.$categories = this.faqService.getCategories();
    }

    /**
     * Get all items from the given categories
     *
     * @param categories
     * @return FaqItem[]
     */
    public getItemsFromCategories(categories: FaqCategory[]): FaqItem[] {
        return categories.flatMap(category => category.items);
    }
}
