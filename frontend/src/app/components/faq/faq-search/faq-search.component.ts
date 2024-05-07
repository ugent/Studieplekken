import {Component, OnInit} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {FaqItem} from '../../../model/FaqItem';
import {FaqService} from '../../../services/api/faq/faq.service';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {FaqCategory} from '../../../model/FaqCategory';
import {map, startWith, withLatestFrom} from 'rxjs/operators';

@Component({
    selector: 'app-faq-search',
    templateUrl: './faq-search.component.html',
    styleUrls: ['./faq-search.component.scss']
})
export class FaqSearchComponent implements OnInit {

    /* Observables */
    protected $pinned: Observable<FaqItem[]>;
    protected $items: Observable<FaqItem[]>;
    protected $searchItems: Observable<FaqItem[]>;

    /* Search form control */
    protected $searchSubject = new Subject<string>();

    /* State */
    protected locale: string;

    constructor(
        private translateService: TranslateService,
        private faqService: FaqService
    ) {
        this.locale = this.translateService.currentLang;

        this.translateService.onLangChange.subscribe((event: LangChangeEvent) => {
            this.locale = event.lang;
        });
    }

    /**
     * Initialize the component
     */
    ngOnInit(): void {
        this.$items = this.faqService.getItems();

        this.$pinned = this.$items.pipe(
            map((items: FaqItem[]) =>
                items.filter(item => item.isPinned)
            )
        );

        this.$searchItems = this.$searchSubject.pipe(
            startWith(''),
            withLatestFrom(this.$items),
            map(([search, items]) =>
                items.filter((item: FaqItem) => {
                    return item.title.translations[this.locale].toLowerCase().includes(search.toLowerCase()) ||
                    item.content.translations[this.locale].toLowerCase().includes(search.toLowerCase())
                })
            )
        );
    }
}
